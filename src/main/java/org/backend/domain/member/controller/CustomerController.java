package org.backend.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.member.dto.CustomerDetailResponse;
import org.backend.domain.member.service.CustomerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerServiceImpl customerService;

    // 고객별 기본 정보 조회
    // 로그인 구현 후 수정예정
    @GetMapping("/{memberId}")
    public ResponseEntity<CommonResponse<CustomerDetailResponse>> getCustomerDetail(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                customerService.getCustomerDetail(memberId)
        );
    }
}