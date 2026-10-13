
package in.algoworks.service;

import in.algoworks.dto.MerchantRequestDTO;
import in.algoworks.entity.Merchant;
import in.algoworks.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        Optional<Merchant> merchantResp =
                merchantRepository.findById(id);

        return merchantResp.orElse(null);
    }

    // Update Merchant
    public Merchant updateMerchant(
            String id, MerchantRequestDTO merchantReq) {

        Optional<Merchant> existingMerchant =
                merchantRepository.findById(id);

        if (existingMerchant.isEmpty()) {
            return null;
        }

        Merchant merchantToSave = existingMerchant.get();

        merchantToSave.setMerchantName(merchantReq.getMerchantName());
        merchantToSave.setBrandName(merchantReq.getBrandName());
        merchantToSave.setProductType(merchantReq.getProductType());
        merchantToSave.setStatus(merchantReq.getStatus());
        merchantToSave.setImage(merchantReq.getImage());

        return merchantRepository.save(merchantToSave);
    }

    // Delete Merchant
    public Boolean deleteMerchant(String id) {
        if (!merchantRepository.existsById(id)) {
            return false;
        }

        merchantRepository.deleteById(id);
        return true;
    }
}
