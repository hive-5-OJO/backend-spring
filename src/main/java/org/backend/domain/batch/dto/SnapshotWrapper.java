package org.backend.domain.batch.dto;

import org.backend.domain.batch.entity.SnapshotBilling;
import org.backend.domain.analysis.entity.Analysis;
import org.backend.domain.entity.Invoice;

public record SnapshotWrapper(
    Invoice invoice,
    SnapshotBilling lastSnapshot,
    Analysis analysis
) {}
