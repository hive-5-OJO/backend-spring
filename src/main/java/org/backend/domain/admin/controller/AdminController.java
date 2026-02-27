package org.backend.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.admin.dto.AdminSummaryDto;
import org.backend.domain.admin.entity.AdminStatus;
import org.backend.domain.admin.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<CommonResponse<Page<AdminSummaryDto>>> getAdmins(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AdminStatus status
    ) {
        Page<AdminSummaryDto> result = adminService.getAdmins(page, size, keyword, status);
        return ResponseEntity.ok(
                CommonResponse.success(result, "관리자 목록 조회 성공")
        );
    }
}