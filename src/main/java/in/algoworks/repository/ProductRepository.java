package in.algoworks.repository;

import in.algoworks.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findAllByName(String name);

    @Query("{ '_id': ?0, 'stock': { '$gt': 0 } }")
    @Update("{ '$inc': { 'stock': -1 } }")
    long decrementStockIfAvailable(String id);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'stock': 1 } }")
    long incrementStock(String id);
}