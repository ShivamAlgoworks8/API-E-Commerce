
package in.algoworks.service;

import in.algoworks.dto.ProductRequestDTO;
import in.algoworks.entity.Product;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.OrderRepository;
import in.algoworks.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // Create a new product
    public Product createProduct(ProductRequestDTO productRequest) {
        Product product = new Product();

        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setDescription(productRequest.getDescription());
        product.setProductType(productRequest.getProductType());

        return productRepository.save(product);
    }

    // Get product by ID
    public Product getProduct(String id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id));
    }

    // Get all products
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    // Update product
    public Product updateProduct(
            String id, ProductRequestDTO productRequest) {

        Product product = getProduct(id);

        if (!Objects.equals(product.getName(), productRequest.getName())
                && orderRepository.existsByProduct(product.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product cannot be renamed while orders reference it");
        }

        int stockDelta = productRequest.getStock() - product.getStock();
        int minimumStock = stockDelta < 0 ? -stockDelta : 0;
        long updatedCount = productRepository.updateProductById(
                id,
                productRequest.getName(),
                productRequest.getPrice(),
                productRequest.getDescription(),
                productRequest.getProductType(),
                minimumStock,
                stockDelta);
        if (stockDelta != 0 && updatedCount == 0) {
            getProduct(id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock changed concurrently; retry the product update");
        }

        return getProduct(id);
    }

    // Delete product
    public Boolean deleteProduct(String id) {
        Product product = getProduct(id);
        if (orderRepository.existsByProduct(product.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product cannot be deleted while orders reference it");
        }
        productRepository.delete(product);
        return true;
    }
}
