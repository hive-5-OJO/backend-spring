package org.backend.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.backend.domain.admin.dto.AdminSummaryDto;
import org.backend.domain.admin.entity.AdminStatus;
import org.backend.domain.admin.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public Page<AdminSummaryDto> getAdmins(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AdminStatus status
    ) {
        return adminService.getAdmins(page, size, keyword, status);
    }
}