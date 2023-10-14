package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.ProductNameProcessor;
import org.swmaestro.repl.gifthub.util.QueryTemplateReader;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.OCRDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;

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
	private final QueryTemplateReader queryTemplateReader;
	private final ProductNameProcessor productNameProcessor;
	private final NotificationService notificationService;
	private final UserService userService;
	private final PendingVoucherService pendingVoucherService;

	public void execute(OCRDto ocrDto, String username) throws IOException {
		pendingVoucherService.create(userService.read(username));
		handleGptResponse(ocrDto, username)
				.flatMap(voucherSaveRequestDto -> handleSearchResponse(voucherSaveRequestDto, username))
				.flatMap(voucherSaveRequestDto -> handleVoucherSaving(voucherSaveRequestDto, username))
				.subscribe(
						// onSuccess
						voucherSaveResponseDto -> {
							System.out.println("등록 성공");
							//notification 저장(알림 성공 저장)
							notificationService.save(userService.read(username), voucherService.read(voucherSaveResponseDto.getId()),
									NotificationType.REGISTERED,
									"기프티콘 등록에 성공했습니다.");
							fcmNotificationService.sendNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
							// 처리 완료
							pendingVoucherService.delete(userService.read(username));
						},
						// onError
						throwable -> {
							System.out.println("등록 실패");
							throwable.printStackTrace();
							// notification 저장(알림 실패 저장)
							notificationService.save(userService.read(username), null,
									NotificationType.REGISTERED,
									"기프티콘 등록에 실패했습니다.");
							fcmNotificationService.sendNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
							// 처리 완료
							pendingVoucherService.delete(userService.read(username));
						});
	}

	public Mono<VoucherSaveRequestDto> handleGptResponse(OCRDto ocrDto, String username) throws IOException {

		return gptService.getGptResponse(ocrDto).flatMap(response -> {
			try {
				VoucherSaveRequestDto voucherSaveRequestDto = createVoucherSaveRequestDto(response);
				System.out.println("GPT response");
				System.out.println(voucherSaveRequestDto.getBrandName());
				System.out.println(voucherSaveRequestDto.getProductName());

				if (voucherSaveRequestDto.getBrandName() == "" || voucherSaveRequestDto.getProductName() == "") {
					throw new BusinessException("GPT 응답이 올바르지 않습니다.", StatusEnum.NOT_FOUND);
				}
				return Mono.just(voucherSaveRequestDto);
			} catch (JsonProcessingException e) {
				return Mono.error(new BusinessException("GPT 응답이 올바르지 않습니다.", StatusEnum.NOT_FOUND));
			}
		});
	}

	public Mono<VoucherSaveRequestDto> handleSearchResponse(VoucherSaveRequestDto voucherSaveRequestDto, String username) {
		return searchService.search(createQuery(productNameProcessor.preprocessing(voucherSaveRequestDto))).flatMap(searchResponseDto -> {
			try {
				String brandName = searchResponseDto.getHits().getHitsList().get(0).getSource().getBrandName();
				String productName = searchResponseDto.getHits().getHitsList().get(0).getSource().getProductName();
				voucherSaveRequestDto.setBrandName(brandName);
				voucherSaveRequestDto.setProductName(productName);
				System.out.println("Search response");
				System.out.println(brandName);
				System.out.println(productName);
				return Mono.just(voucherSaveRequestDto);
			} catch (Exception e) {
				e.printStackTrace();
				return Mono.just(voucherSaveRequestDto);
			}
		});
	}

	public Mono<VoucherSaveResponseDto> handleVoucherSaving(VoucherSaveRequestDto voucherSaveRequestDto, String username) {
		return Mono.fromCallable(() -> {
			try {
				return voucherService.save(username, voucherSaveRequestDto);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private VoucherSaveRequestDto createVoucherSaveRequestDto(GptResponseDto gptResponseDto) throws JsonProcessingException {
		String contentString = gptResponseDto.getChoices().get(0).getMessage().getContent();
		VoucherSaveRequestDto voucherSaveRequestDto = objectMapper.readValue(contentString, VoucherSaveRequestDto.class);
		return voucherSaveRequestDto;
	}

	private String createQuery(VoucherSaveRequestDto voucherSaveRequestDto) {
		String queryTemplate = queryTemplateReader.readQueryTemplate();
		return String.format(queryTemplate,
				voucherSaveRequestDto.getBrandName(),
				voucherSaveRequestDto.getProductName());
	}

}
