package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class DatasitoryService {

    private final DatasitoryRepository datasitoryRepository;

    private final UserRepository userRepository;

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

}
