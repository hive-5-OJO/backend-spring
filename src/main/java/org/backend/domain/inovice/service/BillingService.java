package org.backend.domain.inovice.service;

import org.backend.common.CommonResponse;
import org.backend.domain.inovice.dto.MonthlyBillingResponse;

public interface BillingService {

    CommonResponse<MonthlyBillingResponse> getMonthlyBilling(String baseMonth, Long memberId);
}