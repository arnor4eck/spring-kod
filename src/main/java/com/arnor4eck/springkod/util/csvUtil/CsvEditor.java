package com.arnor4eck.springkod.util.csvUtil;

import com.arnor4eck.springkod.util.exception.FileReadException;
import com.arnor4eck.springkod.util.request.markup.FileToUpdate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CsvEditor {

    public static byte[] removeRowByFirstColumnValue(byte[] csvBytes, List<String> valuesToRemove) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HashSet<String> shouldBeRemoved = new HashSet<>(valuesToRemove);

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

                    if (!shouldBeRemoved.contains(firstColumnValue)) {
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
            List<FileToUpdate> filesToUpdate) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Map<String, String> filesValues = new HashMap<>(filesToUpdate.size());

        for(FileToUpdate fileToUpdate : filesToUpdate){
            filesValues.put(fileToUpdate.fileName(), fileToUpdate.newLabel());
        }

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

                    if (filesValues.containsKey(record.get(0))) {
                        List<String> newRecord = new LinkedList<>();
                        for (int j = 0; j < record.size(); j++) {
                            if (j == 1) {
                                log.info("Строка {} обновлена: {} -> {}",
                                        record.get(0),
                                        record.get(1),
                                        filesValues.get(record.get(0)));
                                newRecord.add(filesValues.get(record.get(0)));
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
