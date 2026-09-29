package com.stms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    // Admin stats
    private Long totalUsers;
    private Long totalStudents;
    private Long totalLecturers;
    private Long totalAdmins;
    private Long totalCourses;
    private Long totalTasks;
    private List<Map<String, Object>> tasksPerCourse;
    private List<Map<String, Object>> enrollmentsPerCourse;

    // Lecturer/Student stats
    private Long assignedCourses;
    private Long activeTasks;
    private Long upcomingDeadlines;
    private Long enrolledCourses;
    private Long unreadNotifications;
}
