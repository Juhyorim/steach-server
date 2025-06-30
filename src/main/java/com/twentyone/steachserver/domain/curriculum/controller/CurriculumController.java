package com.twentyone.steachserver.domain.curriculum.controller;

import com.twentyone.steachserver.domain.auth.model.LoginCredential;
import com.twentyone.steachserver.domain.curriculum.dto.*;
import com.twentyone.steachserver.domain.curriculum.enums.CurriculumCategory;
import com.twentyone.steachserver.domain.curriculum.service.CurriculumService;
import com.twentyone.steachserver.domain.lecture.dto.AllLecturesInCurriculaResponseDto;
import com.twentyone.steachserver.domain.lecture.service.LectureService;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.member.model.Teacher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "커리큘럼")
@Slf4j
@RestController
@RequestMapping("/api/v1/curricula")
@RequiredArgsConstructor
public class CurriculumController {
    private final CurriculumService curriculumService;
    private final LectureService lectureService;

    @Operation(summary = "[All] 커리큘럼 단일조회!")
    @GetMapping("/{id}")
    public ResponseEntity<CurriculumDetailResponse> getDetail(@PathVariable(name = "id") Integer id) {
        CurriculumDetailResponse detail = curriculumService.getDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @Operation(summary = "[강사] 커리큘럼 생성!", description = "category종류: KOREAN, MATH, FOREIGN_LANGUAGE, SCIENCE, ENGINEERING, ARTS_AND_PHYSICAL, SOCIAL, ETC")
    @PostMapping
    public ResponseEntity<CurriculumDetailResponse> createCurriculum(
            @AuthenticationPrincipal LoginCredential credential,
            @RequestBody CurriculumAddRequest request) {
        CurriculumDetailResponse curriculumDetailResponse = curriculumService.create(credential, request);

        return ResponseEntity.ok(curriculumDetailResponse);
    }

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "[학생] 커리큘럼 수강신청!")
    @PostMapping("/{curricula_id}/apply")
    public ResponseEntity<Void> registration(@AuthenticationPrincipal LoginCredential credential,
                                             @PathVariable("curricula_id") Integer curriculaId) {
        curriculumService.registration(credential, curriculaId);

        return ResponseEntity.ok().build(); //TODO 반환값
    }

    @Secured("ROLE_STUDENT")
    @Operation(summary = "[학생] 커리큘럼 수강취소")
    @PostMapping("/{curricula_id}/cancel")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal Student student,
                                             @PathVariable("curricula_id") Integer curriculaId) {
        curriculumService.cancel(student, curriculaId);

        return ResponseEntity.ok().build(); //TODO 반환값
    }

    @Operation(summary = "[All] 커리큘럼 리스트 조회/검색", description = "lecture_start_time 은 날짜시간 같이 나옵니다. /n"
            + "pageSize: 한 페이지당 원소 개수(n개씩보기), currentPageNumber: 현재 몇 페이지, totalPage: 전체 페이지 개수 \n"
            + "pageSize나 currentPageNumber를 null로 줄 경우 전부 다 줌")
    @GetMapping
    public ResponseEntity<CurriculumListResponse> getCurricula(
            @RequestParam(value = "curriculum_category", required = false) CurriculumCategory curriculumCategory,
            @RequestParam(value = "order", required = false) CurriculaOrderType order,
            @RequestParam(value = "only_available", required = false) Boolean onlyAvailable,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "currentPageNumber", required = false) Integer currentPageNumber) {
        CurriculaSearchCondition condition = new CurriculaSearchCondition(curriculumCategory, order, onlyAvailable,
                search);
        CurriculumListResponse result = null;

        if (pageSize != null && currentPageNumber != null) {
            //페이징 처리
            int pageNumber = currentPageNumber - 1; //입력은 1부터 시작, 실제로는 0부터 시작
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            result = curriculumService.search(condition, pageable);
        } else {
            //페이징 없이 처리
            result = curriculumService.search(condition);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "[All] 커리큘럼에 해당하는 강의 리스트 조회")
    @GetMapping("/{curriculum_id}/lectures")
    public ResponseEntity<AllLecturesInCurriculaResponseDto> getLecturesByCurriculum(
            @PathVariable("curriculum_id") Integer curriculumId) {
        AllLecturesInCurriculaResponseDto byCurriculum = lectureService.findByCurriculum(curriculumId);

        return ResponseEntity.ok(byCurriculum);
    }

    @Secured("ROLE_TEACHER")
    @Operation(summary = "[강사, 관련사용자] 커리큘럼 수정", description = "커리큘럼을 만든 사람만 수정가능합니다")
    @PatchMapping("/{curriculum_id}")
    public ResponseEntity<CurriculumDetailResponse> updateCurriculum(
            @PathVariable("curriculum_id") Integer curriculumId,
            @RequestBody CurriculumAddRequest request,
            @AuthenticationPrincipal Teacher teacher) {
        return ResponseEntity.ok(curriculumService.updateCurriculum(curriculumId, teacher, request));
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/{curriculum_id}")
    @Operation(summary = "[강사] 커리큘럼 삭제")
    public ResponseEntity<Void> deleteCurriculum(
            @PathVariable("curriculum_id") Integer curriculumId,
            @AuthenticationPrincipal Teacher teacher) {
        curriculumService.deleteCurriculum(teacher, curriculumId);

        return ResponseEntity.ok().build();
    }
}
