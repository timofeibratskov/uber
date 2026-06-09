package com.example.rating_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rating_table")
@CompoundIndex(
        name = "uk_ride_target_user",
        def = "{'rideId': 1, 'raterUserId': 1, 'targetUserId': 1}",
        unique = true
)
public class RatingEntity {

    @Id
    private String id;

    private UUID rideId;

    @Indexed
    private UUID targetUserId;

    private UUID raterUserId;

    private Integer rating;

    @CreatedDate
    private LocalDateTime createdAt;
}
