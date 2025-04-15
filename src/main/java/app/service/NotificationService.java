package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.repository.NotificationPreferenceRepository;
import app.web.dto.NotificationRequest;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Autowired
    public NotificationService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
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


        return null;
    }
}
