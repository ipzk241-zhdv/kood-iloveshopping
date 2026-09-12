package iloveshopping.demo.shared;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Invalid request");
        return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error", message));
    }
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public org.springframework.http.ResponseEntity<Map<String, String>> business(RuntimeException ex) {
        return org.springframework.http.ResponseEntity.status(ex instanceof IllegalStateException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
