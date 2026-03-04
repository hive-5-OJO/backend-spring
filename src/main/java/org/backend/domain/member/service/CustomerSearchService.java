package org.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.analysis.repository.CustomerSummaryRepository;
import org.backend.domain.analysis.repository.FeatureConsultationRepository;
import org.backend.domain.member.document.MemberSearchDocument;
import org.backend.domain.member.dto.ConsultStatProjection;
import org.backend.domain.member.dto.CustomerSummaryProjection;
import org.backend.domain.member.dto.search.CustomerSearchSummaryResponse;
import org.backend.domain.member.repository.MemberSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerSearchService {

    private final MemberSearchRepository searchRepository;
    private final CustomerSummaryRepository summaryRepository;
    private final FeatureConsultationRepository featureRepository;

    public Page<CustomerSearchSummaryResponse> search(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }

        // 1. ES에서 memberId만 조회
        Page<MemberSearchDocument> searchResult =
                searchRepository.searchByKeyword(keyword, pageable);

        List<Long> memberIds = searchResult.stream()
                .map(MemberSearchDocument::getMemberId)
                .toList();

        if (memberIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 통계 조회
        ConsultStatProjection stats = featureRepository.getConsultStats();
        double avg = stats != null && stats.getAvgValue() != null ? stats.getAvgValue() : 0;
        double std = stats != null && stats.getStdValue() != null ? stats.getStdValue() : 0;

        // 3. DB에서 요약 정보 조회
        List<CustomerSummaryProjection> summaries =
                summaryRepository.findCustomerSummaryByMemberIds(memberIds);

        // 4. 응답 매핑
        List<CustomerSearchSummaryResponse> content = summaries.stream()
                .map(p -> new CustomerSearchSummaryResponse(
                        p.getMemberId(),
                        p.getName(),
                        p.getProductName(),
                        p.getCreatedAt().toLocalDate() + " ~ 현재",
                        p.getTopConsultCategory(),
                        calculateFrequency(p.getLast30dConsultCount(), avg, std),
                        convertVipType(p.getVipType())
                ))
                .toList();

        return new PageImpl<>(content, pageable, searchResult.getTotalElements());
    }

    private String calculateFrequency(Double count, double avg, double std) {

        if (count == null || std == 0) return "LOW";

        double z = (count - avg) / std;

        if (z >= 1) return "HIGH";
        if (z <= -1) return "LOW";
        return "MEDIUM";
    }

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