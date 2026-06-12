package com.example.ride_service.service;

import com.example.ride_service.exception.InvalidStatusTransitionException;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.enums.RideStatus;
import org.springframework.stereotype.Component;

@Component
public class RideStateMachine {
    public void changeRideStatus(RideEntity ride, RideStatus newStatus) {
        var currentStatus = ride.getStatus();

        validateTransition(currentStatus, newStatus);

        ride.setStatus(newStatus);
    }

    private void validateTransition(RideStatus currentStatus, RideStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case null -> newStatus == RideStatus.CREATED;
            case CREATED -> newStatus == RideStatus.ACCEPTED || newStatus == RideStatus.CANCELLED;
            case ACCEPTED -> newStatus == RideStatus.STARTED || newStatus == RideStatus.CANCELLED;
            case STARTED -> newStatus == RideStatus.COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException("Invalid status: " + newStatus + " for changing ride status from: " + currentStatus);
        }
    }
}
