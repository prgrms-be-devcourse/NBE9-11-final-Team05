package com.back.ovengers.global.s3;

public record S3UploadResult(
        String objectKey,
        String imageUrl
) {
}
