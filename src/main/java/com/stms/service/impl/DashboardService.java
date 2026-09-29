package com.stms.service.impl;

import com.stms.dto.response.DashboardStatsResponse;
import com.stms.entity.enums.Role;
import com.stms.repository.*;
import com.stms.service.NotificationService;
import com.stms.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final NotificationService notificationService;

    public DashboardStatsResponse getAdminStats() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalLecturers = userRepository.countByRole(Role.LECTURER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalCourses = courseRepository.count();
        long totalTasks = taskRepository.count();

        List<Object[]> rawTasksPerCourse = taskRepository.countTasksPerCourse();
        List<Map<String, Object>> tasksPerCourse = rawTasksPerCourse.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            Long courseId = (Long) row[0];
            courseRepository.findById(courseId).ifPresent(c -> {
                m.put("courseId", courseId);
                m.put("courseCode", c.getCourseCode());
                m.put("courseName", c.getCourseName());
                m.put("taskCount", row[1]);
            });
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> enrollmentsPerCourse = courseRepository.findAll().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("courseId", c.getId());
            m.put("courseCode", c.getCourseCode());
            m.put("courseName", c.getCourseName());
            m.put("enrollmentCount", enrollmentRepository.countByCourseId(c.getId()));
            return m;
        }).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalUsers(totalStudents + totalLecturers + totalAdmins)
                .totalStudents(totalStudents)
                .totalLecturers(totalLecturers)
                .totalAdmins(totalAdmins)
                .totalCourses(totalCourses)
                .totalTasks(totalTasks)
                .tasksPerCourse(tasksPerCourse)
                .enrollmentsPerCourse(enrollmentsPerCourse)
                .build();
    }

    public DashboardStatsResponse getLecturerStats(Long lecturerId) {
        long assignedCourses = courseLecturerRepository.countByLecturerId(lecturerId);
        long activeTasks = taskRepository.findTasksByLecturerId(lecturerId,
                org.springframework.data.domain.Pageable.unpaged()).stream()
                .filter(t -> t.getDeadline().isAfter(LocalDateTime.now())).count();
        long upcoming = taskRepository.findUpcomingTasksForLecturer(lecturerId, LocalDateTime.now()).size();
        long unread = notificationService.countUnread(lecturerId);

        return DashboardStatsResponse.builder()
                .assignedCourses(assignedCourses)
                .activeTasks(activeTasks)
                .upcomingDeadlines(upcoming)
                .unreadNotifications(unread)
                .build();
    }

    public DashboardStatsResponse getStudentStats(Long studentId) {
        long enrolledCourses = enrollmentRepository.countByStudentId(studentId);
        long activeTasks = taskRepository.findTasksByEnrolledStudent(studentId,
                org.springframework.data.domain.Pageable.unpaged()).stream()
                .filter(t -> t.getDeadline().isAfter(LocalDateTime.now())).count();
        long upcoming = taskRepository.findUpcomingTasksForStudent(studentId, LocalDateTime.now()).size();
        long unread = notificationService.countUnread(studentId);

        return DashboardStatsResponse.builder()
                .enrolledCourses(enrolledCourses)
                .activeTasks(activeTasks)
                .upcomingDeadlines(upcoming)
                .unreadNotifications(unread)
                .build();
    }
}
