package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.MlService;
import com.arnor4eck.springkod.util.response.MlAnaliticsResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/analitics")
@AllArgsConstructor
public class AnaliticsController {

    private final MlService mlService;

    @PostMapping("/{id}")
    private MlAnaliticsResponse analize(@PathVariable("id") long id) throws IOException {
        return mlService.getMlAnalitics(id);
    }
}
