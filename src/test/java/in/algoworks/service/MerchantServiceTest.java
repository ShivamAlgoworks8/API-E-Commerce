package in.algoworks.service;

import in.algoworks.dto.MerchantRequestDTO;
import in.algoworks.entity.Merchant;
import in.algoworks.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MerchantServiceTest {

    private MerchantRepository merchantRepository;
    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantRepository = mock(MerchantRepository.class);
        merchantService = new MerchantService(merchantRepository);
        when(merchantRepository.save(any(Merchant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsMerchantWithoutBrandName() {
        MerchantRequestDTO request = new MerchantRequestDTO();
        request.setMerchantName("A Merchant");
        request.setProductType("Electronics");
        request.setStatus("Active");
        request.setImage("merchant.png");

        Merchant created = merchantService.createMerchant(request);

        assertNull(created.getBrandName());
    }
}
