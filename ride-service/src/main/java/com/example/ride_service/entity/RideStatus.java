package com.example.ride_service.entity;

public enum RideStatus {
    CREATED,       // Создана
    DRIVER_FOUND,  // Водитель найден
    IN_PROGRESS,   // В процессе
    COMPLETED,     // Завершена
    CANCELLED,     // Отменена
    PAID           // Оплачена
}