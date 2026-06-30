package com.back.ovengers.global.s3;

import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.cloudfront-domain}")
    private String cloudFrontDomain;

    /**
     * 캠핑장 이미지를 S3에 업로드
     *
     * 저장 경로 예시: campings/{campingId}/{uuid}.jpg
     * S3에는 objectKey 기준으로 저장하고, 클라이언트에는 CloudFront URL을 내려준다.
     */
    public S3UploadResult upload(MultipartFile file, Long campingId) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String objectKey = "campings/" + campingId + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String imageUrl = getFileUrl(objectKey);

            return new S3UploadResult(objectKey, imageUrl);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public S3UploadResult upload(MultipartFile file, String folder) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String objectKey = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return new S3UploadResult(objectKey, getFileUrl(objectKey));

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // S3에서 objectKey에 해당하는 파일을 삭제
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }


    // 업로드된 파일이 비어 있지 않은 이미지 파일인지 검증
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    //원본 파일명에서 확장자를 추출
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }

    // CloudFront 도메인과 objectKey를 조합해 이미지 조회 URL 생성
    private String getFileUrl(String objectKey) {
        return "https://" + cloudFrontDomain + "/" + objectKey;
    }
}