package org.swmaestro.repl.gifthub.notifications.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

class VoucherExpirationItemReaderTest {
    @Mock
    private VoucherService voucherService;

    private VoucherExpirationItemReader reader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reader = new VoucherExpirationItemReader(voucherService);
    }

    @Test
    void shouldReturnExpiringVouchers() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        Brand brand = Brand.builder()
                .id(1L)
                .name("brand")
                .build();

        User user = User.builder()
                .id(1L)
                .username("user")
                .build();

        List<Voucher> vouchers = new ArrayList<>();

        // 오늘 만료되는 바우처
        Voucher voucher1 = Voucher.builder()
                .expiresAt(today)
                .brand(brand)
                .user(user)
                .build();
        vouchers.add(voucher1);

        // 3일 후 만료되는 바우처
        Voucher voucher2 = Voucher.builder()
                .expiresAt(today.plusDays(3))
                .brand(brand)
                .user(user)
                .build();
        vouchers.add(voucher2);

        // 4일 후 만료되는 바우처 (대상 아님)
        Voucher voucher3 = Voucher.builder()
                .expiresAt(today.plusDays(4))
                .brand(brand)
                .user(user)
                .build();
        vouchers.add(voucher3);

        when(voucherService.list()).thenReturn(vouchers);

        // When & Then
        assertEquals(voucher1, reader.read());
        assertEquals(voucher2, reader.read());
        assertNull(reader.read()); // 3번째 바우처는 필터링되어야 함

        verify(voucherService, times(1)).list();
    }
}