package org.demo.models;

import java.util.List;

public record UserResponse(String code, String message, String userID, String username, List<String> books) {
}
