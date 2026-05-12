package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.dto.projection.ChatUserSearchRow;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatUserSearchResponse {

    private static final String STATUS_PENDING = "계약 대기";
    private static final String STATUS_COMPLETED = "계약 완료";
    private static final String STATUS_NOT_CONTRACTED = "미계약";
    private static final String STATUS_UNAVAILABLE = "-";

    private final Long userId;
    private final String accountName;
    private final Role role;
    private final String profileImageUrl;
    private final Specialty specialty;
    private final String statusLabel;

    public static ChatUserSearchResponse from(ChatUserSearchRow row, boolean contractStatusAvailable) {
        return new ChatUserSearchResponse(
                row.getUserId(),
                row.getAccountName(),
                row.getRole(),
                row.getProfileImageUrl(),
                row.getSpecialty(),
                resolveStatusLabel(row, contractStatusAvailable)
        );
    }

    private static String resolveStatusLabel(ChatUserSearchRow row, boolean contractStatusAvailable) {
        if (!contractStatusAvailable || row.getRole() != Role.SHOP) {
            return STATUS_UNAVAILABLE;
        }
        if (Boolean.TRUE.equals(row.getPendingContractExists())) {
            return STATUS_PENDING;
        }
        if (Boolean.TRUE.equals(row.getApprovedContractExists())) {
            return STATUS_COMPLETED;
        }

        return STATUS_NOT_CONTRACTED;
    }
}
