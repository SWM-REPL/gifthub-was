package org.swmaestro.repl.gifthub.vouchers.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class S3FileDto {
	private String originalFileName;
	private String uploadFileName;
	private String uploadFilePath;
	private String uploadFileUrl;
}
