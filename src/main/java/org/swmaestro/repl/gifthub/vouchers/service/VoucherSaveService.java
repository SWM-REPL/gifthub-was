package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.dto.NoticeNotificationDto;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.OCRDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VoucherSaveService {
	private final GptService gptService;
	private final SearchService searchService;
	private final VoucherService voucherService;
	private final ObjectMapper objectMapper;
	private final FCMNotificationService fcmNotificationService;

	public void execute(OCRDto ocrDto, String username) {
		handleGptResponse(ocrDto, username)
				.flatMap(voucherSaveRequestDto -> handleSearchResponse(voucherSaveRequestDto, username))
				.flatMap(voucherSaveRequestDto -> handleVoucherSaving(voucherSaveRequestDto, username))
				.subscribe(
						// onSuccess
						aVoid -> {
							// logic for successful completion, if
							sendSuccessNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
						},
						// onError
						throwable -> {
							throwable.printStackTrace();
							sendFailureNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
							// logic for error handling, if needed
						}
				);
	}

	public Mono<VoucherSaveRequestDto> handleGptResponse(OCRDto ocrDto, String username) {
		return gptService.getGptResponse(ocrDto)
				.flatMap(response -> {
					try {
						VoucherSaveRequestDto voucherSaveRequestDto = createVoucherSaveRequestDto(response);
						return Mono.just(voucherSaveRequestDto);
					} catch (JsonProcessingException e) {
						sendFailureNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
						return Mono.error(new BusinessException("GPT 응답 에러", StatusEnum.NOT_FOUND));
					}
				});
	}

	public Mono<VoucherSaveRequestDto> handleSearchResponse(VoucherSaveRequestDto voucherSaveRequestDto, String username) {
		return searchService.search(createQuery(voucherSaveRequestDto))
				.flatMap(searchResponseDto -> {
					try {
						String brandName = searchResponseDto.getHits().getHitsList().get(0).getSource().getBrandName();
						String productName = searchResponseDto.getHits().getHitsList().get(0).getSource().getProductName();
						voucherSaveRequestDto.setBrandName(brandName);
						voucherSaveRequestDto.setProductName(productName);
						return Mono.just(voucherSaveRequestDto);
					} catch (Exception e) {
						sendFailureNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
						return Mono.error(new BusinessException("Elasticsearch 응  에러", StatusEnum.NOT_FOUND));
					}
				});
	}

	public Mono<Void> handleVoucherSaving(VoucherSaveRequestDto updatedVoucherSaveRequestDto, String username) {
		return Mono.fromCallable(() -> {
			try {
				voucherService.save(username, updatedVoucherSaveRequestDto);
				sendSuccessNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
				return null;
			} catch (IOException e) {
				sendFailureNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
				throw new RuntimeException(e);
			}
		});
	}

	private void sendFailureNotification(String title, String body, String username) {
		NoticeNotificationDto noticeNotificationDto = NoticeNotificationDto.builder()
				.title(title)
				.body(body)
				.build();
		fcmNotificationService.sendNotification(noticeNotificationDto, username);
	}

	private void sendSuccessNotification(String title, String body, String username) {
		NoticeNotificationDto noticeNotificationDto = NoticeNotificationDto.builder()
				.title(title)
				.body(body)
				.build();
		fcmNotificationService.sendNotification(noticeNotificationDto, username);
	}

	private VoucherSaveRequestDto createVoucherSaveRequestDto(GptResponseDto gptResponseDto) throws JsonProcessingException {
		String contentString = gptResponseDto.getChoices().get(0).getMessage().getContent();
		VoucherSaveRequestDto voucherSaveRequestDto = objectMapper.readValue(contentString, VoucherSaveRequestDto.class);
		return voucherSaveRequestDto;
	}

	private String createQuery(VoucherSaveRequestDto voucherSaveRequestDto) {
		return String.format("{\n" + "  \"query\": {\n" + "    \"bool\": {\n" + "      \"should\": [\n" + "        {\"match\": {\"brand_name\": \"%s\"}},\n"
						+ "        {\"match\": {\"product_name\": \"%s\"}}\n" + "      ]\n" + "    }\n" + "  }\n" + "}",
				voucherSaveRequestDto.getBrandName(),
				voucherSaveRequestDto.getProductName());
	}
}