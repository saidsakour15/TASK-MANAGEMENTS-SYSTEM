package com.stms.service;

import com.stms.dto.request.TaskRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request, Long lecturerId);
    TaskResponse updateTask(Long id, TaskRequest request, Long lecturerId);
    TaskResponse getTaskById(Long id);
    Page<TaskResponse> getTasksByCourse(Long courseId, Pageable pageable);
    Page<TaskResponse> getTasksByLecturer(Long lecturerId, Pageable pageable);
    Page<TaskResponse> getTasksForStudent(Long studentId, Pageable pageable);
    ApiResponse<?> deleteTask(Long id, Long lecturerId);
    List<TaskResponse> getUpcomingTasksForStudent(Long studentId);
    List<TaskResponse> getUpcomingTasksForLecturer(Long lecturerId);
    long countActiveTasks(Long courseId);
    Page<TaskResponse> searchTasks(String search, Pageable pageable);
}
