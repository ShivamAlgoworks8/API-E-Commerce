package in.algoworks.repository;
import in.algoworks.entity.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findAllByActive(boolean active);
    List<Coupon> findAllByEligibilityType(String eligibilityType);
    List<Coupon> findAllByEligibilityTypeAndActive(String eligibilityType, boolean active);

}