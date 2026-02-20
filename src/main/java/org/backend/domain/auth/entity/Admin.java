package org.backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "admin",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_email", columnNames = "email")
        }
)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    // 일반 로그인 계정은 password 필요, 구글 로그인 계정은 null 허용
    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 15)
    private String phone;

    // google 로그인 계정 여부
    @Column(nullable = false)
    private Boolean google;

    @Column(length = 20)
    private String role;

    // 재직/퇴사 같은 상태값
    @Column(nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Admin(String name, String email, String password, String phone, Boolean google, String role, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.google = google;
        this.role = role;
        this.status = status;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeRole(String role) {
        this.role = role;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
