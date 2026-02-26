package org.backend.domain.admin.repository;

import org.backend.domain.admin.entity.Admin;
import org.backend.domain.admin.entity.AdminStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Admin> findByStatus(AdminStatus status, Pageable pageable);

    Page<Admin> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
            String emailKeyword, String nameKeyword, Pageable pageable);

    Page<Admin> findByStatusAndEmailContainingIgnoreCaseOrStatusAndNameContainingIgnoreCase(
            AdminStatus status1, String emailKeyword, AdminStatus status2, String nameKeyword, Pageable pageable);
}
