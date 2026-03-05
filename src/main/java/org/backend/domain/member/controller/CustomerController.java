package org.backend.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.common.page.PageResponse;
import org.backend.domain.member.dto.CustomerDetailResponse;
import org.backend.domain.member.dto.CustomerSummaryResponse;
import org.backend.domain.member.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

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

    // 고객 전체 리스트
    @GetMapping("/list")
    public ResponseEntity<CommonResponse<PageResponse<CustomerSummaryResponse>>> getCustomerList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                customerService.getCustomerList(page, size)
        );
    }
}