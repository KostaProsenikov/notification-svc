package app.web;

import app.model.Notification;
import app.model.NotificationPreference;
import app.service.NotificationService;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationRequest;
import app.web.dto.NotificationResponse;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping ("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> upsertNotificationPreferences(@RequestBody UpsertNotificationPreference upsertNotificationPreference) {

        NotificationPreference notificationPreference = notificationService.upsertPreference(upsertNotificationPreference);
        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getUserNotificationPreference(@RequestParam(name = "userId") UUID userId) {
       Optional<NotificationPreference> notificationPreference = notificationService.getPreferenceByUserId(userId);
        NotificationPreferenceResponse responseDto = null;
        if (notificationPreference.orElse(null) != null) {
            responseDto = DtoMapper.fromNotificationPreference(notificationPreference.orElse(null));
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping()
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest notificationRequest) {

//        Entity
        Notification notification = notificationService.sendNotification(notificationRequest);

//        DTO
        NotificationResponse response = DtoMapper.fromNotification(notification);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updateNotificationPreferences(@RequestParam(name = "userId") UUID userId, @RequestParam(name = "enabled") boolean enabled) {
        NotificationPreference notificationPreference = notificationService.changeNotificationPreference(userId, enabled);
        if (notificationPreference == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationHistory(@RequestParam(name = "userId") UUID userId) {
        List<NotificationResponse> notificationHistory = notificationService.getNotificationHistory(userId).stream().map(DtoMapper::fromNotification).toList();
        return ResponseEntity.status(HttpStatus.OK).body(notificationHistory);
    }

//    DELETE /api/v1/notifications
    @DeleteMapping("")
    public ResponseEntity<Void> clearNotificationHistory(@RequestParam(name = "userId") UUID userId) {
        notificationService.clearNotifications(userId);
        return ResponseEntity.ok().body(null);
    }

    @PutMapping()
    public ResponseEntity<Void> retryFailedNotifications(@RequestParam(name = "userId") UUID userId) {
        Notification notification = notificationService.retryFailedNotifications(userId);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        NotificationResponse responseDto = DtoMapper.fromNotification(notification);
        return ResponseEntity.ok().body(null);
    }
}
