package sparkanalysis.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleException(Exception e){
            return ResponseEntity
            .status(500)
            .body(e.getMessage());
        }
}
