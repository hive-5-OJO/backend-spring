package org.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.common.exception.CustomException;
import org.backend.common.exception.ErrorCode;
import org.backend.domain.member.dto.CustomerDetailResponse;
import org.backend.domain.member.entity.Member;
import org.backend.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final MemberRepository memberRepository;

    // 고객별 기본 정보 조회
    @Override
    public CommonResponse<CustomerDetailResponse> getCustomerDetail(Long memberId) {

        Member member = memberRepository.findWithConsentById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        CustomerDetailResponse response = CustomerDetailResponse.from(member);

        return CommonResponse.success(response, "고객 기본 정보 조회");
    }
}