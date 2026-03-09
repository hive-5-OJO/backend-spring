package org.backend.domain.advice.repository;

import org.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdviceRepository extends JpaRepository<Advice, Long>, AdviceRepositoryCustom {
}
