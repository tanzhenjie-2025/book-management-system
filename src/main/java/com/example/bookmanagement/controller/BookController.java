package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.Book;
import com.example.bookmanagement.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    // 首页获取未下架书籍
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 管理员获取全部书籍（含下架）
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Book> getAllBooksForAdmin() {
        return bookService.getAllBooksForAdmin();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.addBook(book));
    }

    // 全字段更新
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        return ResponseEntity.ok(bookService.updateBook(book));
    }

    // 快捷更新库存
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStock(@PathVariable Long id, @RequestParam int stock) {
        bookService.updateStock(id, stock);
        return ResponseEntity.ok("库存更新成功");
    }

    // 下架
    @PutMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        bookService.softDeleteBook(id);
        return ResponseEntity.ok("下架成功");
    }

    // 上架
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restore(@PathVariable Long id) {
        bookService.restoreBook(id);
        return ResponseEntity.ok("上架成功");
    }

    // 导出
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportBooks() {
        System.out.println("========== 导出图书接口被调用 ==========");
        try {
            byte[] excelBytes = bookService.exportBooksToExcel();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = "图书信息_" + timestamp + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            throw new RuntimeException("导出图书信息失败：" + e.getMessage());
        }
    }

    // 覆盖导入
    @PostMapping("/import/overwrite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importOverwrite(@RequestParam("file") MultipartFile file) {
        try {
            int count = bookService.importBooksOverwrite(file);
            return ResponseEntity.ok("覆盖导入成功，共处理 " + count + " 条记录");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    // 添加导入
    @PostMapping("/import/append")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importAppend(@RequestParam("file") MultipartFile file) {
        try {
            int count = bookService.importBooksAppend(file);
            return ResponseEntity.ok("添加导入成功，共处理 " + count + " 条记录");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }
}