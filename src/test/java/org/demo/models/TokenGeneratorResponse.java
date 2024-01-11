package org.demo.models;

import java.util.Date;

public record TokenGeneratorResponse(
        String token,
        Date expires,
        String status,
        String result) {
}
