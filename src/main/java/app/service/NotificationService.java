package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationPreferenceRepository;
import app.repository.NotificationRepository;
import app.web.dto.NotificationRequest;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final MailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationPreferenceRepository preferenceRepository, MailSender mailSender, NotificationRepository notificationRepository) {
        this.preferenceRepository = preferenceRepository;
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference dto) {

      // Upsert
      // 1. Try to find if such exists in DB
      // 2. If it exists - update it
      // 3. If it does not exist - create a new record in DB

       Optional<NotificationPreference> userNotificationPreferenceOptional = preferenceRepository.findByUserId(dto.getUserId());
       if (userNotificationPreferenceOptional.isPresent()) {
           NotificationPreference preference = userNotificationPreferenceOptional.get();
            preference.setEnabled(dto.isNotificationEnabled());
            preference.setContactInfo(dto.getContactInfo());
            preference.setType(DtoMapper.fromNotificationTypeRequest(dto.getType()));
            preference.setUpdatedOn(LocalDateTime.now());
            return preferenceRepository.save(preference);
       }
       NotificationPreference preference = NotificationPreference.builder()
                   .userId(dto.getUserId())
                   .type(DtoMapper.fromNotificationTypeRequest(dto.getType()))
                   .contactInfo(dto.getContactInfo())
                   .enabled(dto.isNotificationEnabled())
                   .createdOn(LocalDateTime.now())
                   .updatedOn(LocalDateTime.now())
                   .build();

       NotificationPreference notificationPreference = preferenceRepository.save(preference);
       System.out.printf("NotificationPreference was saved for user with id [%s] \n", dto.getUserId());
       return notificationPreference;
    }

    public Optional<NotificationPreference> getPreferenceByUserId(UUID userId) {
        try {
            return preferenceRepository.findByUserId(userId);
        } catch (NullPointerException e) {
            log.warn("Notification preference for user id [%s] was not found!".formatted(userId));
        }
        return Optional.empty();
    }

    public Notification sendNotification(NotificationRequest notificationRequest) {

        UUID userId = notificationRequest.getUserId();
        Optional<NotificationPreference> userPreference = getPreferenceByUserId(userId);

        Notification notification = Notification
                .builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .userId(userId)
                .type(NotificationType.EMAIL)
                .createdOn(LocalDateTime.now())
                .isDeleted(false)
                .build();

        if (userPreference.isPresent() && !userPreference.get().isEnabled()) {
            System.out.printf("User with id [%s] does not allow to receive notifications".formatted(userId));
            notification.setStatus(NotificationStatus.DISABLED);
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            if (userPreference.isPresent()) {
                message.setTo(userPreference.get().getContactInfo());
                message.setSubject(notificationRequest.getSubject());
                message.setText(notificationRequest.getBody());
            }

            try {
                mailSender.send(message);
                notification.setStatus(NotificationStatus.SUCCEEDED);
            }   catch (Exception e) {
                if (userPreference.isPresent()) {
                    log.warn("There was an issue sending an email to %s due to [%s]".formatted(userPreference.get().getContactInfo(), e.getMessage()));
                    notification.setStatus(NotificationStatus.FAILED);
                }
            }
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        return  notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);
    }

    public NotificationPreference changeNotificationPreference(UUID userId, boolean enabled) {
        NotificationPreference preference = getPreferenceByUserId(userId).orElse(null);
        if (preference != null) {
            preference.setEnabled(enabled);
            return preferenceRepository.save(preference);
        }
        return null;
    }

    public void clearNotifications(UUID userId) {
        List<Notification> notifications = getNotificationHistory(userId);

        for (Notification notification : notifications) {
            notification.setDeleted(true);
            notificationRepository.save(notification);
            System.out.printf("Notification with id [%s] was deleted for user with id [%s] \n", notification.getId(), userId);
        }

        System.out.printf("All notifications for user with id [%s] were deleted \n", userId);
    }

    public Notification retryFailedNotifications(UUID userId) {

        Optional<NotificationPreference> preference = getPreferenceByUserId(userId);

        if (preference.isEmpty()) {
            log.warn("Notification preference for user id [%s] was not found!".formatted(userId));
            throw new IllegalArgumentException("Notification preference for user id [%s] was not found!".formatted(userId));
        }
        if (!preference.get().isEnabled()) {
            System.out.printf("User with id [%s] does not allow to receive notifications".formatted(userId));
            throw new IllegalArgumentException("User does not allow to receive notifications");
        }

        List<Notification> notifications = notificationRepository.findAllByUserIdAndStatus(userId, NotificationStatus.FAILED);
        List<Notification> failedNotifications = notifications.stream().filter(i -> !i.isDeleted()).toList();
        for (Notification notification : failedNotifications) {
            SimpleMailMessage message = new SimpleMailMessage();
            Optional<NotificationPreference> userPreference = getPreferenceByUserId(userId);
            if (userPreference.isPresent()) {
                message.setTo(userPreference.get().getContactInfo());
                message.setSubject(notification.getSubject());
                message.setText(notification.getBody());
            }

            try {
                mailSender.send(message);
                notification.setStatus(NotificationStatus.SUCCEEDED);
            } catch (Exception e) {
                if (userPreference.isPresent()) {
                    log.warn("There was an issue sending an email to %s due to [%s]".formatted(userPreference.get().getContactInfo(), e.getMessage()));
                    notification.setStatus(NotificationStatus.FAILED);
                }
            }
            return notificationRepository.save(notification);
        }

        return null;
    }
}
