package org.backend.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
public class CustomerFilterResponse {

    private Long memberId;
    private String name;
    private String serviceName;
    private String mainCategoryName;
    private Integer usageMonths;
    private String frequency;
    private String isVip;

    public CustomerFilterResponse(Long memberId, String name, String serviceName, String mainCategoryName,
            LocalDateTime createdAt, Integer frequency, String isVip) {
        this.memberId = memberId;
        this.name = name;
        this.serviceName = serviceName;
        this.mainCategoryName = mainCategoryName;
        this.usageMonths = createdAt != null ? (int) ChronoUnit.MONTHS.between(createdAt, LocalDateTime.now()) : 0;
        this.frequency = frequency != null ? frequency.toString() : null;
        this.isVip = isVip;
    }
}
