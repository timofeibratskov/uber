package com.example.payment_service.infrastructure.persistence.entity;

import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.TopicType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "outbox_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEntity {
    @Id
    private Long id;
    private TopicType topic;
    private EventType eventType;
    private String payload;
    private Instant createdAt = Instant.now();
}
