package in.algoworks.service;

import in.algoworks.entity.Product;
import in.algoworks.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product productReq) {
        Product productResp = productRepository.save(productReq);
        return productResp;
    }

    public Product getProduct(String id) {
        Optional<Product> productResp = productRepository.findById(id);

        if (productResp.isPresent()) {
            return productResp.get();
        }

        return null;
    }

    public List<Product> getAllProduct() {
        List<Product> productList = productRepository.findAll();
        return productList;
    }
    public Product updateProduct(String id, Product productReq) {

        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isEmpty()) {
            return null;
        }

        Product productToSave = existingProduct.get();

        productToSave.setName(productReq.getName());
        productToSave.setPrice(productReq.getPrice());
        productToSave.setStock(productReq.getStock());
        productToSave.setDescription(productReq.getDescription());
        productToSave.setProductType(productReq.getProductType());

        return productRepository.save(productToSave);
    }
    public Boolean deleteProduct(String id) {

        Boolean isProduct = productRepository.existsById(id);

        if (!isProduct) {
            return false;
        }

        productRepository.deleteById(id);

        return true;
    }
}