
package in.algoworks.service;

import in.algoworks.dto.ProductRequestDTO;
import in.algoworks.entity.Product;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setDescription(productRequest.getDescription());
        product.setProductType(productRequest.getProductType());

        return productRepository.save(product);
    }

    // Delete product
    public Boolean deleteProduct(String id) {
        Product product = getProduct(id);
        productRepository.delete(product);
        return true;
    }
}
