package in.algoworks.repository;

import in.algoworks.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {

    List<Admin> findAllByEmailIgnoreCase(String email);

    Optional<Admin> findByEmailIgnoreCase(String email);
}
