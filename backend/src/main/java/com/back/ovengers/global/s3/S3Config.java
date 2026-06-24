package com.back.ovengers.global.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region}")
    private String region;

    /**
     * AWS S3와 통신하기 위한 S3Client Bean 등록
     *
     * Credential(Access Key / Secret Key)은
     * AWS SDK의 Default Credentials Provider Chain을 통해
     * 환경 변수 또는 IAM Role에서 자동으로 조회된다.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
