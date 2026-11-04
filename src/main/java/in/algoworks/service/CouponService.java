
package in.algoworks.service;

import in.algoworks.dto.CouponRequestDTO;
import in.algoworks.entity.Coupon;
import in.algoworks.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public Coupon createCoupon(CouponRequestDTO requestDTO) {

        // 1. Validate coupon code
        if (requestDTO.getCode() == null || requestDTO.getCode().isBlank()) {
            throw new IllegalArgumentException("Coupon code is required");
        }

        // 2. Check duplicate coupon code
        String code = requestDTO.getCode().trim().toUpperCase();

        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        // 3. Validate discount type
        String type = requestDTO.getDiscountType();

        if (type == null ||
                (!type.equalsIgnoreCase("PERCENTAGE")
                        && !type.equalsIgnoreCase("FIXED"))) {
            throw new IllegalArgumentException("Invalid discount type");
        }

        // 4. Validate discount value
        double value = requestDTO.getDiscountValue();

        if (value <= 0) {
            throw new IllegalArgumentException("Discount must be greater than zero");
        }

        if (type.equalsIgnoreCase("PERCENTAGE") && value > 100) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100");
        }

        // 5. Validate order amount and maximum discount
        if (requestDTO.getMinimumOrderAmount() < 0 ||
                requestDTO.getMaximumDiscount() < 0) {
            throw new IllegalArgumentException("Amounts cannot be negative");
        }

        // 6. Validate dates
        if (requestDTO.getStartDate() == null ||
                requestDTO.getExpiryDate() == null ||
                !requestDTO.getExpiryDate().isAfter(requestDTO.getStartDate())) {
            throw new IllegalArgumentException("Invalid coupon dates");
        }

        // 7. Validate usage limit
        if (requestDTO.getUsageLimit() != null &&
                requestDTO.getUsageLimit() <= 0) {
            throw new IllegalArgumentException("Usage limit must be positive");
        }

        // 8. Create Coupon entity
        Coupon coupon = new Coupon();

        coupon.setCode(code);
        coupon.setDiscountType(type.toUpperCase());
        coupon.setDiscountValue(value);
        coupon.setMinimumOrderAmount(requestDTO.getMinimumOrderAmount());
        coupon.setMaximumDiscount(requestDTO.getMaximumDiscount());
        coupon.setStartDate(requestDTO.getStartDate());
        coupon.setExpiryDate(requestDTO.getExpiryDate());
        coupon.setUsageLimit(requestDTO.getUsageLimit());
        coupon.setActive(requestDTO.isActive());
        coupon.setEligibilityType(requestDTO.getEligibilityType());
        coupon.setEligibleCustomerIds(requestDTO.getEligibleCustomerIds());

        // 9. Save coupon in MongoDB
        return couponRepository.save(coupon);
    }
}
