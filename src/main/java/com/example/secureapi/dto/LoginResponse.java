package com.example.secureapi.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
