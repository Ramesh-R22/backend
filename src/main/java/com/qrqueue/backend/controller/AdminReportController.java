package com.qrqueue.backend.controller;

import com.qrqueue.backend.model.QueueEntry;
import com.qrqueue.backend.repository.QueueEntryRepository;
import com.qrqueue.backend.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public List<QueueEntry> filterByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return queueEntryRepository.findAll().stream()
                .filter(q -> !q.getJoinedAt().toLocalDate().isBefore(startDate)
                && !q.getJoinedAt().toLocalDate().isAfter(endDate))
                .toList();
    }

    @GetMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public void downloadExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<QueueEntry> filtered = queueEntryRepository.findAll().stream()
                .filter(q -> !q.getJoinedAt().toLocalDate().isBefore(startDate)
                && !q.getJoinedAt().toLocalDate().isAfter(endDate))
                .toList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerValue = "attachment; filename=queue_report.xlsx";
        response.setHeader("Content-Disposition", headerValue);

        excelExportService.export(filtered, response);
    }
}
