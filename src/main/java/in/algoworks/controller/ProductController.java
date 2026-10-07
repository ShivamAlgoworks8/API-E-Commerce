package in.algoworks.controller;

import in.algoworks.entity.Product;
import in.algoworks.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3001")
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        // Fetch all products from MongoDB
        return productService.getAllProduct();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        // Fetch a single product using its ID
        return productService.getProduct(id);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        // Create and save a new product
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable String id,
            @RequestBody Product product) {

        // Update existing product using its ID
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteProduct(@PathVariable String id) {
        // Delete existing product using its ID
        return productService.deleteProduct(id);
    }
}