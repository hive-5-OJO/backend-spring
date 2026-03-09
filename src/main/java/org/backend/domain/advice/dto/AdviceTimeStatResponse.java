package org.backend.domain.advice.dto;

public interface AdviceTimeStatResponse {
    Integer getHour(); // 시간 (0~23)
    Long getCount();   // 해당 시간대 상담 건수
}
