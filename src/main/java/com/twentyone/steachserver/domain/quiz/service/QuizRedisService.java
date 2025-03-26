package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import com.twentyone.steachserver.domain.quiz.model.QuizChoice;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service //@Transactional 사용하지 않고 template을 사용
public class QuizRedisService {
    public static final String CURRENT_RANKING_FORMAT = "lecture:%d:current_ranking"; //%d = lectureId
    public static final String PREV_RANKING_FORMAT = "lecture:%d:prev_ranking"; //%d = lectureId
    public static final String QUIZ_CHOICE_COUNT_FORMAT = "quiz:%d:options"; //%d = quizId

    private final StringRedisTemplate redisTemplate;

    public void initialize(Lecture lecture, Quiz quiz) {
        // 현재 랭킹을 이전 랭킹으로 복사
        initializePrevRanking(lecture);
    }

    private void initializePrevRanking(Lecture lecture) {
        String currentKey = String.format(CURRENT_RANKING_FORMAT, lecture.getId());
        String previousKey = String.format(PREV_RANKING_FORMAT, lecture.getId());

        // 현재 랭킹을 이전 랭킹으로 복사
        Set<TypedTuple<String>> rankings = redisTemplate.opsForZSet().rangeWithScores(currentKey, 0, -1);
        if (rankings != null && !rankings.isEmpty()) {
            rankings.forEach(ranking ->
                    redisTemplate.opsForZSet().add(previousKey, ranking.getValue(), ranking.getScore())
            );
        }
    }

    public void updateUserQuizScore(Lecture lecture, Student student, Integer score) {
        // ZADD lecture:{lectureId}:current_ranking
        String userKey = student.getName() + student.getId();
        String key = String.format(CURRENT_RANKING_FORMAT, lecture.getId());

        // 현재 점수 조회
        Double currentScore = redisTemplate.opsForZSet().score(key, userKey);
        double newScore = 0;
        if (currentScore != null) {
            newScore = currentScore;
        }

        // 합산된 점수로 업데이트
        newScore += score;
        redisTemplate.opsForZSet().add(key, userKey, newScore); //TODO 닉네임 중복관련 처리 - 현재는 닉네임과 PK를 묶음
    }

    //@TODO 실패했을 때 메시지큐 등을 사용할 방법이 없는지 확인
    public void updateQuizChoiceCount(Quiz quiz, QuizChoice quizChoice) {
        final String key = String.format(QUIZ_CHOICE_COUNT_FORMAT, quiz.getId());
        final String choiceKey = String.valueOf(quizChoice.getId());

        try {
            // Redis 트랜잭션 사용
            redisTemplate.execute(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) {
                    try {
                        operations.multi(); // tx 시작

                        operations.opsForHash().increment(key, choiceKey, 1); // 카운트 증가
                        operations.expire(key, 1, TimeUnit.HOURS); // TTL 설정: 1시간

                        return operations.exec(); // tx 커밋
                    } catch (Exception e) {
                        operations.discard(); // tx 롤백: 큐 버리기
                        throw e;
                    }
                }
            });
        } catch (RuntimeException e) {
            // MySQL 로직에는 영향이 없도록 예외 처리
            log.info("Redis 트랜잭션 실패(QuizRedisService: updateQuizChoiceCount): " + e.getMessage());
        }
    }

    // 퀴즈 선택지별 선택 수 조회
    public Map<Object, Object> getQuizChoiceCounts(Integer quizId) {
        String key = String.format(QUIZ_CHOICE_COUNT_FORMAT, quizId);

        return redisTemplate.opsForHash().entries(key);
    }

    public Map<String, Double> getCurrentRanking(Integer lectureId) {
        String key = String.format(CURRENT_RANKING_FORMAT, lectureId);

        // 상위 5개 항목 조회
        Set<TypedTuple<String>> rankingSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, 4);

        Map<String, Double> rankingMap = new HashMap<>();
        if (rankingSet != null) {
            for (TypedTuple<String> tuple : rankingSet) {
                rankingMap.put(tuple.getValue(), tuple.getScore());
            }
        }

        return rankingMap;
    }
}
