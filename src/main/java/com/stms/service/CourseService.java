package com.stms.service;

import com.stms.dto.request.CourseRequest;
import com.stms.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CourseRequest request);
    CourseResponse updateCourse(Long id, CourseRequest request);
    CourseResponse getCourseById(Long id);
    Page<CourseResponse> getAllCourses(String search, Pageable pageable);
    Page<CourseResponse> getCoursesByLecturer(Long lecturerId, Pageable pageable);
    Page<CourseResponse> getEnrolledCoursesByStudent(Long studentId, Pageable pageable);
    ApiResponse<?> deleteCourse(Long id);
    ApiResponse<?> assignLecturer(Long courseId, Long lecturerId);
    ApiResponse<?> removeLecturer(Long courseId, Long lecturerId);
    List<UserResponse> getLecturersByCourse(Long courseId);
    List<UserResponse> getStudentsByCourse(Long courseId);
    ApiResponse<?> enrollStudent(Long courseId, Long studentId);
    ApiResponse<?> unenrollStudent(Long courseId, Long studentId);
    Page<CourseResponse> getAvailableCourses(Long studentId, Pageable pageable);
    CsvImportCourseResponse importCoursesFromCsv(MultipartFile file);
}
