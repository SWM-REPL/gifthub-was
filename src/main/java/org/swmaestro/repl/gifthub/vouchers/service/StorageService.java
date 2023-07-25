package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swmaestro.repl.gifthub.vouchers.dto.S3FileDto;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {
	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;
	@Value("${cloud.aws.region.static}")
	private String bucketRegion;
	private final AmazonS3Client amazonS3Client;

	public S3FileDto save(String dirName, MultipartFile multipartFile) throws IOException {
		String originalFileName = multipartFile.getOriginalFilename();
		String uploadFilePath = dirName + "/";
		String uploadFileName = getUUidFileName(originalFileName);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		String keyName = uploadFilePath + uploadFileName;    // example: "vouchers/uuid.jpg"

		InputStream inputStream = multipartFile.getInputStream();
		amazonS3Client.putObject(new PutObjectRequest(bucketName, keyName, inputStream, metadata));

		String uploadFileUrl = amazonS3Client.getUrl(bucketName, keyName).toString();
		return S3FileDto.builder()
			.uploadFileName(uploadFileName)
			.build();
	}

	public String getBucketAddress(String dirName) {
		return "https://" + bucketName + ".s3." + bucketRegion + ".amazonaws.com/" + dirName + "/";
	}

	private String getUUidFileName(String fileName) {
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		return UUID.randomUUID().toString() + "." + ext;
	}
}
