package app.dearobjet.backend.domain.user.dto;

public record AdminRoleCountResponse(
        long customerCount,
        long shopCount,
        long artistCount
) {
}
