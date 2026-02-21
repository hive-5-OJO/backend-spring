package org.backend.domain.analysis.service;

import lombok.RequiredArgsConstructor;
import org.backend.common.exception.CustomException;
import org.backend.common.exception.ErrorCode;
import org.backend.domain.analysis.dto.RfmKpiResponseDto;
import org.backend.domain.analysis.entity.RfmKpi;
import org.backend.domain.analysis.repository.RfmKpiRepository;
import org.springframework.stereotype.Service;

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

    private String evaluateStatus(String kpi, Float value){
        if(value== null) return "UNKNOWN";

        return switch (kpi){
            case "CRR" -> value >= 0.8 ? "EXCELLENT" : (value >= 0.6 ? "STABLE" : "WARNING");
            case "CHURN" -> value <= 0.1 ? "SAFE" : (value <= 0.2 ? "CAUTION" : "DANGER");
            case "NRR" -> value >= 1.1 ? "GROWING" : (value >= 1.0 ? "STAGNANT" : "DECLINING");
            default -> "NORMAL";
        };
    }
}
