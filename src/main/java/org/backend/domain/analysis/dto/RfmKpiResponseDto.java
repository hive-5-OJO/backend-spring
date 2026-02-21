package org.backend.domain.analysis.dto;

public record RfmKpiResponseDto(
    String baseMonth,
    Float crr,
    String crrStatus,
    Float churnRate,
    String churnStatus,
    Float nrr,
    String nrrStatus
) {}
