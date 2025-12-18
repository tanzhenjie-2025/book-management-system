// filePath: book-management-system/src/main/java/com/example/bookmanagement/model/BorrowRecord.java
package com.example.bookmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long bookId;

    private LocalDate borrowTime;

    private LocalDate returnTime;

    private boolean isReturned = false;
}