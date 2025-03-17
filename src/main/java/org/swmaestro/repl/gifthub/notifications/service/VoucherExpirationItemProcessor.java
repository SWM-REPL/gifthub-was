package org.swmaestro.repl.gifthub.notifications.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

/***
 * 만료 예정 모바일 상품권 ItemProcessor
 * 목적: 모바일 상품권 만료 알림을 위한 ItemProcessor
 */
@Component
public class VoucherExpirationItemProcessor implements ItemProcessor<Voucher, FCMNotificationRequestDto> {

    /***
     * 만료 예정 모바일 상품권 ItemProcessor
     * @param voucher 만료 예정 모바일 상품권
     * @return FCMNotificationRequestDto
     * @throws Exception
     */
    @Override
    public FCMNotificationRequestDto process(Voucher voucher) throws Exception {
        LocalDate today = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(today, voucher.getExpiresAt());

        String message;
        if (daysDifference == 0) {
            message = voucher.getBrand().getName() + "에서 사용할 수 있는 기프티콘이 오늘 만료됩니다.";
        } else {
            message = voucher.getBrand().getName() + "에서 사용할 수 있는 기프티콘 기한이 " + daysDifference + "일 남았습니다.";
        }

        return FCMNotificationRequestDto.builder()
                .targetUser(voucher.getUser())
                .targetVoucher(voucher)
                .title(NotificationType.EXPIRATION.getDescription())
                .body(message)
                .build();
    }
}
