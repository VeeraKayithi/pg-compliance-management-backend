package com.pgcompliance.entity;

import com.pgcompliance.constant.TenantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tenants", uniqueConstraints = {
    @UniqueConstraint(columnNames = "mobile_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tenantId;

  @Column(nullable = false)
  private String name;

  @Column(name = "mobile_number", nullable = false, unique = true)
  private String mobileNumber;

  private String email;

  private LocalDate joiningDate;

  @Enumerated(EnumType.STRING)
  private TenantStatus tenantStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;
}