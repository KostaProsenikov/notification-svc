package app.web.mapper;

import app.model.NotificationType;
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
}
