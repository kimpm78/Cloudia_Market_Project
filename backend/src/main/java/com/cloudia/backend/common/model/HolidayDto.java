package com.cloudia.backend.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDto {
    @JsonProperty("date")
    private LocalDate holidayDate;

    @JsonProperty("name")
    private String holidayName;

    @JsonProperty("type")
    private String holidayType;

    private String countryCode;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
