package org.backend.domain.inovice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.backend.domain.inovice.entity.InvoiceDetail;

@Getter
@AllArgsConstructor
public class ServiceItemResponse {

    private String productName;
    private String productType;
    private Long amount;

    public static ServiceItemResponse from(InvoiceDetail detail) {
        return new ServiceItemResponse(
                detail.getProductNameSnapshot(),
                detail.getProductType(),
                detail.getTotalPrice()
        );
    }
}
