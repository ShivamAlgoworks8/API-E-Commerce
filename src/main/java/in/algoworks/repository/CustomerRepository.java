package in.algoworks.repository;

import in.algoworks.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends MongoRepository<Customer, String> {

    List<Customer> findAllByName(String name);

    Optional<Customer> findByEmail(String email);

    List<Customer> findAllByEmailIgnoreCase(String email);
}