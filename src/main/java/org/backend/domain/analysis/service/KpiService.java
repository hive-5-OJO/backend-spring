package org.backend.domain.analysis.service;

import lombok.RequiredArgsConstructor;
import org.backend.common.exception.CustomException;
import org.backend.common.exception.ErrorCode;
import org.backend.domain.analysis.dto.RfmKpiResponseDto;
import org.backend.domain.analysis.entity.RfmKpi;
import org.backend.domain.analysis.repository.RfmKpiRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class KpiService {
    private final RfmKpiRepository rfmKpiRepository;

    public RfmKpiResponseDto getKpi(String baseMonth){
        RfmKpi rfmKpi = rfmKpiRepository.findByBaseMonth(baseMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.KPI_NOT_FOUND));
        return new RfmKpiResponseDto(
                rfmKpi.getBaseMonth(),
                rfmKpi.getCrr(),
                evaluateStatus("CRR", rfmKpi.getCrr()),
                rfmKpi.getChurnRate(),
                evaluateStatus("CHURN", rfmKpi.getChurnRate()),
                rfmKpi.getNrr(),
                evaluateStatus("NRR", rfmKpi.getNrr())
        );
    }

    private String evaluateStatus(String kpi, BigDecimal value){
        if(value== null) return "UNKNOWN";

        return switch (kpi){
            case "CRR" -> value.compareTo(new BigDecimal("0.8")) >= 0 ? "EXCELLENT" :
                    (value.compareTo(new BigDecimal("0.6")) >= 0 ? "STABLE" : "WARNING");
            case "CHURN" -> value.compareTo(new BigDecimal("0.1")) <= 0 ? "SAFE" :
                    (value.compareTo(new BigDecimal("0.2")) <= 0 ? "CAUTION" : "DANGER");
            case "NRR" -> value.compareTo(new BigDecimal("1.1")) >= 0 ? "GROWING" :
                    (value.compareTo(new BigDecimal("1.0")) >= 0 ? "STAGNANT" : "DECLINING");
            default -> "NORMAL";
        };
    }
}
