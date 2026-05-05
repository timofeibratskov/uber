package com.example.payment_service.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TopicType {
    PAYMENT("payment_events_topic");

    private final String topicName;

    public static final String PAYMENT_TOPIC = "payment_events_topic";
}
