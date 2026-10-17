
package in.algoworks.repository;

import in.algoworks.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    List<Customer> findAllByName(String name);
}
