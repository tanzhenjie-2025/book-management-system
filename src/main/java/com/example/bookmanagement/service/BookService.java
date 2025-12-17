package com.example.bookmanagement.service;

import com.example.bookmanagement.model.Book;
import com.example.bookmanagement.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    // 获取所有书籍
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // 按ID获取书籍
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("书籍不存在"));
    }

    // 添加书籍
    public Book addBook(Book book) {
        // 检查书籍是否已存在（名称+作者）
        if (bookRepository.findByNameAndAuthor(book.getName(), book.getAuthor()).isPresent()) {
            throw new RuntimeException("该书籍已存在（名称+作者重复）");
        }
        return bookRepository.save(book);
    }

    // 更新书籍
    public Book updateBook(Book book) {
        if (!bookRepository.existsById(book.getId())) {
            throw new RuntimeException("书籍不存在");
        }
        return bookRepository.save(book);
    }

    // 删除书籍
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // 增加借阅次数
    public void increaseBorrowCount(Long bookId) {
        Book book = getBookById(bookId);
        book.setBorrowCount(book.getBorrowCount() + 1);
        bookRepository.save(book);
    }

    // 减少库存
    public void decreaseStock(Long bookId) {
        Book book = getBookById(bookId);
        if (book.getStock() <= 0) {
            throw new RuntimeException("书籍库存不足");
        }
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
    }

    // 增加库存
    public void increaseStock(Long bookId) {
        Book book = getBookById(bookId);
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);
    }

    // ========== 关键修复：确保该方法是public且参数匹配 ==========
    /**
     * 更新书籍的评分统计（平均评分+评价数量）
     */
    @Transactional
    public void updateBookScores(Long bookId, Double avgScore, Long commentCount) {
        // 先校验书籍存在性
        getBookById(bookId);
        // 调用Repository的更新方法
        bookRepository.updateBookScores(bookId, avgScore, commentCount);
    }
}