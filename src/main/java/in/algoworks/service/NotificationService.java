package in.algoworks.service;

import in.algoworks.entity.Notification;
import in.algoworks.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {
        notification.setId(null);

        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(String id) {
        return notificationRepository.findById(id);
    }

    public Optional<Notification> updateNotification(
            String id, Notification updatedData) {

        return notificationRepository.findById(id).map(existing -> {
            existing.setTitle(updatedData.getTitle());
            existing.setMessage(updatedData.getMessage());
            existing.setType(updatedData.getType());
            existing.setStatus(updatedData.getStatus());
            existing.setDate(updatedData.getDate());

            return notificationRepository.save(existing);
        });
    }

    public Optional<Notification> markAsRead(String id) {
        return notificationRepository.findById(id).map(notification -> {
            notification.setRead(true);
            return notificationRepository.save(notification);
        });
    }

    public boolean deleteNotification(String id) {
        if (!notificationRepository.existsById(id)) {
            return false;
        }

        notificationRepository.deleteById(id);
        return true;
    }
}