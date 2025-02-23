package Com.SimpleFlashSaleBackend.SimpleFlashSale.Mapper;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.CouponDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.Coupon;

public class CouponMapper {

    // Convert Entity to DTO
    public static CouponDTO toDTO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setName(coupon.getName());
        dto.setImageURL(coupon.getImageURL());
        dto.setPrice(coupon.getPrice());
        dto.setDescription(coupon.getDescription());
        dto.setQuantity(coupon.getQuantity());
        dto.setDeleted(coupon.isDeleted()); // ✅ Map isDeleted field
        return dto;
    }

    // Convert DTO to Entity
    public static Coupon toEntity(CouponDTO dto) {
        if (dto == null) {
            return null;
        }

        Coupon coupon = new Coupon();
        coupon.setId(dto.getId());
        coupon.setName(dto.getName());
        coupon.setImageURL(dto.getImageURL());
        coupon.setPrice(dto.getPrice());
        coupon.setDescription(dto.getDescription());
        coupon.setQuantity(dto.getQuantity());
        coupon.setDeleted(dto.isDeleted()); // ✅ Map isDeleted field
        return coupon;
    }
}

