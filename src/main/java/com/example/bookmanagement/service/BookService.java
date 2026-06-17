package com.example.bookmanagement.service;

import com.example.bookmanagement.model.Book;
import com.example.bookmanagement.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("图书信息");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "书名", "作者", "分类", "库存", "借阅次数", "出版社", "平均评分", "评论数", "描述"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

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

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    // ================= 导入相关方法 =================

    /**
     * 解析上传的Excel文件，返回图书列表（仅包含基础信息）
     */
    private List<Book> parseExcel(MultipartFile file) throws Exception {
        List<Book> books = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) { // 跳过表头
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                Book book = new Book();
                // ID
                Cell idCell = row.getCell(0);
                if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                    book.setId((long) idCell.getNumericCellValue());
                } else if (idCell != null && idCell.getCellType() == CellType.STRING) {
                    try {
                        book.setId(Long.parseLong(idCell.getStringCellValue()));
                    } catch (NumberFormatException ignored) {}
                }

                // 书名
                book.setName(getCellString(row, 1));
                // 作者
                book.setAuthor(getCellString(row, 2));
                // 分类
                book.setCategory(getCellString(row, 3));
                // 库存
                Cell stockCell = row.getCell(4);
                if (stockCell != null && stockCell.getCellType() == CellType.NUMERIC) {
                    book.setStock((int) stockCell.getNumericCellValue());
                }
                // 借阅次数、平均评分、评论数不读取（导入时忽略）
                // 出版社
                book.setPublish(getCellString(row, 6));
                // 描述
                book.setDescription(getCellString(row, 9));

                // 至少要有书名和作者，否则跳过空行
                if (book.getName() != null && !book.getName().isEmpty() &&
                        book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                    books.add(book);
                }
            }
        }
        return books;
    }

    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING); // 强制转为字符串
        return cell.getStringCellValue().trim();
    }

    /**
     * 覆盖导入：根据ID或书名+作者匹配，更新基础信息，不影响其他书籍
     */
    @Transactional
    public int importBooksOverwrite(MultipartFile file) throws Exception {
        List<Book> importBooks = parseExcel(file);
        int count = 0;
        for (Book importBook : importBooks) {
            // 优先根据ID查找
            Optional<Book> existing = Optional.empty();
            if (importBook.getId() != null) {
                existing = bookRepository.findById(importBook.getId());
            }
            // 如果ID未匹配，根据书名+作者查找
            if (existing.isEmpty()) {
                existing = bookRepository.findByNameAndAuthor(importBook.getName(), importBook.getAuthor());
            }

            Book book;
            if (existing.isPresent()) {
                book = existing.get();
                // 覆盖基础字段（保留ID、借阅次数、评分、评论数）
                book.setName(importBook.getName());
                book.setAuthor(importBook.getAuthor());
                book.setCategory(importBook.getCategory());
                book.setStock(importBook.getStock());
                book.setPublish(importBook.getPublish());
                book.setDescription(importBook.getDescription());
            } else {
                book = importBook;
                book.setId(null); // 新增时ID自增
                book.setBorrowCount(0);
                book.setAvgScore(0.0);
                book.setCommentCount(0);
            }
            bookRepository.save(book);
            count++;
        }
        return count;
    }

    /**
     * 添加导入：已存在的书籍只累加库存，不存在则新增
     */
    @Transactional
    public int importBooksAppend(MultipartFile file) throws Exception {
        List<Book> importBooks = parseExcel(file);
        int count = 0;
        for (Book importBook : importBooks) {
            Optional<Book> existing = Optional.empty();
            if (importBook.getId() != null) {
                existing = bookRepository.findById(importBook.getId());
            }
            if (existing.isEmpty()) {
                existing = bookRepository.findByNameAndAuthor(importBook.getName(), importBook.getAuthor());
            }

            if (existing.isPresent()) {
                Book book = existing.get();
                // 只累加库存，其他字段保持不变
                book.setStock(book.getStock() + importBook.getStock());
                bookRepository.save(book);
            } else {
                importBook.setId(null);
                importBook.setBorrowCount(0);
                importBook.setAvgScore(0.0);
                importBook.setCommentCount(0);
                bookRepository.save(importBook);
            }
            count++;
        }
        return count;
    }
}