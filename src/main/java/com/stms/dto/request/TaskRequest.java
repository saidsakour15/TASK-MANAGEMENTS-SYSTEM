package com.stms.dto.request;

import com.stms.entity.enums.AssignmentType;
import com.stms.entity.enums.SubmissionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;

    @NotNull(message = "Submission type is required")
    private SubmissionType submissionType;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}
