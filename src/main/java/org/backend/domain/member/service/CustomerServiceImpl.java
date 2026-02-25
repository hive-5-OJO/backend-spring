package org.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.common.exception.CustomException;
import org.backend.common.exception.ErrorCode;
import org.backend.common.page.PageResponse;
import org.backend.domain.analysis.repository.FeatureConsultationRepository;
import org.backend.domain.member.dto.ConsultStatProjection;
import org.backend.domain.member.dto.CustomerDetailResponse;
import org.backend.domain.member.dto.CustomerSummaryProjection;
import org.backend.domain.member.dto.CustomerSummaryResponse;
import org.backend.domain.member.entity.Member;
import org.backend.domain.analysis.repository.CustomerSummaryRepository;
import org.backend.domain.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final MemberRepository memberRepository;
    private final CustomerSummaryRepository summaryRepository;
    private final FeatureConsultationRepository featureRepository;

    // 고객별 기본 정보 조회
    @Override
    public CommonResponse<CustomerDetailResponse> getCustomerDetail(Long memberId) {

        Member member = memberRepository.findWithConsentById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        CustomerDetailResponse response = CustomerDetailResponse.from(member);

        return CommonResponse.success(response, "고객 기본 정보 조회");
    }

    // 고객 전체 리스트
    @Override
    public CommonResponse<PageResponse<CustomerSummaryResponse>> getCustomerList(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // 통계 가져오기
        ConsultStatProjection stats = featureRepository.getConsultStats();

        double avg = stats != null && stats.getAvgValue() != null ? stats.getAvgValue() : 0;

        double std = stats != null && stats.getStdValue() != null ? stats.getStdValue() : 0;

        // 고객 요약 조회
        Page<CustomerSummaryProjection> result = summaryRepository.findCustomerSummary(pageable);

        // 매핑
        Page<CustomerSummaryResponse> mapped = result.map(p ->
                new CustomerSummaryResponse(
                        p.getMemberId(),
                        p.getName(),
                        p.getProductName(),
                        p.getCreatedAt().toLocalDate() + " ~ 현재",
                        p.getTopConsultCategory(),
                        calculateFrequency(p.getLast30dConsultCount(), avg, std),
                        convertVipType(p.getVipType())
                )
        );

        return CommonResponse.success(
                PageResponse.from(mapped),
                "고객 목록 조회"
        );
    }

    // Z-score 기반 상담 빈도 계산 (통걔 로직)
    private String calculateFrequency(Double count, double avg, double std) {

        if (count == null || std == 0) {
            return "LOW";
        }

        double z = (count - avg) / std;

        if (z >= 1) return "HIGH";
        if (z <= -1) return "LOW";

        return "MEDIUM";
    }

    // VIP 타입 변환 (UI 로직)
    private String convertVipType(String type) {

        if (type == null) return "일반 고객";

        return switch (type) {
            case "VIP" -> "VIP";
            case "LOYAL" -> "잠재 VIP";
            case "COMMON" -> "일반 고객";
            case "RISK", "SLEEP" -> "이탈 우려 고객";
            case "LOST" -> "이탈 고객";
            default -> "일반 고객";
        };
    }
}