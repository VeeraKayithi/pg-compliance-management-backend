package com.pgcompliance.dto;

import com.pgcompliance.constant.RoomStatus;
import com.pgcompliance.constant.SharingType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomResponseDto {

    private Long roomId;

    private Long buildingId;

    private String buildingName;

    private String roomNumber;

    private SharingType sharingType;

    private Integer capacity;

    private RoomStatus roomStatus;
}