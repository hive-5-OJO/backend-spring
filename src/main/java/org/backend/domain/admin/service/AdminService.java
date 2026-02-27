package org.backend.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.admin.dto.AdminSummaryDto;
import org.backend.domain.admin.entity.Admin;
import org.backend.domain.admin.entity.AdminStatus;
import org.backend.domain.admin.repository.AdminRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;

    public Page<AdminSummaryDto> getAdmins(Integer page, Integer size, String keyword, AdminStatus status) {
        int p = (page == null) ? 0 : Math.max(page, 0);
        int s = (size == null) ? 20 : Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasStatus = (status != null);

        Page<Admin> result;
        if (hasKeyword && hasStatus) {
            result = adminRepository.findByStatusAndEmailContainingIgnoreCaseOrStatusAndNameContainingIgnoreCase(
                    status, keyword, status, keyword, pageable
            );
        } else if (hasKeyword) {
            result = adminRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        } else if (hasStatus) {
            result = adminRepository.findByStatus(status, pageable);
        } else {
            result = adminRepository.findAll(pageable);
        }

        return result.map(AdminSummaryDto::from);
    }
}