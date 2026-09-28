package in.algoworks.service;

import in.algoworks.entity.Merchant;
import in.algoworks.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MerchantService {

    private MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    // Create Merchant
    public Merchant createMerchant(Merchant merchantReq) {
        Merchant merchantResp = merchantRepository.save(merchantReq);
        return merchantResp;
    }

    // Get All Merchants
    public List<Merchant> getAllMerchant() {
        List<Merchant> merchantList = merchantRepository.findAll();
        return merchantList;
    }

    // Get Merchant by ID
    public Merchant getMerchant(String id) {

        Optional<Merchant> merchantResp = merchantRepository.findById(id);

        if (merchantResp.isPresent()) {
            return merchantResp.get();
        }

        return null;
    }

    // Update Merchant
    public Merchant updateMerchant(String id, Merchant merchantReq) {

        Optional<Merchant> existingMerchant = merchantRepository.findById(id);

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

        Boolean isMerchant = merchantRepository.existsById(id);

        if (!isMerchant) {
            return false;
        }

        merchantRepository.deleteById(id);

        return true;
    }
}