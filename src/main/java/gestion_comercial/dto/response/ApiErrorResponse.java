package gestion_comercial.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {

    public static ApiErrorResponse simple(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                Map.of()
        );
    }

    public static ApiErrorResponse validation(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                errors
        );
    }
}