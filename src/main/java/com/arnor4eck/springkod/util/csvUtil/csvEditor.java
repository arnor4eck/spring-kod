package com.arnor4eck.springkod.util.csvUtil;

import com.arnor4eck.springkod.util.exception.FileReadException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class csvEditor {

    public static byte[] removeRowByFirstColumnValue(byte[] csvBytes, String valueToRemove) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try(Reader reader = new InputStreamReader(
                new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8)) {

            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(',');

            Iterable<CSVRecord> records = csvFormat.parse(reader);

            List<String> headers = records.iterator().next()
                    .getParser()
                    .getHeaderNames();
            String firstColumnName = headers.getFirst();

            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer,
                         CSVFormat.DEFAULT
                                 .withHeader(headers.toArray(new String[0]))
                                 .withDelimiter(','))) {

                for (CSVRecord record : records) {
                    String firstColumnValue = record.get(firstColumnName);

                    if (!firstColumnValue.equals(valueToRemove)) {
                        printer.printRecord(record);
                    }
                }

                printer.flush();
            }
        }
        catch (Exception ex){
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
                    .withFirstRecordAsHeader()
                    .withDelimiter(',');

            Iterable<CSVRecord> records = csvFormat.parse(reader);

            // Получаем заголовки
            List<String> headers = records.iterator().next()
                    .getParser()
                    .getHeaderNames();
            String firstColumnName = headers.get(0);
            String secondColumnName = headers.get(1);

            // Записываем результат
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer,
                         CSVFormat.DEFAULT
                                 .withHeader(headers.toArray(new String[0]))
                                 .withDelimiter(','))) {

                for (CSVRecord record : records) {
                    // Проверяем значение в первом столбце
                    if (record.get(firstColumnName).equals(firstColumnValue)) {
                        List<String> newRecord = new ArrayList<>();
                        for (int i = 0; i < record.size(); i++) {
                            if (i == 1) {
                                newRecord.add(newSecondColumnValue);
                            } else {
                                newRecord.add(record.get(i));
                            }
                        }
                        printer.printRecord(newRecord);
                    } else {
                        printer.printRecord(record);
                    }
                }

                printer.flush();
            }
        }
        catch (Exception exception){
            throw new FileReadException("Error reading csv");
        }

        return outputStream.toByteArray();
    }

}
