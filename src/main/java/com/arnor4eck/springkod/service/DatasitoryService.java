package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.util.exception.FileNotFoundInStorageException;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
        return datasitoryRepository.findAllByUserEmail(email);
    }

    private List<Datasitory> getAllDatasitoriesByUserId(long userId) {
        String userEmail = userRepository.getEmailById(userId);

        return datasitoryRepository.findAllByUserEmail(userEmail);
    }

    public List<Datasitory> getAllDatasitoriesByUserIdExpectPrivate(long userId) {
        return getAllDatasitoriesByUserId(userId).stream()
                .filter(d -> d.getDatasitoryType() == DatasitoryType.OPEN)
                .toList();
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

    public StreamingResponseBody export(long datasitoryId) throws FileNotFoundInStorageException {
        return exportService.export(datasitoryId);
    }

    public void delete(long datasitoryId) {
        datasitoryRepository.deleteById(datasitoryId);
    }

    public boolean isOwner(Authentication auth, long datasitoryId) {
        String userEmail = (String) auth.getPrincipal();
        Datasitory datasitory = getById(datasitoryId);

        if(isOwner(userEmail, datasitory))
            return true;

        throw new AccessDeniedException("У вас нет доступа для этого действия.");
    }

    private boolean isOwner(String email, Datasitory datasitory) {
        return datasitory.getCreator().getEmail().equals(email);
    }

    public boolean hasAccess(Authentication auth, long datasitoryId) {
        Datasitory datasitory = getById(datasitoryId);

        if(datasitory.getDatasitoryType() == DatasitoryType.OPEN)
            return true;

        String userEmail = (String) auth.getPrincipal();

        if(isOwner(userEmail, datasitory))
            return true;

        List<DatasitoryMember> members = datasitoryMembersService.findAllMembersExceptOwner(datasitory);

        for(DatasitoryMember member : members){ // в будущем можно заменить одним запросом
            if(member.getUser().getEmail().equals(userEmail))
                return true;
        }

        throw new AccessDeniedException("У вас нет доступа к данному датазиторию.");
    }
}
