package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.quiz.model.Quiz;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class QuizRedisService {
    public static final String CURRENT_RANKING_FORMAT = "lecture:%d:current_ranking"; //%d = lectureId
    public static final String PREV_RANKING_FORMAT = "lecture:%d:prev_ranking"; //%d = lectureId
    private final StringRedisTemplate redisTemplate;

    public void initialize(Lecture lecture, Quiz quiz) {
        // 현재 랭킹을 이전 랭킹으로 복사
        initializePrevRanking(lecture);

        // 퀴즈 선택지 카운트 초기화
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
}
