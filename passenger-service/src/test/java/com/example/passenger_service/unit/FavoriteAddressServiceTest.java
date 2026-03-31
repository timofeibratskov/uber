package com.example.passenger_service.unit;

import com.example.passenger_service.exception.FavoriteAddressLimitException;
import com.example.passenger_service.exception.FavoriteAddressNotFoundException;
import com.example.passenger_service.mapper.FavoriteAddressMapper;
import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.model.entity.FavoriteAddressEntity;
import com.example.passenger_service.repo.FavoriteAddressRepo;
import com.example.passenger_service.service.FavoriteAddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteAddressServiceTest {

    @Mock
    private FavoriteAddressRepo favoriteAddressRepo;

    @Mock
    private FavoriteAddressMapper favoriteAddressMapper;

    @InjectMocks
    private FavoriteAddressService favoriteAddressService;

    @Test
    @DisplayName("Успешное получение списка всех избранных адресов пассажира")
    void getAllAddressesByPassengerId_ShouldReturnList_WhenAddressesExist() {
        // Arrange
        UUID passengerId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        FavoriteAddressEntity entity = FavoriteAddressEntity.builder()
                .id(UUID.randomUUID())
                .passengerId(passengerId)
                .label("Gym")
                .address("Grodno, Kosmonavtov 100")
                .build();

        FavoriteAddressResponseDto responseDto = FavoriteAddressResponseDto.builder()
                .label("Gym")
                .address("Grodno, Kosmonavtov 100")
                .build();

        List<FavoriteAddressEntity> entities = List.of(entity);

        when(favoriteAddressRepo.findByPassengerId(passengerId)).thenReturn(entities);
        when(favoriteAddressMapper.toResponseDto(entity)).thenReturn(responseDto);

        // Act
        List<FavoriteAddressResponseDto> actualResponse = favoriteAddressService.getAllAddressesByPassengerId(passengerId);

        // Assert
        assertThat(actualResponse).hasSize(1);
        assertThat(actualResponse.getFirst().label()).isEqualTo("Gym");
        assertThat(actualResponse.getFirst().address()).isEqualTo("Grodno, Kosmonavtov 100");

        verify(favoriteAddressRepo, times(1)).findByPassengerId(passengerId);
        verify(favoriteAddressMapper, times(1)).toResponseDto(entity);
    }

    @Test
    @DisplayName("Успешное добавление избранного адреса, когда лимит не превышен")
    void addFavoriteAddress_ShouldSaveAddress_WhenLimitNotExceeded() {
        // Arrange
        UUID passengerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        FavoriteAddressRequestDto requestDto = FavoriteAddressRequestDto.builder()
                .label("Home")
                .address("Grodno, Sovetskaya 10")
                .build();

        FavoriteAddressEntity entity = FavoriteAddressEntity.builder()
                .passengerId(passengerId)
                .label("Home")
                .address("Grodno, Sovetskaya 10")
                .build();

        FavoriteAddressResponseDto expectedResponse = FavoriteAddressResponseDto.builder()
                .label("Home")
                .address("Grodno, Sovetskaya 10")
                .build();

        when(favoriteAddressRepo.findByPassengerId(passengerId)).thenReturn(Collections.emptyList());
        when(favoriteAddressMapper.toEntity(passengerId, requestDto)).thenReturn(entity);
        when(favoriteAddressRepo.save(entity)).thenReturn(entity);
        when(favoriteAddressMapper.toResponseDto(entity)).thenReturn(expectedResponse);

        // Act
        FavoriteAddressResponseDto actualResponse = favoriteAddressService.addFavoriteAddress(passengerId, requestDto);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.label()).isEqualTo("Home");
        assertThat(actualResponse.address()).isEqualTo("Grodno, Sovetskaya 10");
        verify(favoriteAddressRepo, times(1)).findByPassengerId(passengerId);
        verify(favoriteAddressRepo, times(1)).save(entity);
    }

    @Test
    @DisplayName("Выброс исключения при превышении лимита (5 адресов)")
    void addFavoriteAddress_ShouldThrowException_WhenLimitIsExceeded() {
        // Arrange
        UUID passengerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        FavoriteAddressRequestDto requestDto = FavoriteAddressRequestDto.builder().label("Work").build();
        List<FavoriteAddressEntity> existingAddresses = Collections.nCopies(5, mock(FavoriteAddressEntity.class));

        when(favoriteAddressRepo.findByPassengerId(passengerId)).thenReturn(existingAddresses);

        // Act
        Executable action = () -> favoriteAddressService.addFavoriteAddress(passengerId, requestDto);

        // Assert
        FavoriteAddressLimitException exception = assertThrows(FavoriteAddressLimitException.class, action);
        assertThat(exception.getMessage()).isEqualTo("favorite addresses is too much!");
        verify(favoriteAddressRepo, times(1)).findByPassengerId(passengerId);
        verify(favoriteAddressRepo, never()).save(any(FavoriteAddressEntity.class));
    }

    @Test
    @DisplayName("Успешное удаление существующего адреса")
    void removeFavoriteAddress_ShouldDelete_WhenAddressExists() {
        // Arrange
        UUID passengerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID addressId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        when(favoriteAddressRepo.deleteByIdAndPassengerId(addressId, passengerId)).thenReturn(1);

        // Act
        favoriteAddressService.removeFavoriteAddress(passengerId, addressId);

        // Assert
        verify(favoriteAddressRepo, times(1)).deleteByIdAndPassengerId(addressId, passengerId);
    }

    @Test
    @DisplayName("Выброс исключения при удалении несуществующего адреса")
    void removeFavoriteAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        UUID passengerId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID addressId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        when(favoriteAddressRepo.deleteByIdAndPassengerId(addressId, passengerId)).thenReturn(0);

        // Act
        Executable action = () -> favoriteAddressService.removeFavoriteAddress(passengerId, addressId);

        // Assert
        FavoriteAddressNotFoundException exception = assertThrows(FavoriteAddressNotFoundException.class, action);
        assertThat(exception.getMessage()).contains(passengerId.toString());
        verify(favoriteAddressRepo, times(1)).deleteByIdAndPassengerId(addressId, passengerId);
    }
}