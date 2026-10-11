
package com.securebank.dto;

import java.util.UUID;

public record LoginResponse(
        String message,
        UUID userId,
        String email

) {}
