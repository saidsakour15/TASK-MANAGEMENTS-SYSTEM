package com.stms.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.stms.dto.request.CourseRequest;
import com.stms.dto.response.*;
import com.stms.entity.Course;
import com.stms.entity.CourseLecturer;
import com.stms.entity.CourseEnrollment;
import com.stms.entity.User;
import com.stms.entity.enums.Role;
import com.stms.exception.BadRequestException;
import com.stms.exception.ConflictException;
import com.stms.exception.ResourceNotFoundException;
import com.stms.repository.*;
import com.stms.service.CourseService;
import com.stms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new ConflictException("Course code already exists: " + request.getCourseCode());
        }
        Course course = Course.builder()
                .courseCode(request.getCourseCode().toUpperCase())
                .courseName(request.getCourseName())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        return CourseResponse.from(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        if (!course.getCourseCode().equals(request.getCourseCode()) &&
            courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new ConflictException("Course code already exists: " + request.getCourseCode());
        }
        course.setCourseCode(request.getCourseCode().toUpperCase());
        course.setCourseName(request.getCourseName());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        return enrichCourseResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return enrichCourseResponse(course);
    }

    @Override
    public Page<CourseResponse> getAllCourses(String search, Pageable pageable) {
        Page<Course> courses;
        if (search != null && !search.isBlank()) {
            courses = courseRepository.findBySearch(search, pageable);
        } else {
            courses = courseRepository.findAll(pageable);
        }
        return courses.map(this::enrichCourseResponse);
    }

    @Override
    public Page<CourseResponse> getCoursesByLecturer(Long lecturerId, Pageable pageable) {
        return courseRepository.findByLecturerId(lecturerId, pageable).map(this::enrichCourseResponse);
    }

    @Override
    public Page<CourseResponse> getEnrolledCoursesByStudent(Long studentId, Pageable pageable) {
        return courseRepository.findEnrolledCoursesByStudentId(studentId, pageable).map(this::enrichCourseResponse);
    }

    @Override
    public Page<CourseResponse> getAvailableCourses(Long studentId, Pageable pageable) {
        List<Long> enrolledIds = courseEnrollmentRepository.findByStudentId(studentId)
                .stream().map(e -> e.getCourse().getId()).toList();
        Page<Course> allCourses = courseRepository.findAll(pageable);
        List<CourseResponse> available = allCourses.stream()
                .filter(c -> !enrolledIds.contains(c.getId()) && "ACTIVE".equals(c.getStatus()))
                .map(this::enrichCourseResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(available, pageable, available.size());
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
        return ApiResponse.success("Course deleted successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> assignLecturer(Long courseId, Long lecturerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
        if (lecturer.getRole() != Role.LECTURER) {
            throw new BadRequestException("User is not a lecturer");
        }
        if (courseLecturerRepository.existsByCourseIdAndLecturerId(courseId, lecturerId)) {
            throw new ConflictException("Lecturer already assigned to this course");
        }
        CourseLecturer cl = CourseLecturer.builder().course(course).lecturer(lecturer).build();
        courseLecturerRepository.save(cl);
        notificationService.createNotification(lecturerId,
                "Course Assignment",
                "You have been assigned to course: " + course.getCourseCode() + " - " + course.getCourseName());
        return ApiResponse.success("Lecturer assigned successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> removeLecturer(Long courseId, Long lecturerId) {
        if (!courseLecturerRepository.existsByCourseIdAndLecturerId(courseId, lecturerId)) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        courseLecturerRepository.deleteByCourseIdAndLecturerId(courseId, lecturerId);
        return ApiResponse.success("Lecturer removed from course successfully", null);
    }

    @Override
    public List<UserResponse> getLecturersByCourse(Long courseId) {
        return courseLecturerRepository.findByCourseId(courseId).stream()
                .map(cl -> UserResponse.from(cl.getLecturer()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getStudentsByCourse(Long courseId) {
        return courseEnrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> UserResponse.from(e.getStudent()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<?> enrollStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("User is not a student");
        }
        if (courseEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ConflictException("Student already enrolled in this course");
        }
        CourseEnrollment enrollment = CourseEnrollment.builder().course(course).student(student).build();
        courseEnrollmentRepository.save(enrollment);
        return ApiResponse.success("Enrolled in course successfully", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> unenrollStudent(Long courseId, Long studentId) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        courseEnrollmentRepository.delete(enrollment);
        return ApiResponse.success("Unenrolled from course successfully", null);
    }

    private CourseResponse enrichCourseResponse(Course course) {
        CourseResponse response = CourseResponse.from(course);
        List<UserResponse> lecturers = courseLecturerRepository.findByCourseId(course.getId()).stream()
                .map(cl -> UserResponse.from(cl.getLecturer()))
                .collect(Collectors.toList());
        response.setLecturers(lecturers);
        response.setEnrollmentCount(courseEnrollmentRepository.countByCourseId(course.getId()));
        return response;
    }

    @Override
    @Transactional
    public CsvImportCourseResponse importCoursesFromCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        List<CourseResponse> imported = new ArrayList<>();
        int total = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                throw new BadRequestException("CSV file is empty");
            }

            // Skip header row
            rows = rows.subList(1, rows.size());
            total = rows.size();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int lineNum = i + 2;

                try {
                    if (row.length < 3) {
                        errors.add("Line " + lineNum + ": Insufficient columns. Expected 3, got " + row.length);
                        continue;
                    }

                    String course_code = row[0].trim();
                    String course_name = row[1].trim();
                    String status = row[2].trim();

                    if (course_name.isEmpty() || course_code.isEmpty() || status.isEmpty()) {
                        errors.add("Line " + lineNum + ": Required fields are empty");
                        continue;
                    }

                    if (courseRepository.existsByCourseCode(course_code)) {
                        errors.add("Line " + lineNum + ": Course already exists: " + course_code);
                        continue;
                    }
                    Course course = Course.builder()
                            .courseCode(course_code)
                            .courseName(course_name)
                            .status(status)
                            .build();

                    imported.add(CourseResponse.from(courseRepository.save(course)));
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException | CsvException e) {
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }

        return CsvImportCourseResponse.builder()
                .totalRecords(total)
                .successCount(imported.size())
                .failedCount(errors.size())
                .errors(errors)
                .importedCourses(imported)
                .build();
    }

}
