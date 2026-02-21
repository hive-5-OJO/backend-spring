package org.backend.domain.advice.repository;

import java.util.List;
import org.backend.domain.advice.document.AdviceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AdviceSearchRepository extends ElasticsearchRepository<AdviceDocument, Long> {
    List<AdviceDocument> findByAdviceContentContaining(String keyword);

}
