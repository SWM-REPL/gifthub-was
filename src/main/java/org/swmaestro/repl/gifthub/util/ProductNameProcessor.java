package org.swmaestro.repl.gifthub.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;

@Service
public class ProductNameProcessor {

	private static final Pattern AMOUNT_PATTERN = Pattern.compile("[\\d,]+(?=원)");

	public VoucherSaveRequestDto preprocessing(VoucherSaveRequestDto voucherSaveRequestDto) {
		String productName = voucherSaveRequestDto.getProductName();
		Matcher matcher = AMOUNT_PATTERN.matcher(productName);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String match = matcher.group();
			int number = Integer.parseInt(match.replace(",", ""));
			String koreanNumber = convertNumberToKorean(number);
			matcher.appendReplacement(sb, koreanNumber);
		}
		matcher.appendTail(sb);

		voucherSaveRequestDto.setProductName(sb.toString());
		return voucherSaveRequestDto;
	}

	private String convertNumberToKorean(int number) {
		String[] units = {"", "십", "백", "천", "만", "십만", "백만", "천만"};

		StringBuilder result = new StringBuilder();
		String numStr = String.valueOf(number);
		int length = numStr.length();

		for (int i = 0; i < length; i++) {
			int digit = Integer.parseInt(Character.toString(numStr.charAt(i)));
			if (digit != 0) {
				result.append(digit).append(units[length - i - 1]);
			}
		}

		return result.toString();
	}
}
