package com.stms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CsvImportResponse {
    private int totalRecords;
    private int successCount;
    private int failedCount;
    private List<String> errors;
    private List<UserResponse> importedUsers;
}
