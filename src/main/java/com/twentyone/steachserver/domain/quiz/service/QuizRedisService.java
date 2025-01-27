package com.twentyone.steachserver.domain.quiz.service;

import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.member.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class QuizRedisService {
    private final StringRedisTemplate redisTemplate;

    public void initialize(Integer lectureId, Integer quizId) {
        // 현재 랭킹을 이전 랭킹으로 복사
        // 현재 랭킹 초기화
        // 퀴즈 선택지 카운트 초기화
    }

    public void updateUserQuizScore(Lecture lecture, Student student, Integer score) {
        // ZADD lecture:{lectureId}:current_ranking
        String userKey = student.getName() + student.getId();
        String key = String.format("lecture:%d:current_ranking", lecture.getId());

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
