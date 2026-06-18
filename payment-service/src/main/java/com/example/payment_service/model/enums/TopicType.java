package com.example.payment_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TopicType {
    RIDE_LIFECYCLE("ride_events_topic"),
    PAYMENT("payment_events_topic");

    private final String topicName;

    public static final String RIDE_LIFECYCLE_TOPIC = "ride_events_topic";
    public static final String PAYMENT_TOPIC = "payment_events_topic";
}
