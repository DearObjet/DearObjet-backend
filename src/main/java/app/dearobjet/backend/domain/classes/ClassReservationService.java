package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassReservationService {

    private final ClassReservationRepository classReservationRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public ClassReservationListResponse getReservations(Long userId, String status, int page, int size) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "reservationId")
        );

        Page<ClassReservation> reservationPage = (status == null || status.isBlank())
                ? classReservationRepository.findByClasses_Shop_User_Id(userId, pageable)
                : classReservationRepository.findByClasses_Shop_User_IdAndReservationStatus(
                userId,
                status,
                pageable
        );

        List<ClassReservationListResponse.Item> items = new ArrayList<>();
        for (ClassReservation reservation : reservationPage.getContent()) {
            items.add(new ClassReservationListResponse.Item(
                    reservation.getReservationStatus(),
                    reservation.getUser() == null ? null : reservation.getUser().getName(),
                    reservation.getUser() == null ? null : reservation.getUser().getPhoneNumber(),
                    reservation.getReservationId(),
                    reservation.getReservationTime(),
                    reservation.getClasses() == null ? null : reservation.getClasses().getClassName()
            ));
        }

        return new ClassReservationListResponse(
                reservationPage.getTotalElements(),
                items,
                page,
                reservationPage.getTotalPages()
        );
    }
}
