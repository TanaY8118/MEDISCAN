package com.mediscan.dto.v1.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "events", "totalEvents", "page", "pageSize" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineResponse {
    List<TimelineEventDTO> events;
    int totalEvents;
    int page;
    int pageSize;
}
