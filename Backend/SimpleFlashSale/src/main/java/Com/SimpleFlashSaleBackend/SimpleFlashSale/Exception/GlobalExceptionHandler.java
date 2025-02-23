package Com.SimpleFlashSaleBackend.SimpleFlashSale.Exception;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Response.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ✅ Handle generic exceptions with detailed logs
    @ExceptionHandler(Exception.class)
    public Response<Object> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("🔥 [GLOBAL ERROR] Exception occurred - Message: {}, URL: {}, Description: {}",
                ex.getMessage(), request.getDescription(false), ex);
        ex.printStackTrace(); // For stack trace visibility in logs
        return new Response<>(500, "Internal Server Error: " + ex.getMessage(), null);
    }

    // ✅ Handle resource not found exceptions with logging
    @ExceptionHandler(ResourceNotFoundException.class)
    public Response<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("⚠️ [RESOURCE NOT FOUND] Exception - Message: {}, URL: {}",
                ex.getMessage(), request.getDescription(false));
        return new Response<>(404, ex.getMessage(), null);
    }

    // ✅ Handle invalid requests (e.g., validation errors) with detailed logging
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("❌ [BAD REQUEST] Invalid argument - Message: {}, URL: {}",
                ex.getMessage(), request.getDescription(false));
        return new Response<>(400, "Bad Request: " + ex.getMessage(), null);
    }
}