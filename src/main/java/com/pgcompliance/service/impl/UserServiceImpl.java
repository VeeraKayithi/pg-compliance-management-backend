package com.pgcompliance.service.impl;

import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.constant.TenantStatus;
import com.pgcompliance.constant.UserRole;
import com.pgcompliance.dto.TenantAccountRequestDto;
import com.pgcompliance.dto.TenantAccountResponseDto;
import com.pgcompliance.entity.Room;
import com.pgcompliance.entity.Tenant;
import com.pgcompliance.entity.User;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.TenantRepository;
import com.pgcompliance.repository.UserRepository;
import com.pgcompliance.service.NotificationService;
import com.pgcompliance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TenantAccountResponseDto createTenantAccount(
            TenantAccountRequestDto request
    ) {
        Tenant tenant = tenantRepository
                .findById(request.getTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with id: "
                                        + request.getTenantId()
                        )
                );

        if (tenant.getTenantStatus() != TenantStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Login account can be created only for an ACTIVE tenant"
            );
        }

        if (userRepository.existsByTenantTenantId(
                tenant.getTenantId())) {
            throw new IllegalStateException(
                    "A login account already exists for this tenant"
            );
        }

        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException(
                    "Username already exists"
            );
        }

        User user = User.builder()
                .username(username)
                .password(
                        passwordEncoder.encode(
                                request.getTemporaryPassword()
                        )
                )
                .role(UserRole.TENANT)
                .active(true)
                .tenant(tenant)
                .build();

        User savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser.getUserId(),
                NotificationEventType.TENANT_ACCOUNT_ACTIVATED,
                NotificationSeverity.INFO,
                "USER",
                "Portal account activated",
                "Your Nandu PG portal account has been activated.",
                "/tenant/dashboard",
                "TENANT",
                tenant.getTenantId(),
                "TENANT_ACCOUNT_ACTIVATED:"
                        + tenant.getTenantId()
        );

        createInitialRoomAssignmentNotification(
                savedUser,
                tenant
        );

        return TenantAccountResponseDto.builder()
                .userId(savedUser.getUserId())
                .tenantId(tenant.getTenantId())
                .tenantName(tenant.getName())
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .active(savedUser.getActive())
                .message(
                        "Tenant login account created successfully"
                )
                .build();
    }

    private void createInitialRoomAssignmentNotification(
            User tenantUser,
            Tenant tenant
    ) {
        Room room = tenant.getRoom();

        if (room == null) {
            return;
        }

        notificationService.createNotification(
                tenantUser.getUserId(),
                NotificationEventType.TENANT_ROOM_ASSIGNED,
                NotificationSeverity.INFO,
                "TENANT",
                "Room assigned",
                "You have been assigned to Room "
                        + room.getRoomNumber()
                        + " in "
                        + room.getBuilding().getBuildingName()
                        + ".",
                "/tenant/dashboard",
                "TENANT",
                tenant.getTenantId(),
                "TENANT_ROOM_ASSIGNED:"
                        + tenant.getTenantId()
                        + ":"
                        + room.getRoomId()
        );
    }
}
