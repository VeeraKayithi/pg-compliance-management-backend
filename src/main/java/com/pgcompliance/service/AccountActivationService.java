package com.pgcompliance.service;

import com.pgcompliance.dto.ActivationValidationResponseDto;
import com.pgcompliance.dto.CompleteActivationRequestDto;

public interface AccountActivationService {

    void createAndSendActivation(String username);

    ActivationValidationResponseDto validateToken(String rawToken);

    void completeActivation(CompleteActivationRequestDto request);

    void resendActivation(String username);
}
