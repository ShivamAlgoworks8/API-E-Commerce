
package in.algoworks.service;

import in.algoworks.dto.NotificationRequestDTO;
import in.algoworks.entity.Notification;
import in.algoworks.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(NotificationRequestDTO request) {
        Notification notification = new Notification();

        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());

        if (request.getStatus() != null) {
            notification.setStatus(request.getStatus());
        }

        if (request.getRead() != null) {
            notification.setRead(request.getRead());
        }

        if (request.getDate() != null) {
            notification.setDate(request.getDate());
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
            String id, NotificationRequestDTO updatedData) {

        return notificationRepository.findById(id).map(existing -> {
            existing.setTitle(updatedData.getTitle());
            existing.setMessage(updatedData.getMessage());
            existing.setType(updatedData.getType());

            if (updatedData.getRead() != null) {
                existing.setRead(updatedData.getRead());
            }

            if (updatedData.getStatus() != null) {
                existing.setStatus(updatedData.getStatus());
            }

            if (updatedData.getDate() != null) {
                existing.setDate(updatedData.getDate());
            }

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
