package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.UserReservationHistoryResponse;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassReservationService {

    private final ClassReservationRepository classReservationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserReservationHistoryResponse> getMyReservations(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        return classReservationRepository.findByUserIdWithClassAndShop(userId).stream()
                .map(UserReservationHistoryResponse::from)
                .toList();
    }
}
