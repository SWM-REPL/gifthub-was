package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.util.ProductNameProcessor;
import org.swmaestro.repl.gifthub.util.QueryTemplateReader;
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
	private final QueryTemplateReader queryTemplateReader;
	private final ProductNameProcessor productNameProcessor;

	public void execute(OCRDto ocrDto, String username) throws IOException {
		handleGptResponse(ocrDto, username)
				.flatMap(voucherSaveRequestDto -> handleSearchResponse(voucherSaveRequestDto, username))
				.flatMap(voucherSaveRequestDto -> handleVoucherSaving(voucherSaveRequestDto, username))
				.subscribe(
						// onSuccess
						aVoid -> {
							fcmNotificationService.sendNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
						},
						// onError
						throwable -> {
							throwable.printStackTrace();
							fcmNotificationService.sendNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
						});
	}

	public Mono<VoucherSaveRequestDto> handleGptResponse(OCRDto ocrDto, String username) throws IOException {
		return gptService.getGptResponse(ocrDto).flatMap(response -> {
			try {
				VoucherSaveRequestDto voucherSaveRequestDto = createVoucherSaveRequestDto(response);
				System.out.println("GPT response");
				System.out.println(voucherSaveRequestDto.getBrandName());
				System.out.println(voucherSaveRequestDto.getProductName());
				return Mono.just(voucherSaveRequestDto);
			} catch (JsonProcessingException e) {
				fcmNotificationService.sendNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
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

	public Mono<Void> handleVoucherSaving(VoucherSaveRequestDto voucherSaveRequestDto, String username) {
		return Mono.fromCallable(() -> {
			try {
				voucherService.save(username, voucherSaveRequestDto);
				fcmNotificationService.sendNotification("기프티콘 등록 성공", "기프티콘 등록에 성공했습니다!", username);
				return null;
			} catch (IOException e) {
				fcmNotificationService.sendNotification("기프티콘 등록 실패", "기프티콘 등록에 실패했습니다.", username);
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
		System.out.println("voucherSaveRequestDto.getproductname() = " + voucherSaveRequestDto.getProductName());
		String queryTemplate = queryTemplateReader.readQueryTemplate();
		return String.format(queryTemplate,
				voucherSaveRequestDto.getBrandName(),
				voucherSaveRequestDto.getProductName());
	}

	private VoucherSaveRequestDto preprocessing(VoucherSaveRequestDto voucherSaveRequestDto) {
		Pattern pattern = Pattern.compile("[\\d,]+(?=원)");
		String productName = voucherSaveRequestDto.getProductName();
		Matcher matcher = pattern.matcher(productName);

		while (matcher.find()) {
			String match = matcher.group();
			int number = Integer.parseInt(match.replace(",", ""));

			String koreanNumber = convertNumberToKorean(number);

			productName = productName.replace(match, koreanNumber);
			voucherSaveRequestDto.setProductName(productName);
		}
		return voucherSaveRequestDto;
	}

	private String convertNumberToKorean(int number) {
		Map<Integer, String> koreanUnits = new HashMap<>();
		koreanUnits.put(100, "백");
		koreanUnits.put(1000, "천");
		koreanUnits.put(10000, "만");

		String result = "";
		int start = 1000;
		int x = number / start;
		int y = number % start;

		if (x >= 10) {
			start *= 10;
			x = number / start;
			y = number % start;
		}

		if (String.valueOf(y).charAt(0) == '0') {
			result = x + koreanUnits.get(start);
		} else {
			result = x + koreanUnits.get(start) + String.valueOf(y).charAt(0) + koreanUnits.get(start / 10);
		}
		return result;
	}
}
