package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class DatasitoryService {

    private final DatasitoryMembersService datasitoryMembersService;

    private final DatasitoryRepository datasitoryRepository;

    private final UserRepository userRepository;

    private final ExportService exportService;

    public Datasitory createDatasitory(CreateDatasitoryRequest createDatasitoryRequest) {
        User creator = userRepository.findById(createDatasitoryRequest.creatorId()).get();

        Datasitory datasitory = Datasitory.builder()
                .name(createDatasitoryRequest.name())
                .description(createDatasitoryRequest.description())
                .datasitoryType(DatasitoryType.valueOf(createDatasitoryRequest.datasitoryType()))
                .creator(creator)
                .build();

        return datasitoryRepository.save(datasitory);
    }

    public Datasitory getById(long id) {
        return datasitoryRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Репозитория с заданным ID нет.")
        );
    }

    public List<Datasitory> getAllDatasitoriesByUserEmail(String email) {
        return datasitoryRepository.findAllByUserEmail(email); // TODO проверить, попадают ли даты, в которх пользователь как юзер
    }

    public List<Datasitory> getAllDatasitoriesByUserId(long userId) {
        String userEmail = userRepository.getEmailById(userId);

        return datasitoryRepository.findAllByUserEmail(userEmail);
    }

    public DatasitoryMember addMember(long datasitoryId,
                                      AddMemberToDatasitoryRequest request){
        return datasitoryMembersService.addMember(datasitoryId, request);
    }

    public List<Datasitory> getDatasitoriesByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return datasitoryRepository.findAllById(ids);
    }

    public StreamingResponseBody export(long datasitoryId) throws FileNotFoundException {
        return exportService.export(datasitoryId);
    }
}
