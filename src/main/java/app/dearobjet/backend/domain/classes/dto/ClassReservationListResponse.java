package app.dearobjet.backend.domain.classes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassReservationListResponse {
    private final long reservationCount;
    private final List<ClassReservationItemResponse> reservations;
}
