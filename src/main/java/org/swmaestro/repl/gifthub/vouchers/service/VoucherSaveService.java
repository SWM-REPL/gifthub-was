package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.exception.GptResponseException;
import org.swmaestro.repl.gifthub.exception.TimeoutException;
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
		Long pendingId = pendingVoucherService.create(userService.read(username));
		handleGptResponse(ocrDto, username)
				.flatMap(voucherSaveRequestDto -> handleSearchResponse(voucherSaveRequestDto, username))
				.flatMap(voucherSaveRequestDto -> handleVoucherSaving(voucherSaveRequestDto, username))
				.subscribe(
						// onSuccess
						voucherSaveResponseDto -> {
							System.out.println("등록 성공");
							// 처리 완료
							pendingVoucherService.delete(pendingId);
							// 만료된 기프티콘을 등록할 경우
							if (voucherService.read(voucherSaveResponseDto.getId()).getExpiresAt().isBefore(LocalDate.now())) {
								fcmNotificationService.sendNotification("기프티콘 등록 성공", "만료된 기프티콘을 등록했습니다.", username);
								notificationService.save(userService.read(username), voucherService.read(voucherSaveResponseDto.getId()),
										NotificationType.REGISTERED,
										"만료된 기프티콘을 등록했습니다.");
							} else {
								fcmNotificationService.sendNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
								notificationService.save(userService.read(username), voucherService.read(voucherSaveResponseDto.getId()),
										NotificationType.REGISTERED,
										"기프티콘 등록에 성공했습니다.");
							}
						},
						// onError
						throwable -> {
							System.out.println("등록 실패");
							// 처리 완료
							pendingVoucherService.delete(pendingId);
							throwable.printStackTrace();
							// 15초 이상 응답이 없을 경우
							if (throwable instanceof TimeoutException) {
								fcmNotificationService.sendNotification("기프티콘 등록 실패", "자동 등록에 실패했습니다. 다시 시도해 주세요", username);
								notificationService.save(userService.read(username), null,
										NotificationType.REGISTERED,
										"GPT 요청이 시간초과되었습니다.");
							}
							//Gpt 에러일 경우
							if (throwable instanceof GptResponseException) {
								fcmNotificationService.sendNotification("기프티콘 등록 실패", "자동 등록에 실패했습니다. 수동 등록을 이용해 주세요.", username);
								notificationService.save(userService.read(username), null,
										NotificationType.REGISTERED,
										"자동 등록에 실패했습니다. 수동 등록을 이용해 주세요.");
							} else {
								fcmNotificationService.sendNotification("기프티콘 등록 실패", "이미 등록된 기프티콘 입니다.", username);
								notificationService.save(userService.read(username), null,
										NotificationType.REGISTERED,
										"이미 등록된 기프티콘 입니다.");
							}
						});
	}

	public Mono<VoucherSaveRequestDto> handleGptResponse(OCRDto ocrDto, String username) throws IOException, GptResponseException, TimeoutException {
		return gptService.getGptResponse(ocrDto)
				.timeout(Duration.ofSeconds(15))
				.onErrorResume(TimeoutException.class, throwable -> Mono.error(new TimeoutException("GPT 요청이 시간초과되었습니다.", StatusEnum.NOT_FOUND)))
				.flatMap(response -> {
					try {
						VoucherSaveRequestDto voucherSaveRequestDto = createVoucherSaveRequestDto(response);
						if (voucherSaveRequestDto.getBrandName() == "" ||
								voucherSaveRequestDto.getProductName() == "" ||
								voucherSaveRequestDto.getBarcode() == "" ||
								voucherSaveRequestDto.getExpiresAt() == "") {
							throw new GptResponseException("GPT 응답이 올바르지 않습니다.", StatusEnum.NOT_FOUND);
						}
						return Mono.just(voucherSaveRequestDto);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						return Mono.error(new GptResponseException("GPT 응답이 올바르지 않습니다.", StatusEnum.NOT_FOUND));
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
