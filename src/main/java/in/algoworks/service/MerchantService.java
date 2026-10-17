
package in.algoworks.service;

import in.algoworks.dto.MerchantRequestDTO;
import in.algoworks.entity.Merchant;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.MerchantRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    // Create Merchant
    public Merchant createMerchant(MerchantRequestDTO merchantReq) {
        Merchant merchant = new Merchant();

        merchant.setMerchantName(merchantReq.getMerchantName());
        merchant.setBrandName(merchantReq.getBrandName());
        merchant.setProductType(merchantReq.getProductType());
        merchant.setStatus(merchantReq.getStatus());
        merchant.setImage(merchantReq.getImage());

        return merchantRepository.save(merchant);
    }

    // Get All Merchants
    public List<Merchant> getAllMerchant() {
        return merchantRepository.findAll();
    }

    // Get Merchant by ID
    public Merchant getMerchant(String id) {
        return merchantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Merchant not found with id: " + id));
    }

    // Update Merchant
    public Merchant updateMerchant(
            String id, MerchantRequestDTO merchantReq) {

        Merchant merchant = getMerchant(id);

        merchant.setMerchantName(merchantReq.getMerchantName());
        merchant.setBrandName(merchantReq.getBrandName());
        merchant.setProductType(merchantReq.getProductType());
        merchant.setStatus(merchantReq.getStatus());
        merchant.setImage(merchantReq.getImage());

        return merchantRepository.save(merchant);
    }

    // Delete Merchant
    public Boolean deleteMerchant(String id) {
        Merchant merchant = getMerchant(id);
        merchantRepository.delete(merchant);

        return true;
    }
}
