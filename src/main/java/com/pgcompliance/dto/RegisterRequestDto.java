package com.pgcompliance.dto;

import com.pgcompliance.constant.UserRole;
import lombok.Data;

@Data
public class RegisterRequestDto {

  private String username;

  private String password;

  private UserRole role;
}