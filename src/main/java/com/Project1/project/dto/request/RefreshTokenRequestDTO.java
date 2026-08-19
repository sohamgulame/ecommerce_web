package com.Project1.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
