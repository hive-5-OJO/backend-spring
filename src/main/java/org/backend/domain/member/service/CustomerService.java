package org.backend.domain.member.service;

import org.backend.common.CommonResponse;
import org.backend.domain.member.dto.CustomerDetailResponse;

public interface CustomerService {

    // 고객별 기본 정보 조회
    CommonResponse<CustomerDetailResponse> getCustomerDetail(Long memberId);
}
