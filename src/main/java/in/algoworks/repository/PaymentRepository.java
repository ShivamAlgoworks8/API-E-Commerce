package in.algoworks.repository;
import in.algoworks.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface PaymentRepository extends
        MongoRepository<Payment, String> {

}
