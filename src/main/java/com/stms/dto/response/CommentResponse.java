package com.stms.dto.response;

import com.stms.entity.TaskComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private Long taskId;
    private Long userId;
    private String userName;
    private String userRole;
    private String comment;
    private LocalDateTime commentDate;

    public static CommentResponse from(TaskComment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .taskId(c.getTask().getId())
                .userId(c.getUser().getId())
                .userName(c.getUser().getFullName())
                .userRole(c.getUser().getRole().name())
                .comment(c.getComment())
                .commentDate(c.getCommentDate())
                .build();
    }
}
