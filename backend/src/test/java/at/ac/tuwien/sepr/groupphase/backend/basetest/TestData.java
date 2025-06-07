package at.ac.tuwien.sepr.groupphase.backend.basetest;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface TestData {

    Long ID = 1L;
    String TEST_NEWS_TITLE = "Title";
    String TEST_NEWS_SUMMARY = "Summary";
    String TEST_NEWS_TEXT = "TestMessageText";
    LocalDateTime TEST_NEWS_PUBLISHED_AT =
        LocalDateTime.of(2019, 11, 13, 12, 15, 0, 0);

    String BASE_URI = "/api/v1";
    String MESSAGE_BASE_URI = BASE_URI + "/messages";

    String ADMIN_USER_EMAIL = "admin@email.com";
    String NORMAL_USER_EMAIL = "user@email.com";

    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };
    String DEFAULT_USER = "admin@email.com";
    List<String> USER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
        }
    };

    String ADMIN_PW = "pass123";

    Long VACATION_ID = 1L;
    Long OVERLAPPING_ID = 2L;
    String EMPLOYEE_EMAIL = "user@example.com";

    LocalDate START_DATE = LocalDate.of(2025, 8, 1);
    LocalDate END_DATE = LocalDate.of(2025, 8, 10);

    LocalDate OVERLAPPING_START = LocalDate.of(2025, 8, 5);
    LocalDate OVERLAPPING_END = LocalDate.of(2025, 8, 15);

    Department DEPARTMENT = new Department();

    String SUPERVISOR_EMAIL = "supervisor@shyft.local";
    String DEPARTMENT_NAME = "TestDept";


    String NORMAL_USER_PW = "userPass123";

}
