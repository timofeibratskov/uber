package com.example.ride_service;

import com.example.ride_service.dto.RideCreatedEvent;
import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.dto.RatingIdEvent;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.enums.SenderType;
import com.example.ride_service.exception.InvalidStatusException;
import com.example.ride_service.exception.NotFoundException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.repo.RideRepo;
import com.example.ride_service.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {

    @Mock
    private RideRepo rideRepo;

    @Mock
    private RideMapper mapper;

    @Mock
    private KafkaTemplate<String, RideCreatedEvent> kafkaTemplate;

    @InjectMocks
    private RideService rideService;

    private RideEntity rideEntity;
    private RideRequestDto rideRequestDto;
    private RideCreatedEvent rideCreatedEvent;

    @BeforeEach
    void setUp() {
        rideEntity = RideEntity.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .status(RideStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        rideRequestDto = new RideRequestDto("ул. Пушкина, 15", "пр. Ленина, 42", 1L, (byte)3);
        rideRequestDto.setPointA("Start");
        rideRequestDto.setPointB("End");
        rideRequestDto.setCreatorId(1L);
        rideRequestDto.setSeats((byte) 4);

        rideCreatedEvent = RideCreatedEvent.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .build();
    }

    @Test
    void createRide_Success() {
        // Arrange
        when(mapper.toEntity(rideRequestDto)).thenReturn(rideEntity);
        when(rideRepo.save(rideEntity)).thenReturn(rideEntity);
        when(mapper.requestToEvent(rideRequestDto)).thenReturn(rideCreatedEvent);

        // Act
        RideStatus result = rideService.createRide(rideRequestDto);

        // Assert
        assertEquals(RideStatus.CREATED, result);
        verify(rideRepo, times(2)).save(rideEntity);
        verify(kafkaTemplate).send(eq("ride-created"), eq(rideCreatedEvent));
    }

    @Test
    void payRide_Success() {
        // Arrange
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));

        // Act
        String result = rideService.payRide("1a2s3d", RideStatus.PAID, BigDecimal.valueOf(100.0));

        // Assert
        assertEquals("поездка оплачена!", result);
        assertEquals(RideStatus.PAID, rideEntity.getStatus());
        assertEquals(BigDecimal.valueOf(100.0), rideEntity.getAmount());
        verify(rideRepo).save(rideEntity);
    }

    @Test
    void assignDriver_Success() {
        // Arrange
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));

        // Act
        rideService.assignDriver("1a2s3d", 2L);

        // Assert
        assertEquals(2L, rideEntity.getDriverId());
        assertEquals(RideStatus.DRIVER_FOUND, rideEntity.getStatus());
        verify(rideRepo).save(rideEntity);
    }

    @Test
    void assignDriver_InvalidStatus() {
        // Arrange
        rideEntity.setStatus(RideStatus.PAID);
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));

        // Act & Assert
        InvalidStatusException exception = assertThrows(InvalidStatusException.class, () -> {
            rideService.assignDriver("1a2s3d", 2L);
        });
        assertEquals("водителя можно назначить только на CREATED поездку", exception.getMessage());
    }

    @Test
    void changeStatus_Success() {
        // Arrange
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));

        // Act
        String result = rideService.changeStatus("1a2s3d", RideStatus.COMPLETED);

        // Assert
        assertEquals("СТАТУС БЫЛ ИЗМЕНЕН УСПЕШНО", result);
        assertEquals(RideStatus.COMPLETED, rideEntity.getStatus());
        assertNotNull(rideEntity.getCompletedIn());
        verify(rideRepo).save(rideEntity);
    }

    @Test
    void findRideById_Success() {
        // Arrange
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));
        when(mapper.toDto(rideEntity)).thenReturn(RideDto.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .driverId(null)
                .amount(null)
                .status(RideStatus.CREATED)
                .createdAt(rideEntity.getCreatedAt())
                .updatedAt(rideEntity.getUpdatedAt())
                .completedIn(null)
                .passengerRatingId(null)
                .driverRatingId(null)
                .build());

        // Act
        RideDto result = rideService.findRideById("1a2s3d");

        // Assert
        assertNotNull(result);
        assertEquals("1a2s3d", result.getId());
        verify(rideRepo).findById("1a2s3d");
    }

    @Test
    void findRideById_NotFound() {
        // Arrange
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rideService.findRideById("1a2s3d");
        });
        assertEquals("поездка с таким айди: 1a2s3d не найдено", exception.getMessage());
    }

    @Test
    void getRidesByPassengerId_Success() {
        // Arrange
        when(rideRepo.findAllByCreatorId(1L)).thenReturn(List.of(rideEntity));
        when(mapper.toDto(rideEntity)).thenReturn(RideDto.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .driverId(null)
                .amount(null)
                .status(RideStatus.CREATED)
                .createdAt(rideEntity.getCreatedAt())
                .updatedAt(rideEntity.getUpdatedAt())
                .completedIn(null)
                .passengerRatingId(null)
                .driverRatingId(null)
                .build());

        // Act
        List<RideDto> result = rideService.getRidesByPassengerId(1L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("1a2s3d", result.get(0).getId());
    }

    @Test
    void getRidesByPassengerId_NotFound() {
        // Arrange
        when(rideRepo.findAllByCreatorId(1L)).thenReturn(List.of());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rideService.getRidesByPassengerId(1L);
        });
        assertEquals("Поезки для пассажира с таким id не существует", exception.getMessage());
    }

    @Test
    void getRidesByDriverId_Success() {
        // Arrange
        rideEntity.setDriverId(2L);
        when(rideRepo.findAllByDriverId(2L)).thenReturn(List.of(rideEntity));
        when(mapper.toDto(rideEntity)).thenReturn(RideDto.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .driverId(2L)
                .amount(null)
                .status(RideStatus.CREATED)
                .createdAt(rideEntity.getCreatedAt())
                .updatedAt(rideEntity.getUpdatedAt())
                .completedIn(null)
                .passengerRatingId(null)
                .driverRatingId(null)
                .build());

        // Act
        List<RideDto> result = rideService.getRidesByDriverId(2L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("1a2s3d", result.get(0).getId());
    }

    @Test
    void getRidesByDriverId_NotFound() {
        // Arrange
        when(rideRepo.findAllByDriverId(2L)).thenReturn(List.of());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rideService.getRidesByDriverId(2L);
        });
        assertEquals("Поезки для водителя с таким id не существует", exception.getMessage());
    }

    @Test
    void getRidesByStatus_Success() {
        // Arrange
        when(rideRepo.findByStatus(RideStatus.CREATED)).thenReturn(List.of(rideEntity));
        when(mapper.toDto(rideEntity)).thenReturn(RideDto.builder()
                .id("1a2s3d")
                .pointA("Start")
                .pointB("End")
                .creatorId(1L)
                .seats((byte) 4)
                .driverId(null)
                .amount(null)
                .status(RideStatus.CREATED)
                .createdAt(rideEntity.getCreatedAt())
                .updatedAt(rideEntity.getUpdatedAt())
                .completedIn(null)
                .passengerRatingId(null)
                .driverRatingId(null)
                .build());

        // Act
        List<RideDto> result = rideService.getRidesByStatus(RideStatus.CREATED);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("1a2s3d", result.get(0).getId());
    }

    @Test
    void getRidesByStatus_NotFound() {
        // Arrange
        when(rideRepo.findByStatus(RideStatus.CREATED)).thenReturn(List.of());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rideService.getRidesByStatus(RideStatus.CREATED);
        });
        assertEquals("Поезки с таким статусом пока что не существует", exception.getMessage());
    }

    @Test
    void addRatingInRide_Success() {
        // Arrange
        RatingIdEvent event = new RatingIdEvent("1a2s3d", 1L, SenderType.DRIVER);
        when(rideRepo.findById("1a2s3d")).thenReturn(Optional.of(rideEntity));

        // Act
        rideService.addRatingInRide(event);

        // Assert
        assertEquals(1L, rideEntity.getPassengerRatingId());
        verify(rideRepo).save(rideEntity);
    }
}