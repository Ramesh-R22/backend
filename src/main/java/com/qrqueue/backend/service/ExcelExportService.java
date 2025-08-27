package com.qrqueue.backend.service;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.qrqueue.backend.model.QueueEntry;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelExportService {

    public void export(List<QueueEntry> queueEntries, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Queue Report");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Queue ID");
            headerRow.createCell(1).setCellValue("Counter Name");
            headerRow.createCell(2).setCellValue("Joined At");
            headerRow.createCell(3).setCellValue("Served");

            int rowIdx = 1;
            for (QueueEntry entry : queueEntries) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getId());
                row.createCell(1).setCellValue(entry.getCounter().getName());
                row.createCell(2).setCellValue(entry.getJoinedAt().toString());
                row.createCell(3).setCellValue(entry.isServed() ? "Yes" : "No");
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        }
    }
}
