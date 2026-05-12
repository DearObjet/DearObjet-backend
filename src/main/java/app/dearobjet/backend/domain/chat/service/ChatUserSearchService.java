package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.chat.dto.ChatUserSearchResponse;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUserSearchService {

    private static final List<Role> SEARCHABLE_ROLES = List.of(Role.CUSTOMER, Role.ARTIST, Role.SHOP);

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    public Page<ChatUserSearchResponse> search(Long currentUserId, String keyword, Pageable pageable) {
        Long currentArtistId = artistRepository.findByUserId(currentUserId)
                .map(Artist::getId)
                .orElse(null);
        boolean contractStatusAvailable = currentArtistId != null;

        return userRepository.searchChatUsers(
                        currentUserId,
                        normalizeKeyword(keyword),
                        SEARCHABLE_ROLES,
                        Role.SHOP,
                        UserStatus.ACTIVE,
                        ContractStatus.PENDING,
                        ContractStatus.APPROVED,
                        LocalDate.now(),
                        currentArtistId,
                        pageable
                )
                .map(row -> ChatUserSearchResponse.from(row, contractStatusAvailable));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
