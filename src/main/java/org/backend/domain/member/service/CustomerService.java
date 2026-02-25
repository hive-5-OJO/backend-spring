package org.backend.domain.member.service;

import org.backend.common.CommonResponse;
import org.backend.common.page.PageResponse;
import org.backend.domain.member.dto.CustomerDetailResponse;
import org.backend.domain.member.dto.CustomerSummaryResponse;

public interface CustomerService {

    // 고객별 기본 정보 조회
    CommonResponse<CustomerDetailResponse> getCustomerDetail(Long memberId);

    // 고객 전체 리스트
    CommonResponse<PageResponse<CustomerSummaryResponse>> getCustomerList(int page, int size);
}
