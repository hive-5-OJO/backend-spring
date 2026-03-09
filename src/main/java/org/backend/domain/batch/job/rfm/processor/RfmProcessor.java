package org.backend.domain.batch.job.rfm.processor;

import org.backend.domain.analysis.entity.Rfm;
import org.backend.domain.batch.entity.Monetary;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RfmProcessor implements ItemProcessor<Monetary, Rfm> {
    @Override
    public Rfm process(Monetary monetary){

        return Rfm.builder()
            .memberId(monetary.getMemberId())
            .recency(monetary.getLastPaymentDate().atStartOfDay())
            .frequency(monetary.getPaymentCount6m())
            .monetary(monetary.getTotalRevenue())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
