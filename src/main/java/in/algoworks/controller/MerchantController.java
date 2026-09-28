package in.algoworks.controller;

import in.algoworks.entity.Merchant;
import in.algoworks.service.MerchantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    // Create Merchant
    @PostMapping
    public Merchant createMerchant(@RequestBody Merchant merchantReq) {
        return merchantService.createMerchant(merchantReq);
    }

    // Get All Merchants
    @GetMapping
    public List<Merchant> getAllMerchant() {
        return merchantService.getAllMerchant();
    }

    // Get Merchant by ID
    @GetMapping("/{id}")
    public Merchant getMerchant(@PathVariable String id) {
        return merchantService.getMerchant(id);
    }

    // Update Merchant
    @PutMapping("/{id}")
    public Merchant updateMerchant(
            @PathVariable String id,
            @RequestBody Merchant merchantReq) {

        return merchantService.updateMerchant(id, merchantReq);
    }

    // Delete Merchant
    @DeleteMapping("/{id}")
    public Boolean deleteMerchant(@PathVariable String id) {
        return merchantService.deleteMerchant(id);
    }
}