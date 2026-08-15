package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.AdminClassDetailResponse;
import app.dearobjet.backend.domain.classes.dto.AdminClassListResponse;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminClassService {

    private static final int PAGE_SIZE = 20;

    private final ClassesRepository classesRepository;
    private final ClassReservationRepository classReservationRepository;
    private final ClassSessionRepository classSessionRepository;

    @Transactional(readOnly = true)
    public AdminClassListResponse getClasses(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.strip() : "";
        Page<Classes> classPage = classesRepository.searchForAdmin(normalizedKeyword, pageable);

        List<AdminClassListResponse.ClassSummary> classes = classPage.getContent().stream()
                .map(classItem -> new AdminClassListResponse.ClassSummary(
                        classItem.getId(),
                        classItem.getClassName(),
                        classItem.getShop().getShopName(),
                        classItem.getCreatedAt(),
                        Boolean.TRUE.equals(classItem.getBlinded())
                ))
                .toList();

        return new AdminClassListResponse(classes, page, classPage.getTotalPages(), classPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminClassDetailResponse getClass(Long classId) {
        return toDetailResponse(findClass(classId));
    }

    @Transactional
    public AdminClassDetailResponse updateBlinded(Long classId, boolean blinded) {
        Classes classes = findClass(classId);
        classes.changeBlinded(blinded);

        return toDetailResponse(classes);
    }

    @Transactional
    public void deleteClass(Long classId) {
        Classes classes = findClass(classId);

        // FK 제약 때문에 자식 데이터부터 삭제
        classReservationRepository.deleteByClasses_Id(classId);
        classSessionRepository.deleteByClasses_Id(classId);

        classesRepository.delete(classes);
    }

    private Classes findClass(Long classId) {
        return classesRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));
    }

    private AdminClassDetailResponse toDetailResponse(Classes classes) {
        return new AdminClassDetailResponse(
                classes.getId(),
                classes.getClassName(),
                classes.getShop().getShopName(),
                classes.getCreatedAt(),
                Boolean.TRUE.equals(classes.getBlinded()),
                classes.getClassDescription(),
                List.copyOf(classes.getClassImageUrls()),
                classes.getPrice(),
                classes.getMaxCapacity(),
                classes.getNotes()
        );
    }
}
