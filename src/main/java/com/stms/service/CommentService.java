package com.stms.service;

import com.stms.dto.request.CommentRequest;
import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(Long taskId, Long userId, CommentRequest request);
    Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable);
    ApiResponse<?> deleteComment(Long commentId, Long userId);
}
