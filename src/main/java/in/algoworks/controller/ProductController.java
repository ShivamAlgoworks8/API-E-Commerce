package in.algoworks.controller;

import in.algoworks.dto.ProductRequestDTO;
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
        return productService.getAllProduct();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequestDTO productRequest) {
        return productService.createProduct(productRequest);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequestDTO productRequest) {
        return productService.updateProduct(id, productRequest);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id);
    }
}