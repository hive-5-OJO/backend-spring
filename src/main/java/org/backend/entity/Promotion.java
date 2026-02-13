package org.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @Column(name = "promotion_id", length = 255)
    private String id;

    @Column(name = "promotion_name", nullable = false, length = 255)
    private String promotionName;

    @Column(name = "promotion_detail", columnDefinition = "TEXT")
    private String promotionDetail;

}