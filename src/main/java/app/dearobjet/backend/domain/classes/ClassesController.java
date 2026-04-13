package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.ClassListResponse;
import app.dearobjet.backend.domain.classes.dto.ClassResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassRequest;
import app.dearobjet.backend.domain.classes.dto.UpdateClassRequest;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClassesController {

    private final ClassesService classesService;

    @PostMapping(value = "/classes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ClassResponse> createClass(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") CreateClassRequest request,
            @RequestPart("classImageFiles") List<MultipartFile> classImageFiles
    ) {
        return ApiResponse.of(classesService.createClass(userDetails.getUserId(), request, classImageFiles));
    }

    @GetMapping("/classes")
    public ApiResponse<ClassListResponse> getMyClasses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(classesService.getMyClasses(userDetails.getUserId(), page, size));
    }

    @GetMapping("/classes/{classId}")
    public ApiResponse<ClassResponse> getMyClass(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long classId
    ) {
        return ApiResponse.of(classesService.getMyClass(userDetails.getUserId(), classId));
    }

    @PutMapping(value = "/classes/{classId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ClassResponse> updateClass(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long classId,
            @Valid @RequestPart("request") UpdateClassRequest request,
            @RequestPart(value = "classImageFiles", required = false) List<MultipartFile> classImageFiles
    ) {
        return ApiResponse.of(classesService.updateClass(userDetails.getUserId(), classId, request, classImageFiles));
    }

    @DeleteMapping("/classes/{classId}")
    public ApiResponse<Void> deleteClass(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long classId
    ) {
        classesService.deleteClass(userDetails.getUserId(), classId);
        return ApiResponse.of(null);
    }
}
