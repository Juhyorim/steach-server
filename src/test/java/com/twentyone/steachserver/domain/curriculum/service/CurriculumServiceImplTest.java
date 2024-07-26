package com.twentyone.steachserver.domain.curriculum.service;

import com.twentyone.steachserver.domain.auth.error.ForbiddenException;
import com.twentyone.steachserver.domain.curriculum.dto.CurriculumAddRequest;
import com.twentyone.steachserver.domain.curriculum.dto.CurriculumDetailResponse;
import com.twentyone.steachserver.domain.curriculum.enums.CurriculumCategory;
import com.twentyone.steachserver.domain.curriculum.repository.CurriculumDetailRepository;
import com.twentyone.steachserver.domain.curriculum.repository.CurriculumRepository;
import com.twentyone.steachserver.domain.curriculum.repository.CurriculumSearchRepository;
import com.twentyone.steachserver.domain.curriculum.validator.CurriculumValidator;
import com.twentyone.steachserver.domain.lecture.repository.LectureRepository;
import com.twentyone.steachserver.domain.member.model.Student;
import com.twentyone.steachserver.domain.member.model.Teacher;
import com.twentyone.steachserver.domain.studentCurriculum.repository.StudentCurriculumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceImplTest {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PATH = "path";
    public static final String TITLE = "title";
    public static final String SUB_TITLE = "subTitle";
    public static final String INTRO = "intro";
    public static final String INFORMATION = "information";
    public static final CurriculumCategory CURRICULUM_CATEGORY = CurriculumCategory.EDUCATION;
    public static final String SUB_CATEGORY = "subCategory";
    public static final String BANNER_IMG_URL = "bannerImgUrl";
    public static final LocalDateTime NOW = LocalDateTime.now();
    public static final String WEEKDAY_BITMASK = "0101011";
    public static final int MAX_ATTENDEES = 4;

    @InjectMocks
    CurriculumServiceImpl curriculumService;

    @Mock
    private CurriculumRepository curriculumRepository;

    @Mock
    private CurriculumSearchRepository curriculumSearchRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CurriculumDetailRepository curriculumDetailRepository;

    @Mock
    private StudentCurriculumRepository studentCurriculumRepository;

    @Mock
    private CurriculumValidator curriculumValidator;

    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        teacher = Teacher.of(USERNAME, PASSWORD, NAME, EMAIL, PATH);
        student = Student.of(USERNAME, PASSWORD, NAME, EMAIL);
    }

    @Test
    void create() {
        //given
        CurriculumAddRequest request = new CurriculumAddRequest(TITLE, SUB_TITLE, INTRO, INFORMATION, CURRICULUM_CATEGORY, SUB_CATEGORY, BANNER_IMG_URL,
                NOW.toLocalDate(), NOW.toLocalDate(), WEEKDAY_BITMASK, NOW.toLocalTime(), NOW.toLocalTime(), MAX_ATTENDEES);

        //when
        CurriculumDetailResponse curriculumDetailResponse = curriculumService.create(teacher, request);

        //then
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 원하는 형식으로 설정
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // 원하는 형식으로 설정

        assertEquals(curriculumDetailResponse.getTitle(), TITLE);
        assertEquals(curriculumDetailResponse.getSubTitle(), SUB_TITLE);
        assertEquals(curriculumDetailResponse.getIntro(), INTRO);
        assertEquals(curriculumDetailResponse.getInformation(), INFORMATION);
        assertEquals(curriculumDetailResponse.getCategory(), CURRICULUM_CATEGORY);
        assertEquals(curriculumDetailResponse.getSubCategory(), SUB_CATEGORY);
        assertEquals(curriculumDetailResponse.getBannerImgUrl(), BANNER_IMG_URL);
        assertEquals(String.format(String.valueOf(request.getStartDate())), NOW.toLocalDate().format(dateFormatter));
        assertEquals(String.format(String.valueOf(request.getEndDate())), NOW.toLocalDate().format(dateFormatter));
        assertEquals(curriculumDetailResponse.getWeekdaysBitmask(), WEEKDAY_BITMASK);
        assertEquals(String.format(request.getLectureStartTime().format(timeFormatter)), NOW.toLocalTime().format(timeFormatter));
        assertEquals(String.format(request.getLectureEndTime().format(timeFormatter)), NOW.toLocalTime().format(timeFormatter));
        assertEquals(curriculumDetailResponse.getMaxAttendees(), MAX_ATTENDEES);
        //then
//        assertEquals(curriculumDetailResponse.getTitle(), TITLE);
//        assertEquals(curriculumDetailResponse.getSubTitle(), SUB_TITLE);
//        assertEquals(curriculumDetailResponse.getIntro(), INTRO);
//        assertEquals(curriculumDetailResponse.getInformation(), INFORMATION);
//        assertEquals(curriculumDetailResponse.getCategory(), CURRICULUM_CATEGORY);
//        assertEquals(curriculumDetailResponse.getSubCategory(), SUB_CATEGORY);
//        assertEquals(curriculumDetailResponse.getBannerImgUrl(), BANNER_IMG_URL);
//        assertEquals(curriculumDetailResponse.getStartDate(), NOW.toLocalDate());
//        assertEquals(curriculumDetailResponse.getEndDate(), NOW.toLocalDate());
//        assertEquals(curriculumDetailResponse.getWeekdaysBitmask(), WEEKDAY_BITMASK);
//        assertEquals(curriculumDetailResponse.getLectureStartTime(), NOW.toLocalTime());
//        assertEquals(curriculumDetailResponse.getLectureEndTime(), NOW.toLocalTime());
//        assertEquals(curriculumDetailResponse.getMaxAttendees(), MAX_ATTENDEES);
    }

    @Test
    void create_선생님만_가능() {
        //given
        CurriculumAddRequest request = new CurriculumAddRequest(TITLE, SUB_TITLE, INTRO, INFORMATION, CURRICULUM_CATEGORY, SUB_CATEGORY, BANNER_IMG_URL,
                NOW.toLocalDate(), NOW.toLocalDate(), WEEKDAY_BITMASK, NOW.toLocalTime(), NOW.toLocalTime(), MAX_ATTENDEES);

        //when //then
        assertThrows(ForbiddenException.class, () -> {
            curriculumService.create(student, request);
        });
    }
}
