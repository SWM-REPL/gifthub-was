package org.swmaestro.repl.gifthub.notifications.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.auth.entity.Device;
import org.swmaestro.repl.gifthub.auth.service.DeviceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final JobLauncher jobLauncher;
    private final Job voucherExpirationNotificationJob;
    private final DeviceService deviceService;

    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul") // 매일 오전 10시 실행
    public void runVoucherExpirationNotificationJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("currentTime", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(voucherExpirationNotificationJob, jobParameters);
    }

    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul") // 매일 오전 9시 실행
    public void deleteDeviceToken() {
        LocalDateTime now = LocalDateTime.now();

        List<Device> deviceList = deviceService.list();
        for (Device device : deviceList) {
            long daysDifference = ChronoUnit.DAYS.between(now, device.getCreatedAt());

            if (daysDifference > 30) {
                deviceService.delete(device.getId());
            }
        }
    }
}
