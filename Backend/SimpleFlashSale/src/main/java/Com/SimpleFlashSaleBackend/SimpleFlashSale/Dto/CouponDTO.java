package Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponDTO {
    private Long id;
    private String name;
    private String imageURL;
    private Integer price;
    private String description;
    private int quantity;
    private boolean paymentSuccessful;
    private boolean isDeleted;
}