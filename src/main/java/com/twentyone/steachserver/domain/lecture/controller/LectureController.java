package com.twentyone.steachserver.domain.lecture.controller;

import com.twentyone.steachserver.domain.auth.error.ForbiddenException;
import com.twentyone.steachserver.domain.classroom.dto.ClassroomResponseDto;
import com.twentyone.steachserver.domain.classroom.model.Classroom;
import com.twentyone.steachserver.domain.lecture.dto.CompletedLecturesByStudentResponseDto;
import com.twentyone.steachserver.domain.lecture.dto.CompletedLecturesResponseDto;
import com.twentyone.steachserver.domain.lecture.dto.FinalLectureInfoByTeacherDto;
import com.twentyone.steachserver.domain.lecture.dto.LectureBeforeStartingResponseDto;
import com.twentyone.steachserver.domain.lecture.dto.MyLectureHistoryResponse;
import com.twentyone.steachserver.domain.lecture.dto.update.UpdateLectureRequestDto;
import com.twentyone.steachserver.domain.lecture.error.LectureTimeNotYetException;
import com.twentyone.steachserver.domain.lecture.model.Lecture;
import com.twentyone.steachserver.domain.lecture.service.LectureService;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.statistic.service.StatisticService;
import com.twentyone.steachserver.domain.studentLecture.service.StudentLectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "강의")
@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
public class LectureController {
    private final LectureService lectureService;
    private final StudentLectureService studentLectureService;
    private final StatisticService statisticService;

    @Operation(summary = "[공통] 강의에 대한 다양한 정보 반환", description = "무조건 200을 반환, 강의에 대해서 시작 전 강의면 시작 전 형태로, 끝난 강의는 끝난형태로 반환.")
    @GetMapping("/{lectureId}")
    public ResponseEntity<?> getLectureInformation(@PathVariable("lectureId") Integer lectureId) {
        LectureBeforeStartingResponseDto lectureBeforeStartingResponseDto = lectureService.getLectureInformation(lectureId);
        if (lectureBeforeStartingResponseDto.getIsCompleted()) {
            CompletedLecturesResponseDto completedLecturesResponseDto = lectureService.getFinalLectureInformation(lectureBeforeStartingResponseDto, lectureId);
            return ResponseEntity.ok().body(completedLecturesResponseDto);
        }
        return ResponseEntity.ok().body(lectureBeforeStartingResponseDto);
    }

    @Secured("ROLE_STUDENT") //@PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "[공통] 수강한 강의에 대한 다양한 정보 반환", description = "무조건 200을 반환, 강의에 대해서 시작 전 강의면 시작 전 형태로, 끝난 강의는 끝난형태로 반환.")
    @GetMapping("/complete/student")
    public ResponseEntity<?> getLectureEndInformation(@AuthenticationPrincipal Student student){
        List<CompletedLecturesByStudentResponseDto> completedLecturesResponseDto = lectureService.getFinalLectureInformationByStudent(student);
        return ResponseEntity.ok().body(completedLecturesResponseDto);
    }

    @Operation(
            summary = "[선생님] 강의 수정!",
            description = "성공시 200 반환, 실패시 204 NO_CONTENT 반환.<br/>" +
                    "title이나 lecture_start_time이 null이면 변경안됨"
    )
    @PatchMapping("/{lectureId}")
    public ResponseEntity<?> updateLectureInformation(@PathVariable("lectureId") Integer lectureId, @RequestBody UpdateLectureRequestDto updatelectureRequestDto) {
        return lectureService.updateLectureInformation(lectureId, updatelectureRequestDto)
                .map(lectureBeforeStartingResponseDto -> ResponseEntity.ok().body(lectureBeforeStartingResponseDto))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @Operation(summary = "[학생] 강의를 들을 학생인지 확인!",
            description = "권한이 있으면 200과 sessionId 반환, 권한 있는데 강의 시작 전이면 403," +
                    "권한이 없으면 401, 학생이 아닌 권한이면 500")
    @GetMapping("/check/{lectureId}")
    public ResponseEntity<?> confirmStudentByApply(@AuthenticationPrincipal Student student, @PathVariable("lectureId") Integer lectureId) {
        Boolean isCheck = lectureService.checkStudentByLecture(student.getId(), lectureId);

        if (!isCheck) {
            throw new ForbiddenException("권한이 없습니다");
        }

        Optional<Classroom> classroomOptional = lectureService.getClassroomByLectureAndStudent(student.getId(), lectureId);
        return classroomOptional
                .map(classroom -> ResponseEntity.ok().
                        body(ClassroomResponseDto.createClassroomResponseDto(classroom)))
                .orElseThrow(() -> new LectureTimeNotYetException("강의 시간이 아님"));
    }

    @Operation(summary = "[선생님] 선생님이 강의를 끝내고 관련 정보 처리 및 최종정보 반환 ", description = "무조건 200을 반환")
    @GetMapping("/final/{lectureId}")
    public ResponseEntity<FinalLectureInfoByTeacherDto> getFinalLectureInformation(@PathVariable("lectureId") Integer lectureId) {
        Lecture updateLecture = lectureService.updateRealEndTime(lectureId);
        lectureService.addVolunteerMinute(updateLecture);
        studentLectureService.updateStudentLectureByFinishLecture(lectureId);
        statisticService.createStatisticsByFinalLecture(updateLecture);
        FinalLectureInfoByTeacherDto finalLectureInfoByTeacherDto = lectureService.getFinalLectureInformation(lectureId);
        return ResponseEntity.ok().body(finalLectureInfoByTeacherDto);
    }

    @Operation(summary = "선생님이 강의 시작을 누르면 강의 리얼 시작시간이 들어가는 메서드", description = "무조건 200을 반환")
    @PatchMapping("/start/{lectureId}")
    public ResponseEntity<?> updateRealStartTime(@PathVariable("lectureId") Integer lectureId) {
//        Todo: 자신꺼 아니면 안되게 처리해줘야하나 고민
        lectureService.updateRealStartTime(lectureId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<?> deleteLecture(@PathVariable("lectureId") Integer lectureId, @AuthenticationPrincipal Teacher teacher) {
        lectureService.delete(lectureId, teacher);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[선생님] (강의 끝난 후) 강의 레포트 반환", description = "무조건 200을 반환, 강의 끝냈을 때 데이터 똑같이 보내줌")
    @GetMapping("/report/{lectureId}")
    public ResponseEntity<FinalLectureInfoByTeacherDto> getLectureReport(@PathVariable("lectureId") Integer lectureId) {
        FinalLectureInfoByTeacherDto finalLectureInfoByTeacherDto = lectureService.getFinalLectureInformation(lectureId);
        return ResponseEntity.ok().body(finalLectureInfoByTeacherDto);
    }

    @Secured("ROLE_STUDENT")
    @Operation(summary = "[학생] 나의 강의 히스토리 반환", description = "강의 히스토리 반환")
    @GetMapping("/history")
    public ResponseEntity<MyLectureHistoryResponse> getMyLectureHistory(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(lectureService.getMyLectureHistory(student));
    }
}
