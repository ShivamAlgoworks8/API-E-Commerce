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

    @Query("{ '_id': ?0, 'stock': { '$gte': ?5 } }")
    @Update("{ '$set': { 'name': ?1, 'price': ?2, 'description': ?3, 'productType': ?4 }, '$inc': { 'stock': ?6 } }")
    long updateProductById(String id, String name, double price,
                           String description, String productType,
                           int minimumStock, int stockDelta);
}