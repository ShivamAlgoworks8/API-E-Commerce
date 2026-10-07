package in.algoworks.repository;

import in.algoworks.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

// Repository handles database operations for Order
// MongoRepository already provides CRUD methods
public interface OrderRepository extends MongoRepository<Order, String> {

}