package com.arnor4eck.springkod.util.response;

import java.util.List;

public record ExceptionResponse(int code, List<String> messages) {}
