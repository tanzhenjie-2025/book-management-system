package com.example.bookmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String author;

    private String category;

    private int stock;

    private String description;

    private int borrowCount = 0;

    private String publish;
}