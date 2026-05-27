package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.MlService;
import com.arnor4eck.springkod.util.response.ml.to_frontend.MlAnalyticsResponseWithUrls;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/analitics")
@AllArgsConstructor
public class AnaliticsController {

    private final MlService mlService;

    @GetMapping("/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public MlAnalyticsResponseWithUrls analize(@PathVariable("id") long datasitoryId) throws IOException {
        return mlService.getMlAnalitics(datasitoryId);
    }
}
