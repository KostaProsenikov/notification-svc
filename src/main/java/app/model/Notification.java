package app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private String subject;

    @Column (nullable = false)
    private String body;

    @Column (nullable = false, name="created_on")
    private LocalDateTime createdOn;

    @Column (nullable = false)
    @Enumerated (EnumType.STRING)
    private NotificationType type;

    @Column (nullable = false)
    @Enumerated (EnumType.STRING)
    private NotificationStatus status;

    private UUID userId;

    private boolean isDeleted;
}
