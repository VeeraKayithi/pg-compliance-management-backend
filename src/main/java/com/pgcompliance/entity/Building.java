package com.pgcompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long buildingId;

    private String buildingName;

    private String buildingType;

    private String address;

    private String city;

    private LocalDateTime createdAt;
}