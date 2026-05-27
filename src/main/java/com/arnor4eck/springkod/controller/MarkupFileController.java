package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.CsvService;
import com.arnor4eck.springkod.util.request.markup.DeleteMarkupLineRequest;
import com.arnor4eck.springkod.util.request.markup.UpdateMarkupLineRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/markup")
@AllArgsConstructor
public class MarkupFileController {

    private final CsvService csvService;

    @PatchMapping("/delete/{id}")
    public ResponseEntity<@NonNull Void> deleteLineMarkup(@PathVariable("id") long datasitoryId,
                                                          @RequestBody @Valid DeleteMarkupLineRequest request) throws IOException {
        csvService.deleteMarkupLineByValueInFirstColumn(datasitoryId, request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<@NonNull Void> updateLineMarkup(@PathVariable("id") long datasitoryId,
                                                          @RequestBody @Valid UpdateMarkupLineRequest request) throws IOException {
        csvService.updateSecondColumnByFirstColumnValue(datasitoryId, request);

        return ResponseEntity.ok().build();
    }
}
