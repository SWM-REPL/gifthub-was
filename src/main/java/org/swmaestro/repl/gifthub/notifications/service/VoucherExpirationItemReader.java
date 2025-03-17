package org.swmaestro.repl.gifthub.notifications.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import lombok.RequiredArgsConstructor;

/***
 * 만료 예정인 모바일 상품권을 읽어오는 ItemReader 구현체
 * 현재 날짜 기준으로 3일 이내 만료 예정인 상품권을 조회
 */
@Component
@RequiredArgsConstructor
public class VoucherExpirationItemReader implements ItemReader<Voucher> {
    private final VoucherService voucherService;
    private Iterator<Voucher> voucherIterator;
    private boolean initialized = false;

    /***
     * 모바일 상품권을 한 개씩 읽어오는 메서드
     * 초기화되지 않았다면 초기화 진행 후 모바일 상품권 순차적 반환
     * 더 이상 반환할 상품권이 없다면 null 반환 (배치 작업 종료)
     * @return 다음 처리할 모바일 상품권 혹은 null
     * @throws Exception
     */
    @Override
    public Voucher read() throws Exception {
        if (!initialized) {
            initialize();
        }

        if (voucherIterator != null && voucherIterator.hasNext()) {
            return voucherIterator.next();
        }

        // 데이터 종료
        return null;
    }

    /***
     * 만료 예정인 모바일 상품권 목록을 초기화하는 메서드
     * 오늘 기준으로 3일 이내 만료 예정인 상품권만 필터링
     */
    private void initialize() {
        LocalDate today = LocalDate.now();
        List<Voucher> expiringVouchers = voucherService.list().stream()
                .filter(voucher -> {
                    long daysDifference = ChronoUnit.DAYS.between(today, voucher.getExpiresAt());
                    return daysDifference <= 3 && daysDifference >= 0;
                })
                .toList();

        voucherIterator = expiringVouchers.iterator();
        initialized = true;
    }
}
