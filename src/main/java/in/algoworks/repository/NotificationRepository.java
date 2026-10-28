package in.algoworks.repository;

import in.algoworks.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository
        extends MongoRepository<Notification, String> {
}