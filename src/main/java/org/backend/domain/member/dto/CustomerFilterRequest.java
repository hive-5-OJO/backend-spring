package org.backend.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CustomerFilterRequest {
    private List<String> segments;    // 세그먼트 다중 선택 (ex: ["VIP", "RISK"])
    private List<String> frequencies; // 상담 빈도 다중 선택 (ex: ["LOW", "HIGH"])
    private List<Long> categoryIds;   // 카테고리 다중 선택 (ex: [1, 2, 3])
}