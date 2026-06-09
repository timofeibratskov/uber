package com.example.rating_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_rating_table")
public class UserRatingEntity {
    @Id
    private String id;

    private UUID targetUserId;

    private BigDecimal averageRating;

    private Long ratingCount;

    private Long ratingSum;
}
