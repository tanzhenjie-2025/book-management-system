package com.example.bookmanagement.service;

import com.example.bookmanagement.model.Book;
import com.example.bookmanagement.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("书籍不存在"));
    }

    public Book addBook(Book book) {
        if (bookRepository.findByNameAndAuthor(book.getName(), book.getAuthor()).isPresent()) {
            throw new RuntimeException("该书籍已存在（名称+作者重复）");
        }
        return bookRepository.save(book);
    }

    public Book updateBook(Book book) {
        if (!bookRepository.existsById(book.getId())) {
            throw new RuntimeException("书籍不存在");
        }
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public void increaseBorrowCount(Long bookId) {
        Book book = getBookById(bookId);
        book.setBorrowCount(book.getBorrowCount() + 1);
        bookRepository.save(book);
    }

    public void decreaseStock(Long bookId) {
        Book book = getBookById(bookId);
        if (book.getStock() <= 0) {
            throw new RuntimeException("书籍库存不足");
        }
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
    }

    public void increaseStock(Long bookId) {
        Book book = getBookById(bookId);
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);
    }

    @Transactional
    public void updateBookScores(Long bookId, Double avgScore, Long commentCount) {
        getBookById(bookId);
        bookRepository.updateBookScores(bookId, avgScore, commentCount);
    }

    // ================= 导出 Excel 方法 =================
    public byte[] exportBooksToExcel() throws Exception {
        List<Book> books = bookRepository.findAll();
//        log.info("导出图书，共 {} 条记录", books.size());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("图书信息");

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 表头行
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "书名", "作者", "分类", "库存", "借阅次数", "出版社", "平均评分", "评论数", "描述"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        int rowNum = 1;
        for (Book book : books) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(book.getId());
            row.createCell(1).setCellValue(book.getName());
            row.createCell(2).setCellValue(book.getAuthor());
            row.createCell(3).setCellValue(book.getCategory());
            row.createCell(4).setCellValue(book.getStock());
            row.createCell(5).setCellValue(book.getBorrowCount());
            row.createCell(6).setCellValue(book.getPublish());
            row.createCell(7).setCellValue(book.getAvgScore() != null ? book.getAvgScore() : 0);
            row.createCell(8).setCellValue(book.getCommentCount() != null ? book.getCommentCount() : 0);
            row.createCell(9).setCellValue(book.getDescription());
        }

        // 自动调整列宽
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}