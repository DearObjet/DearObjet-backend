package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.ClassListResponse;
import app.dearobjet.backend.domain.classes.dto.ClassResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassRequest;
import app.dearobjet.backend.domain.classes.dto.UpdateClassRequest;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassesService {
    private static final int MAX_CLASS_IMAGE_COUNT = 5;

    private final ClassesRepository classesRepository;
    private final ShopRepository shopRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public ClassResponse createClass(Long userId, CreateClassRequest request, List<MultipartFile> classImageFiles) {
        Shop shop = shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        List<String> classImageUrls = uploadClassImages(userId, classImageFiles, true);

        Classes classes = Classes.builder()
                .className(request.getClassName())
                .classDescription(request.getClassDescription())
                .classImageUrls(classImageUrls)
                .price(request.getPrice())
                .maxCapacity(request.getMaxCapacity())
                .notes(request.getNotes())
                .shop(shop)
                .build();

        Classes saved = classesRepository.save(classes);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClassListResponse getMyClasses(Long userId, int page, int size) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "classesId")
        );

        Page<Classes> classPage = classesRepository.findByShop_User_Id(userId, pageable);
        List<ClassListResponse.Item> items = new ArrayList<>();
        for (Classes classes : classPage.getContent()) {
            items.add(new ClassListResponse.Item(
                    classes.getClassesId(),
                    classes.getClassName(),
                    getFirstImageUrl(classes),
                    classes.getMaxCapacity()
            ));
        }

        return new ClassListResponse(items, page, classPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ClassResponse getMyClass(Long userId, Long classId) {
        Classes classes = classesRepository.findByClassesIdAndShop_User_Id(classId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));
        return toResponse(classes);
    }

    @Transactional
    public ClassResponse updateClass(
            Long userId,
            Long classId,
            UpdateClassRequest request,
            List<MultipartFile> classImageFiles
    ) {
        Classes classes = classesRepository.findByClassesIdAndShop_User_Id(classId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));

        classes.updateClassInfo(
                request.getClassName(),
                request.getClassDescription(),
                request.getPrice(),
                request.getMaxCapacity(),
                request.getNotes()
        );

        if (hasImageFiles(classImageFiles)) {
            List<String> classImageUrls = uploadClassImages(userId, classImageFiles, false);
            classes.updateClassImageUrls(classImageUrls);
        }

        return toResponse(classes);
    }

    @Transactional
    public void deleteClass(Long userId, Long classId) {
        Classes classes = classesRepository.findByClassesIdAndShop_User_Id(classId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));
        classesRepository.delete(classes);
    }

    private ClassResponse toResponse(Classes classes) {
        return new ClassResponse(
                classes.getClassesId(),
                classes.getClassName(),
                classes.getClassDescription(),
                List.copyOf(classes.getClassImageUrls()),
                classes.getPrice(),
                classes.getMaxCapacity(),
                classes.getNotes()
        );
    }

    private String getFirstImageUrl(Classes classes) {
        return classes.getClassImageUrls().isEmpty() ? null : classes.getClassImageUrls().get(0);
    }

    private List<String> uploadClassImages(Long userId, List<MultipartFile> classImageFiles, boolean required) {
        if (!hasImageFiles(classImageFiles)) {
            if (required) {
                throw new IllegalArgumentException("클래스 사진은 최소 1장 필요합니다.");
            }
            return List.of();
        }

        if (classImageFiles.size() > MAX_CLASS_IMAGE_COUNT) {
            throw new IllegalArgumentException("클래스 사진은 최대 5장까지 업로드할 수 있습니다.");
        }

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile classImageFile : classImageFiles) {
            if (classImageFile == null || classImageFile.isEmpty()) {
                throw new IllegalArgumentException("빈 클래스 사진 파일은 업로드할 수 없습니다.");
            }
            uploadedUrls.add(s3FileUploadService.uploadClassImage(classImageFile, userId));
        }
        return uploadedUrls;
    }

    private boolean hasImageFiles(List<MultipartFile> classImageFiles) {
        return classImageFiles != null
                && !classImageFiles.isEmpty()
                && classImageFiles.stream().anyMatch(file -> file != null && !file.isEmpty());
    }
}
