package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.Device;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.DeviceService;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.notifications.dto.NoticeNotificationDto;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMNotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final DeviceService deviceService;
    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 단일 사용자에게 알림 전송
     * @param requestDto 알림 요청 정보
     */
    public void sendNotificationByToken(FCMNotificationRequestDto requestDto) {
        Long userId = requestDto.getTargetUser().getId();
        List<Device> deviceList = deviceService.list(userId);

        // 빈 디바이스 목록 검사
        if (deviceList.isEmpty()) {
            log.info("사용자 ID {}에 대한 등록된 디바이스가 없습니다.", userId);
            return;
        }

        // 알림 정보 저장
        org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
                = notificationService.save(requestDto.getTargetUser(), requestDto.getTargetVoucher(), NotificationType.EXPIRATION, requestDto.getBody());

        // 각 디바이스에 알림 전송
        for (Device device : deviceList) {
            if (device.getFcmToken() == null) {
                continue;
            }
            Notification notification = Notification.builder()
                    .setTitle(requestDto.getTitle())
                    .setBody(requestDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(device.getFcmToken())
                    .setNotification(notification)
                    .putData("notification_id", savedNotification.getId().toString())
                    .build();

            try {
                firebaseMessaging.send(message);
                log.debug("알림 전송 성공, 사용자 ID: {}, 디바이스: {}", userId, device.getId());
            } catch (FirebaseMessagingException e) {
                log.error("알림 전송 실패, 사용자 ID: {}, 디바이스 ID: {}, 오류: {}", userId, device.getId(), e.getMessage());
                deviceService.delete(userId, device.getDeviceToken());
            }
        }
    }

    /**
     * 모든 회원에게 알림을 보내는 메서드
     * @param noticeNotificationDto
     */
    public void sendNotification(NoticeNotificationDto noticeNotificationDto) {
        List<Device> deviceList = deviceService.list();

        // 빈 디바이스 목록 검사
        if (deviceList.isEmpty()) {
            log.info("등록된 디바이스가 없습니다.");
            return;
        }

        // 사용자별로 디바이스 그룹화 (멀티스레드 환경에서 안전)
        Map<Long, List<Device>> devicesByUser = deviceList.stream()
                .filter(device -> device.getFcmToken() != null)
                .collect(Collectors.groupingBy(
                        Device::getUserId,
                        ConcurrentHashMap::new,
                        Collectors.toList()
                ));

        // 각 사용자별로 알림 처리 (병렬 처리 가능)
        devicesByUser.forEach((userId, devices) -> {
            User user = userService.readById(userId);
            org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
                    = notificationService.save(user, null, NotificationType.NOTICE, noticeNotificationDto.getBody());

            // 해당 사용자의 모든 디바이스에 알림 전송
            for (Device device : devices) {
                Notification notification = Notification.builder()
                        .setTitle(noticeNotificationDto.getTitle())
                        .setBody(noticeNotificationDto.getBody())
                        .build();

                Message message = Message.builder()
                        .setToken(device.getFcmToken())
                        .setNotification(notification)
                        .putData("notification_id", savedNotification.getId().toString())
                        .build();

                try {
                    firebaseMessaging.send(message);
                    log.debug("알림 전송 성공, 사용자 ID: {}, 디바이스: {}", userId, device.getId());
                } catch (FirebaseMessagingException e) {
                    log.error("알림 전송 실패, 사용자 ID: {}, 디바이스 ID: {}, 오류: {}", userId, device.getId(), e.getMessage());
                    deviceService.delete(device.getId());
                }
            }
        });
    }

    /**
     * title과 body를 받아서 특정 회원에게 알림을 보내는 메서드(username으로 검색)
     */
    public void sendNotification(String title, String body, String username) {
        User user = userService.read(username);

        if (user == null) {
            log.warn("사용자를 찾을 수 없습니다: {}", username);
            return;
        }

        NoticeNotificationDto noticeNotificationDto = NoticeNotificationDto.builder()
                .title(title)
                .body(body)
                .build();

        List<Device> deviceList = deviceService.list(user.getId());

        if (deviceList.isEmpty()) {
            log.info("사용자 {}에 대한 등록된 디바이스가 없습니다.", username);
            return;
        }

        for (Device device : deviceList) {
            if (device.getFcmToken() == null) {
                continue;
            }
            Notification notification = Notification.builder()
                    .setTitle(noticeNotificationDto.getTitle())
                    .setBody(noticeNotificationDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(device.getFcmToken())
                    .setNotification(notification)
                    .putData("notification_type", NotificationType.REGISTERED.toString())
                    .build();

            try {
                firebaseMessaging.send(message);
                log.debug("알림 전송 성공, 사용자: {}, 디바이스: {}", username, device.getId());
            } catch (FirebaseMessagingException e) {
                log.error("알림 전송 실패, 사용자: {}, 디바이스 ID: {}, 오류: {}", username, device.getId(), e.getMessage());
                deviceService.delete(user.getId(), device.getDeviceToken());
            }
        }
    }

    /**
     * 배치 알림 처리용 메서드 (멀티스레딩 지원)
     * @param notificationRequests 알림 요청 목록
     */
    public void sendBatchNotifications(List<? extends FCMNotificationRequestDto> notificationRequests) {
        if (notificationRequests == null || notificationRequests.isEmpty()) {
            return;
        }

        log.info("배치 알림 전송 시작: {} 건", notificationRequests.size());

        // 사용자별로 요청 그룹화 (멀티스레드 환경에서 안전)
        Map<User, List<FCMNotificationRequestDto>> requestsByUser = notificationRequests.stream()
                .collect(Collectors.groupingBy(
                        FCMNotificationRequestDto::getTargetUser,
                        ConcurrentHashMap::new,
                        Collectors.toList()
                ));

        // 각 사용자별로 알림 처리 (병렬 처리 가능)
        requestsByUser.forEach((user, requests) -> {
            // 사용자에게 전송할 알림 내용 구성
            int voucherCount = requests.size();
            String title = "상품권 만료 알림";
            String body = String.format("%d개의 상품권이 곧 만료됩니다. 지금 확인하세요!", voucherCount);

            // 사용자의 디바이스 목록 조회
            List<Device> devices = deviceService.list(user.getId());
            if (devices.isEmpty()) {
                log.info("사용자 ID {}에 대한 등록된 디바이스가 없습니다.", user.getId());
                return;
            }

            // 알림 정보 저장 (첫 번째 바우처 기준)
            org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
                    = notificationService.save(user, requests.get(0).getTargetVoucher(), NotificationType.EXPIRATION, body);

            // 해당 사용자의 모든 디바이스에 알림 전송
            for (Device device : devices) {
                if (device.getFcmToken() == null) {
                    continue;
                }

                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

                Message message = Message.builder()
                        .setToken(device.getFcmToken())
                        .setNotification(notification)
                        .putData("notification_id", savedNotification.getId().toString())
                        .putData("notification_type", NotificationType.EXPIRATION.toString())
                        .putData("voucher_count", String.valueOf(voucherCount))
                        .build();

                try {
                    firebaseMessaging.send(message);
                    log.debug("배치 알림 전송 성공, 사용자 ID: {}, 디바이스: {}", user.getId(), device.getId());
                } catch (FirebaseMessagingException e) {
                    log.error("배치 알림 전송 실패, 사용자 ID: {}, 디바이스 ID: {}, 오류: {}", user.getId(), device.getId(), e.getMessage());
                    deviceService.delete(user.getId(), device.getDeviceToken());
                }
            }
        });

        log.info("배치 알림 전송 완료: {} 건", notificationRequests.size());
    }
}
