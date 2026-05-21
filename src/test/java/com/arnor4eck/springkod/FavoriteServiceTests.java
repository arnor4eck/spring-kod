package com.arnor4eck.springkod;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.favorite.Favourites;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.FavoriteRepository;
import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.service.FavoriteService;
import com.arnor4eck.springkod.service.UserService;
import com.arnor4eck.springkod.util.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTests {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserService userService;

    @Mock
    private DatasitoryService datasitoryService;

    @InjectMocks
    private FavoriteService favoriteService;

    private User mockUser;
    private Datasitory mockDatasitory;
    private Favourites mockFavourite;
    private final String email = "user@example.com";
    private final Long datasitoryId = 1L;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockDatasitory = mock(Datasitory.class);
        mockFavourite = mock(Favourites.class);
    }

    // ===================== addFavorite Tests =====================

    @Test
    void addFavorite_shouldSaveSuccessfully() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId)).thenReturn(mockDatasitory);

        favoriteService.addFavorite(email, datasitoryId);

        verify(favoriteRepository).save(any(Favourites.class));
    }

    @Test
    void addFavorite_shouldThrowBusinessException_whenSaveFails() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId)).thenReturn(mockDatasitory);
        doThrow(new RuntimeException("DB error")).when(favoriteRepository).save(any());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> favoriteService.addFavorite(email, datasitoryId));

        assertEquals("Filed to create favorite", exception.getMessage());
        verify(favoriteRepository).save(any(Favourites.class));
    }

    @Test
    void addFavorite_shouldThrowException_whenUserNotFound() {
        when(userService.getUser(email)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class,
                () -> favoriteService.addFavorite(email, datasitoryId));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_shouldThrowException_whenDatasitoryNotFound() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId))
                .thenThrow(new NotFoundException("Datasitory not found"));

        assertThrows(NotFoundException.class,
                () -> favoriteService.addFavorite(email, datasitoryId));

        verify(favoriteRepository, never()).save(any());
    }

    // ===================== getFavoritesIds Tests =====================

    @Test
    void getFavoritesIds_shouldReturnListOfIds() {
        List<Long> expectedIds = Arrays.asList(1L, 2L, 3L);
        when(userService.getUser(email)).thenReturn(mockUser);
        when(favoriteRepository.findDatasitoryIdsByUser(mockUser)).thenReturn(expectedIds);

        List<Long> result = favoriteService.getFavoritesIds(email);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedIds, result);
        verify(favoriteRepository).findDatasitoryIdsByUser(mockUser);
    }

    @Test
    void getFavoritesIds_shouldReturnEmptyList_whenNoFavorites() {
        List<Long> emptyList = Collections.emptyList();
        when(userService.getUser(email)).thenReturn(mockUser);
        when(favoriteRepository.findDatasitoryIdsByUser(mockUser)).thenReturn(emptyList);

        List<Long> result = favoriteService.getFavoritesIds(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getFavoritesIds_shouldThrowException_whenUserNotFound() {
        when(userService.getUser(email)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class,
                () -> favoriteService.getFavoritesIds(email));

        verify(favoriteRepository, never()).findDatasitoryIdsByUser(any());
    }

    @Test
    void getFavoritesIds_shouldThrowException_whenRepositoryFails() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(favoriteRepository.findDatasitoryIdsByUser(mockUser))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> favoriteService.getFavoritesIds(email));
    }

    // ===================== deleteFavorite Tests =====================

    @Test
    void deleteFavorite_shouldDeleteSuccessfully() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId)).thenReturn(mockDatasitory);
        when(favoriteRepository.findByUserAndDatasitory(mockUser, mockDatasitory))
                .thenReturn(Optional.of(mockFavourite));

        favoriteService.deleteFavorite(email, datasitoryId);

        verify(favoriteRepository).findByUserAndDatasitory(mockUser, mockDatasitory);
        verify(favoriteRepository).delete(mockFavourite);
    }

    @Test
    void deleteFavorite_shouldThrowNotFoundException_whenFavoriteNotFound() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId)).thenReturn(mockDatasitory);
        when(favoriteRepository.findByUserAndDatasitory(mockUser, mockDatasitory))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> favoriteService.deleteFavorite(email, datasitoryId));

        assertEquals("Filed to delete favorite", exception.getMessage());
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void deleteFavorite_shouldThrowBusinessException_whenDeleteFails() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId)).thenReturn(mockDatasitory);
        when(favoriteRepository.findByUserAndDatasitory(mockUser, mockDatasitory))
                .thenReturn(Optional.of(mockFavourite));
        doThrow(new RuntimeException("DB error")).when(favoriteRepository).delete(mockFavourite);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> favoriteService.deleteFavorite(email, datasitoryId));

        assertEquals("Filed to delete favorite", exception.getMessage());
        verify(favoriteRepository).delete(mockFavourite);
    }

    @Test
    void deleteFavorite_shouldThrowException_whenUserNotFound() {
        when(userService.getUser(email)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class,
                () -> favoriteService.deleteFavorite(email, datasitoryId));

        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void deleteFavorite_shouldThrowException_whenDatasitoryNotFound() {
        when(userService.getUser(email)).thenReturn(mockUser);
        when(datasitoryService.getById(datasitoryId))
                .thenThrow(new NotFoundException("Datasitory not found"));

        assertThrows(NotFoundException.class,
                () -> favoriteService.deleteFavorite(email, datasitoryId));

        verify(favoriteRepository, never()).delete(any());
    }
}