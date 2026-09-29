package com.stms.service.impl;

import com.stms.dto.request.TaskRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.TaskResponse;
import com.stms.entity.Course;
import com.stms.entity.Task;
import com.stms.entity.User;
import com.stms.exception.BadRequestException;
import com.stms.exception.ResourceNotFoundException;
import com.stms.repository.*;
import com.stms.service.NotificationService;
import com.stms.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request, Long lecturerId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        // Verify lecturer is assigned to the course
        if (!courseLecturerRepository.existsByCourseIdAndLecturerId(request.getCourseId(), lecturerId)) {
            throw new BadRequestException("You are not assigned to this course");
        }
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .assignmentType(request.getAssignmentType())
                .submissionType(request.getSubmissionType())
                .course(course)
                .lecturer(lecturer)
                .build();
        Task saved = taskRepository.save(task);

        // Notify enrolled students
        List<Long> studentIds = courseEnrollmentRepository.findByCourseId(course.getId())
                .stream().map(e -> e.getStudent().getId()).collect(Collectors.toList());
        for (Long studentId : studentIds) {
            notificationService.createNotification(studentId,
                    "New Task: " + saved.getTitle(),
                    "A new task has been added to " + course.getCourseCode() + ": " + saved.getTitle()
                    + ". Deadline: " + saved.getDeadline());
        }
        log.info("Task created: {} for course {}", saved.getTitle(), course.getCourseCode());
        return buildTaskResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, Long lecturerId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getLecturer().getId().equals(lecturerId)) {
            throw new BadRequestException("You can only edit your own tasks");
        }
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (!courseLecturerRepository.existsByCourseIdAndLecturerId(request.getCourseId(), lecturerId)) {
            throw new BadRequestException("You are not assigned to this course");
        }
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setAssignmentType(request.getAssignmentType());
        task.setSubmissionType(request.getSubmissionType());
        task.setCourse(course);
        return buildTaskResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return buildTaskResponse(task);
    }

    @Override
    public Page<TaskResponse> getTasksByCourse(Long courseId, Pageable pageable) {
        return taskRepository.findByCourseId(courseId, pageable).map(this::buildTaskResponse);
    }

    @Override
    public Page<TaskResponse> getTasksByLecturer(Long lecturerId, Pageable pageable) {
        return taskRepository.findTasksByLecturerId(lecturerId, pageable).map(this::buildTaskResponse);
    }

    @Override
    public Page<TaskResponse> getTasksForStudent(Long studentId, Pageable pageable) {
        return taskRepository.findTasksByEnrolledStudent(studentId, pageable).map(this::buildTaskResponse);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteTask(Long id, Long lecturerId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getLecturer().getId().equals(lecturerId)) {
            throw new BadRequestException("You can only delete your own tasks");
        }
        taskRepository.deleteById(id);
        return ApiResponse.success("Task deleted successfully", null);
    }

    @Override
    public List<TaskResponse> getUpcomingTasksForStudent(Long studentId) {
        return taskRepository.findUpcomingTasksForStudent(studentId, LocalDateTime.now())
                .stream().map(this::buildTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getUpcomingTasksForLecturer(Long lecturerId) {
        return taskRepository.findUpcomingTasksForLecturer(lecturerId, LocalDateTime.now())
                .stream().map(this::buildTaskResponse).collect(Collectors.toList());
    }

    @Override
    public long countActiveTasks(Long courseId) {
        return taskRepository.findByCourseId(courseId, Pageable.unpaged()).stream()
                .filter(t -> t.getDeadline().isAfter(LocalDateTime.now())).count();
    }

    @Override
    public Page<TaskResponse> searchTasks(String search, Pageable pageable) {
        return taskRepository.findBySearch(search, pageable).map(this::buildTaskResponse);
    }

    private TaskResponse buildTaskResponse(Task task) {
        TaskResponse resp = TaskResponse.from(task);
        resp.setCommentCount(task.getComments() != null ? task.getComments().size() : 0L);
        return resp;
    }
}
