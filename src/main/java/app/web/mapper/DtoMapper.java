package app.web.mapper;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationResponse;
import app.web.dto.NotificationTypeRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

//    Mapping logic - transfer one type of data to another
    public static NotificationType fromNotificationTypeRequest(NotificationTypeRequest dto) {
        return switch(dto) {
            case EMAIL -> NotificationType.EMAIL;
        };
    }

//    Build dto from entity
    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference entity) {
        return NotificationPreferenceResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .contactInfo(entity.getContactInfo())
                .enabled(entity.isEnabled())
                .notificationType(entity.getType())
                .updatedOn(entity.getUpdatedOn())
                .build();
    }

    public static NotificationResponse fromNotification(Notification entity) {

        return NotificationResponse.builder()
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .type(entity.getType())
                .build();
    }
}
