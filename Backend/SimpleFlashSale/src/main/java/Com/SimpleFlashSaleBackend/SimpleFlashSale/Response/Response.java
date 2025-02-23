package Com.SimpleFlashSaleBackend.SimpleFlashSale.Response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response<T> {
    // ✅ Getters and Setters
    private int statusCode;
    private String message;
    private T data;

    public Response(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
}
