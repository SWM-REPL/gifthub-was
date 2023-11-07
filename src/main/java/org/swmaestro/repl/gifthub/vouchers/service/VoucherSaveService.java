package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.GptResponseException;
import org.swmaestro.repl.gifthub.exception.GptTimeoutException;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.ProductNameProcessor;
import org.swmaestro.repl.gifthub.util.QueryTemplateReader;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherAutoSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sentry.Sentry;
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

	public void execute(VoucherAutoSaveRequestDto voucherAutoSaveRequestDto, String username, Long pendingId) throws IOException {
		String filename = voucherAutoSaveRequestDto.getFilename();
		handleGptResponse(voucherAutoSaveRequestDto, username).flatMap(voucherSaveRequestDto -> handleSearchResponse(voucherSaveRequestDto, username, filename))
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
										NotificationType.REGISTERED, "만료된 기프티콘을 등록했습니다.");
							} else {
								fcmNotificationService.sendNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
								notificationService.save(userService.read(username), voucherService.read(voucherSaveResponseDto.getId()),
										NotificationType.REGISTERED, "기프티콘 등록에 성공했습니다.");
							}
						},
						// onError
						throwable -> {
							System.out.println("등록 실패");
							Sentry.captureException(throwable);
							// 처리 완료
							pendingVoucherService.delete(pendingId);
							throwable.printStackTrace();
							if (throwable instanceof BusinessException) {
								fcmNotificationService.sendNotification("기프티콘 등록 실패", "이미 등록된 기프티콘 입니다.", username);
								notificationService.save(userService.read(username), null, NotificationType.REGISTERED, "이미 등록된 기프티콘 입니다.");
							} else {
								fcmNotificationService.sendNotification("기프티콘 등록 실패", "자동 등록에 실패했습니다. 수동 등록을 이용해 주세요.", username);
								notificationService.save(userService.read(username), null, NotificationType.REGISTERED, "자동 등록에 실패했습니다. 수동 등록을 이용해 주세요.");
							}
						});
	}

	public Mono<VoucherSaveRequestDto> handleGptResponse(VoucherAutoSaveRequestDto voucherAutoSaveRequestDto, String username) throws
			GptResponseException,
			GptTimeoutException {

		return gptService.getGptResponse(voucherAutoSaveRequestDto)
				.timeout(Duration.ofMinutes(15))
				.onErrorResume(GptTimeoutException.class, throwable -> Mono.error(new GptTimeoutException()))
				.flatMap(response -> {
					VoucherSaveRequestDto voucherSaveRequestDto = null;
					try {
						voucherSaveRequestDto = createVoucherSaveRequestDto(response);
					} catch (JsonProcessingException e) {
						return Mono.error(new RuntimeException(e));
					}
					if (voucherSaveRequestDto.getBrandName() == "" || voucherSaveRequestDto.getProductName() == "" || voucherSaveRequestDto.getBarcode() == ""
							|| voucherSaveRequestDto.getExpiresAt() == "") {
						return Mono.error(new GptResponseException());
					}
					return Mono.just(voucherSaveRequestDto);
				});
	}

	public Mono<VoucherSaveRequestDto> handleSearchResponse(VoucherSaveRequestDto voucherSaveRequestDto, String username, String filename) {
		return searchService.search(createQuery(productNameProcessor.preprocessing(voucherSaveRequestDto))).flatMap(searchResponseDto -> {
			try {
				String brandName = searchResponseDto.getHits().getHitsList().get(0).getSource().getBrandName();
				String productName = searchResponseDto.getHits().getHitsList().get(0).getSource().getProductName();
				voucherSaveRequestDto.setBrandName(brandName);
				voucherSaveRequestDto.setProductName(productName);
				voucherSaveRequestDto.setImageUrl(filename);
				System.out.println("Search response");
				System.out.println(brandName);
				System.out.println(productName);
				System.out.println(filename);
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
		return String.format(queryTemplate, voucherSaveRequestDto.getBrandName(), voucherSaveRequestDto.getProductName());
	}

}
