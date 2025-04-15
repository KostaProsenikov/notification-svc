package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.repository.NotificationPreferenceRepository;
import app.repository.NotificationRepository;
import app.web.dto.NotificationRequest;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
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

      // upsert
      // 1. Try to find if such exists in the DB
      // 2. If exists - update it
      // 3. If it does not exist - just create a new record in DB

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
                   .isEnabled(dto.isNotificationEnabled())
                   .createdOn(LocalDateTime.now())
                   .updatedOn(LocalDateTime.now())
                   .build();
       return  preferenceRepository.save(preference);
    }

    public Optional<NotificationPreference> getPreferenceByUserId(UUID userId) {
        return Optional.ofNullable(preferenceRepository.findByUserId(userId).orElseThrow(() -> new NullPointerException("Notification preference for user id [%s] was not found!".formatted(userId))));
    }

    public Notification sendMail(NotificationRequest notificationRequest) {

        UUID userId = notificationRequest.getUserId();
        Optional<NotificationPreference> userPreference = getPreferenceByUserId(userId);

        if (userPreference.isPresent() && !userPreference.get().isEnabled()) {
            throw new IllegalArgumentException("User with id [%s] does not allow to receive notifications".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.get().getContactInfo());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getBody());

        Notification notification = Notification
                .builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .isDeleted(false)
                .build();

        try {
            mailSender.send(message);
        }   catch (Exception e) {
            e.printStackTrace();
        }

        return notificationRepository.save(notification);
    }
}
