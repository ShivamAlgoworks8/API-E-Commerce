package in.algoworks.dto;
import jakarta.validation.constraints.NotBlank;

public class MerchantRequestDTO {
    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotBlank(message = "Product type is required")
    private String productType;

    @NotBlank(message = "Status is required")
    private String status;

    private String image;

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
