package app.web;

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

        notificationService.sendMail(notificationRequest);
        return null;
    }
}
