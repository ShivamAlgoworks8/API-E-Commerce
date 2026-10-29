
package in.algoworks.repository;

import in.algoworks.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findAllByName(String name);

    // Existing: Decrease stock by one
    @Query("{ '_id': ?0, 'stock': { '$gt': 0 } }")
    @Update("{ '$inc': { 'stock': -1 } }")
    long decrementStockIfAvailable(String id);

    // NEW: Check required stock, then apply the supplied stock change
    // Pass positive quantity as minimumStock and negative quantity as stockDelta
    @Query("{ '_id': ?0, 'stock': { '$gte': ?1 } }")
    @Update("{ '$inc': { 'stock': ?2 } }")
    long decrementStockByQuantityIfAvailable(
            String id,
            int minimumStock,
            int stockDelta
    );

    // Existing: Increase stock by one
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'stock': 1 } }")
    long incrementStock(String id);

    // NEW: Restore stock by a given quantity
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'stock': ?1 } }")
    long incrementStockByQuantity(String id, int quantity);

    // Existing: Update product details and stock
    @Query("{ '_id': ?0, 'stock': { '$gte': ?5 } }")
    @Update("{ '$set': { 'name': ?1, 'price': ?2, 'description': ?3, 'productType': ?4, 'merchantName': ?7, 'imageUrl': ?8 }, '$inc': { 'stock': ?6 } }")
    long updateProductById(
            String id,
            String name,
            double price,
            String description,
            String productType,
            int minimumStock,
            int stockDelta,
            String merchantName,
            String imageUrl
    );
}
