package com.team05.petmeeting.infra.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(byte[] bytes, String fileName) {
        String key = "cardnews/" + UUID.randomUUID() + "_" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType("image/png");

        try {
            amazonS3.putObject(new PutObjectRequest(bucket, key,
                    new ByteArrayInputStream(bytes), metadata));

            return amazonS3.getUrl(bucket, key).toString();
        } catch (Exception e) {
            e.printStackTrace(); // 추가
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }
}