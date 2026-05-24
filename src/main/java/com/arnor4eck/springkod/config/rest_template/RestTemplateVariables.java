package com.arnor4eck.springkod.config.rest_template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record RestTemplateVariables(@Value("${ml.url}") String mlServiceUrl) {}
