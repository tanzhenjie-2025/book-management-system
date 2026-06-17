package com.example.bookmanagement.controller;

import com.example.bookmanagement.annotation.LogOperation;
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

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

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
    @LogOperation(value = "ADD_BOOK", description = "添加新书籍")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.addBook(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "UPDATE_BOOK", description = "编辑书籍信息")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        return ResponseEntity.ok(bookService.updateBook(book));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "UPDATE_STOCK", description = "修改库存")
    public ResponseEntity<String> updateStock(@PathVariable Long id, @RequestParam int stock) {
        bookService.updateStock(id, stock);
        return ResponseEntity.ok("库存更新成功");
    }

    @PutMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "SOFT_DELETE", description = "下架书籍")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        bookService.softDeleteBook(id);
        return ResponseEntity.ok("下架成功");
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "RESTORE", description = "上架书籍")
    public ResponseEntity<String> restore(@PathVariable Long id) {
        bookService.restoreBook(id);
        return ResponseEntity.ok("上架成功");
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "EXPORT", description = "导出图书")
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

    @PostMapping("/import/overwrite")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "IMPORT_OVERWRITE", description = "覆盖导入书籍")
    public ResponseEntity<String> importOverwrite(@RequestParam("file") MultipartFile file) {
        try {
            int count = bookService.importBooksOverwrite(file);
            return ResponseEntity.ok("覆盖导入成功，共处理 " + count + " 条记录");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/import/append")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "IMPORT_APPEND", description = "添加导入书籍")
    public ResponseEntity<String> importAppend(@RequestParam("file") MultipartFile file) {
        try {
            int count = bookService.importBooksAppend(file);
            return ResponseEntity.ok("添加导入成功，共处理 " + count + " 条记录");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }
}