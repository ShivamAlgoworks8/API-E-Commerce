package in.algoworks.service;

import in.algoworks.dto.ProductRequestDTO;
import in.algoworks.entity.Product;
import in.algoworks.repository.ProductRepository;
import org.springframework.stereotype.Service;
import in.algoworks.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create a new product using DTO
    public Product createProduct(ProductRequestDTO productRequest) {

        Product product = new Product();

        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setDescription(productRequest.getDescription());
        product.setProductType(productRequest.getProductType());

        return productRepository.save(product);
    }

    // Get product by MongoDB ID

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

    // Update an existing product using DTO
    public Product updateProduct(String id, ProductRequestDTO productRequest) {

        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id);
        }

        Product productToSave = existingProduct.get();

        productToSave.setName(productRequest.getName());
        productToSave.setPrice(productRequest.getPrice());
        productToSave.setStock(productRequest.getStock());
        productToSave.setDescription(productRequest.getDescription());
        productToSave.setProductType(productRequest.getProductType());

        return productRepository.save(productToSave);
    }

    // Delete product by MongoDB ID
    public Boolean deleteProduct(String id) {

        Boolean isProduct = productRepository.existsById(id);

        if (!isProduct) {
            return false;
        }

        productRepository.deleteById(id);

        return true;
    }
}