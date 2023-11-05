package org.swmaestro.repl.gifthub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class AmazonS3Config {
	@Value("${cloud.aws.s3.access-key}")
	private String accessKey;

	@Value("${cloud.aws.s3.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.s3.max-file-size}")
	private String maxFileSize;

	@Value("${cloud.aws.s3.max-request-size}")
	private String maxRequestSize;

	@Bean
	public AmazonS3Client amazonS3Client() {
		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
		return (AmazonS3Client)AmazonS3ClientBuilder
			.standard()
			.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
			.withRegion(region)
			.build();
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.parse(maxFileSize));
		factory.setMaxRequestSize(DataSize.parse(maxRequestSize));
		return factory.createMultipartConfig();
	}

}
