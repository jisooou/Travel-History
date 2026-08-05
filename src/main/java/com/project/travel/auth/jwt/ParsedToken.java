package com.project.travel.auth.jwt;

public record ParsedToken(Integer userNo, long remainingExpiration) {
}
