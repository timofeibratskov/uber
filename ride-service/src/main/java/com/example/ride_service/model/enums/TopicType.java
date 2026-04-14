package com.example.ride_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TopicType {
    RIDE_LIFECYCLE("ride_events_topic");

    private final String topicName;
}
