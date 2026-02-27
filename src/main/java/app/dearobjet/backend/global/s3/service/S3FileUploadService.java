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

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region}")
    private String region;

    public String uploadBusinessLicense(MultipartFile file, Long userId) {
        validateFile(file);

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
                    "S3 업로드 실패 - " + detailMessage
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사업자등록증 파일은 필수입니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "허용되지 않은 파일 형식입니다. (jpg, png, webp, pdf만 가능)"
            );
        }
    }

    private String buildKey(Long userId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "business-license/" + userId + "/" + UUID.randomUUID() + extension;
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
