package com.cloudia.backend.batch.model;

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
    private LocalDate holidayDate;   // 祝日の日付
    @JsonProperty("name")
    private String holidayName;      // 祝日名
    @JsonProperty("type")
    private String holidayType;      // 祝日の種類（例：国民の祝日、振替休日など）
    private String countryCode;      // 国コード
    private String createdBy;        // 作成者
    private LocalDateTime createdAt; // 作成日時
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日時
}