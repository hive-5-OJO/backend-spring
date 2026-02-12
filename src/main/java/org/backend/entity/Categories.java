package org.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

}
