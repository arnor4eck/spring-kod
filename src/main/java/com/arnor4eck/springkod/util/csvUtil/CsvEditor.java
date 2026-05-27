package com.arnor4eck.springkod.util.csvUtil;

import com.arnor4eck.springkod.util.exception.FileReadException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CsvEditor {

    public static byte[] removeRowByFirstColumnValue(byte[] csvBytes, String valueToRemove) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Reader reader = new InputStreamReader(
                new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8)) {

            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withTrim();

            List<CSVRecord> allRecords = csvFormat.parse(reader).getRecords();

            if (allRecords.isEmpty()) {
                return outputStream.toByteArray();
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < allRecords.get(0).size(); i++) {
                headers.add(allRecords.get(0).get(i));
            }

            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer,
                         CSVFormat.DEFAULT
                                 .withDelimiter(';'))) {

                printer.printRecord(headers);
                for (int i = 1; i < allRecords.size(); i++) {
                    CSVRecord record = allRecords.get(i);
                    String firstColumnValue = record.get(0);

                    if (!firstColumnValue.equals(valueToRemove)) {
                        printer.printRecord(record);
                        log.info("Строка {} оставлена", firstColumnValue);
                    } else {
                        log.info("Строка {} удалена", firstColumnValue);
                    }
                }

                printer.flush();
            }

        } catch (Exception ex) {
            log.error("Ошибка при удалении строки", ex);
            throw new FileReadException("Error reading csv");
        }

        return outputStream.toByteArray();
    }


    public static byte[] updateSecondColumnByFirstColumnValue(
            byte[] csvBytes,
            String firstColumnValue,
            String newSecondColumnValue) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Reader reader = new InputStreamReader(
                new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8)) {

            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withTrim();

            List<CSVRecord> allRecords = csvFormat.parse(reader).getRecords();

            if (allRecords.isEmpty()) {
                return outputStream.toByteArray();
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < allRecords.get(0).size(); i++) {
                headers.add(allRecords.get(0).get(i));
            }

            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer,
                         CSVFormat.DEFAULT
                                 .withDelimiter(';'))) {
                printer.printRecord(headers);

                for (int i = 1; i < allRecords.size(); i++) {
                    CSVRecord record = allRecords.get(i);

                    if (record.get(0).equals(firstColumnValue)) {
                        List<String> newRecord = new LinkedList<>();
                        for (int j = 0; j < record.size(); j++) {
                            if (j == 1) {
                                log.info("Строка {} обновлена: {} -> {}",
                                        record.get(0),
                                        record.get(1),
                                        newSecondColumnValue);
                                newRecord.add(newSecondColumnValue);
                            } else {
                                newRecord.add(record.get(j));
                            }
                        }
                        printer.printRecord(newRecord);
                    } else {
                        log.info("Строка {} оставлена без изменений", record.get(0));
                        printer.printRecord(record);
                    }
                }

                printer.flush();
            }
        } catch (Exception exception) {
            log.error("Ошибка при редактировании CSV", exception);
            throw new FileReadException("Error reading csv");
        }

        return outputStream.toByteArray();
    }
}
