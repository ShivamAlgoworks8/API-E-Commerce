
package in.algoworks.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CouponRequestDTO {

    private String code;
    private String discountType;
    private double discountValue;
    private double minimumOrderAmount;
    private double maximumDiscount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Integer usageLimit;
    private boolean active;
    private String eligibilityType;
    private List<String> eligibleCustomerIds;

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(double minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public double getMaximumDiscount() {
        return maximumDiscount;
    }

    public void setMaximumDiscount(double maximumDiscount) {
        this.maximumDiscount = maximumDiscount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEligibilityType() {
        return eligibilityType;
    }

    public void setEligibilityType(String eligibilityType) {
        this.eligibilityType = eligibilityType;
    }

    public List<String> getEligibleCustomerIds() {
        return eligibleCustomerIds;
    }

    public void setEligibleCustomerIds(List<String> eligibleCustomerIds) {
        this.eligibleCustomerIds = eligibleCustomerIds;
    }
}
