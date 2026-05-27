package com.arnor4eck.springkod.util.request.markup;

import java.util.List;

public record UpdateMarkupLineRequest(List<FileToUpdate> filesToUpdate) {
}
