package app.dearobjet.backend.domain.chat.dto.projection;

import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;

public interface ChatUserSearchRow {

    Long getUserId();

    String getAccountName();

    Role getRole();

    String getProfileImageUrl();

    Specialty getSpecialty();

    Boolean getPendingContractExists();

    Boolean getApprovedContractExists();
}
