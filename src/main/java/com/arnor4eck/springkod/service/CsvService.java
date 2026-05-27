package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.util.csvUtil.CsvEditor;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.loader.FileLoader;
import com.arnor4eck.springkod.util.file.saver.FileSaveClass;
import com.arnor4eck.springkod.util.file.saver.FileSaver;
import com.arnor4eck.springkod.util.key.KeyGenerator;
import com.arnor4eck.springkod.util.request.markup.DeleteMarkupLineRequest;
import com.arnor4eck.springkod.util.request.markup.FileToDelete;
import com.arnor4eck.springkod.util.request.markup.FileToUpdate;
import com.arnor4eck.springkod.util.request.markup.UpdateMarkupLineRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
public class CsvService {

    private final FileLoader fileLoader;

    private final FileSaver fileSaver;

    private final KeyGenerator keyGenerator;

    private final DatasitoryFileRepository datasitoryFileRepository;

    public void deleteMarkupLineByValueInFirstColumn(long datasitoryId, DeleteMarkupLineRequest request) throws IOException {
        List<String> ids = new LinkedList<>();

        for(FileToDelete fileToDelete : request.filesToDelete()) {
            FileImpl file = fileLoader.load(datasitoryId, FileType.MARKUP_FILE);

            byte[] newFileContent = CsvEditor.removeRowByFirstColumnValue(file.getBytes(), fileToDelete.fileName());
            file.setContent(newFileContent);

            saveUpdatedFile(datasitoryId, file);
            ids.add(keyGenerator.generateKey(fileToDelete.fileName(), datasitoryId));
        }

        datasitoryFileRepository.deleteAllByFileIds(ids);
    }

    public void updateSecondColumnByFirstColumnValue(long datasitoryId, UpdateMarkupLineRequest request) throws IOException {
        for(FileToUpdate fileToUpdate : request.filesToUpdate()) {
            FileImpl file = fileLoader.load(datasitoryId, FileType.MARKUP_FILE);

            byte[] newFileContent = CsvEditor.updateSecondColumnByFirstColumnValue(
                    file.getBytes(), fileToUpdate.fileName(), fileToUpdate.newLabel()
            );
            file.setContent(newFileContent);

            saveUpdatedFile(datasitoryId, file);
        }
    }

    private void saveUpdatedFile(long datasitoryId, FileImpl file) {

        fileSaver.updateOnlyContent(new FileSaveClass(
                            keyGenerator.generateKey(file.getOriginalFilename(), datasitoryId), file));
    }
}
