package com.stms.dto.response;

import com.stms.entity.Task;
import com.stms.entity.enums.AssignmentType;
import com.stms.entity.enums.SubmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private AssignmentType assignmentType;
    private SubmissionType submissionType;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long commentCount;
    private boolean isActive;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .assignmentType(task.getAssignmentType())
                .submissionType(task.getSubmissionType())
                .courseId(task.getCourse().getId())
                .courseCode(task.getCourse().getCourseCode())
                .courseName(task.getCourse().getCourseName())
                .lecturerId(task.getLecturer().getId())
                .lecturerName(task.getLecturer().getFullName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isActive(task.getDeadline().isAfter(LocalDateTime.now()))
                .build();
    }
}
