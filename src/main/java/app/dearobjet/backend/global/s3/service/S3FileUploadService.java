package app.dearobjet.backend.global.s3.service;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3FileUploadService {

    private static final long MAX_DOCUMENT_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_IMAGE_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region}")
    private String region;

    public String uploadBusinessLicense(MultipartFile file, Long userId) {
        validateFile(file, "사업자등록증 파일", MAX_DOCUMENT_FILE_SIZE_BYTES);

        String key = buildKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadClassImage(MultipartFile file, Long userId) {
        validateImageFile(file, "클래스 사진 파일", MAX_IMAGE_FILE_SIZE_BYTES);

        String key = buildClassImageKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadProductImage(MultipartFile file, Long userId) {
        validateImageFile(file, "상품 이미지 파일", MAX_IMAGE_FILE_SIZE_BYTES);

        String key = buildProductImageKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadProfileImage(MultipartFile file, Long userId) {
        validateImageFile(file, "프로필 이미지 파일", MAX_IMAGE_FILE_SIZE_BYTES);

        String key = buildProfileImageKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadBankbookImage(MultipartFile file, Long userId) {
        validateFile(file, "통장사본 파일", MAX_DOCUMENT_FILE_SIZE_BYTES);

        String key = buildBankbookImageKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadPostImage(MultipartFile file, Long userId) {
        validateImageFile(file, "포스트 이미지 파일", MAX_IMAGE_FILE_SIZE_BYTES);

        String key = buildPostImageKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    public String uploadStoryThumbnail(MultipartFile file, Long userId) {
        validateImageFile(file, "스토리 썸네일 파일", MAX_IMAGE_FILE_SIZE_BYTES);

        String key = buildStoryThumbnailKey(userId, file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 읽기에 실패했습니다.");
        } catch (S3Exception e) {
            String detailMessage = e.awsErrorDetails() == null
                    ? e.getMessage()
                    : e.awsErrorDetails().errorCode() + ": " + e.awsErrorDetails().errorMessage();
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "S3 업로드에 실패했습니다. " + detailMessage
            );
        }
    }

    private void validateFile(MultipartFile file, String fileLabel, long maxFileSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fileLabel + "은(는) 필수입니다.");
        }

        validateFileSize(file, fileLabel, maxFileSizeBytes);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fileLabel + " 형식이 올바르지 않습니다. jpg, png, webp, pdf만 업로드할 수 있습니다."
            );
        }
    }

    private void validateImageFile(MultipartFile file, String fileLabel, long maxFileSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fileLabel + "은(는) 필수입니다.");
        }

        validateFileSize(file, fileLabel, maxFileSizeBytes);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fileLabel + " 형식이 올바르지 않습니다. jpg, png, webp만 업로드할 수 있습니다."
            );
        }
    }

    private void validateFileSize(MultipartFile file, String fileLabel, long maxFileSizeBytes) {
        if (file.getSize() > maxFileSizeBytes) {
            long maxFileSizeMb = maxFileSizeBytes / (1024 * 1024);
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fileLabel + " 크기는 " + maxFileSizeMb + "MB를 초과할 수 없습니다."
            );
        }
    }

    private String buildKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "business-license/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildClassImageKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "class-image/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildProductImageKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "product-image/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildProfileImageKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "profile-image/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildBankbookImageKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "bankbook-image/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildPostImageKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "post-image/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String buildStoryThumbnailKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "story-thumbnail/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }

        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }

        return filename.substring(index);
    }

    private String buildPublicUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
