package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                Map<String, String> fieldErrors = new LinkedHashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(
                                                error -> fieldErrors.put(
                                                                error.getField(),
                                                                error.getDefaultMessage()));

                ApiErrorResponse body = ApiErrorResponse.validation(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "Existen datos inválidos",
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(body);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidJson(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "El cuerpo de la solicitud contiene datos inválidos",
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(body);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentials(
                        BadCredentialsException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Correo o contraseña incorrectos",
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(body);
        }

        @ExceptionHandler(UsuarioNoHabilitadoException.class)
        public ResponseEntity<ApiErrorResponse> handleUsuarioNoHabilitado(
                        UsuarioNoHabilitadoException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(body);
        }

        @ExceptionHandler(GrupoCatalogoNoEncontradoException.class)
        public ResponseEntity<ApiErrorResponse> handleGrupoCatalogoNoEncontrado(
                        GrupoCatalogoNoEncontradoException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(body);
        }

        @ExceptionHandler(GrupoCatalogoDuplicadoException.class)
        public ResponseEntity<ApiErrorResponse> handleGrupoCatalogoDuplicado(
                        GrupoCatalogoDuplicadoException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(body);
        }

        @ExceptionHandler(CategoriaNoEncontradaException.class)
        public ResponseEntity<ApiErrorResponse> handleCategoriaNoEncontrada(
                        CategoriaNoEncontradaException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(body);
        }

        @ExceptionHandler(CategoriaDuplicadaException.class)
        public ResponseEntity<ApiErrorResponse> handleCategoriaDuplicada(
                        CategoriaDuplicadaException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(body);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
                        IllegalArgumentException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(body);
        }

        @ExceptionHandler(MarcaNoEncontradaException.class)
        public ResponseEntity<ApiErrorResponse> handleMarcaNoEncontrada(
                        MarcaNoEncontradaException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(body);
        }

        @ExceptionHandler(MarcaDuplicadaException.class)
        public ResponseEntity<ApiErrorResponse> handleMarcaDuplicada(
                        MarcaDuplicadaException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(body);
        }

        @ExceptionHandler(EmailYaRegistradoException.class)
        public ResponseEntity<ApiErrorResponse> handleRegistradoDuplicada(
                        EmailYaRegistradoException exception,
                        HttpServletRequest request) {

                ApiErrorResponse body = ApiErrorResponse.simple(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(body);
        }

        @ExceptionHandler(ProductoNoEncontradoException.class)
        public ResponseEntity<ApiErrorResponse> handleProductoNoEncontrado(
                ProductoNoEncontradoException exception,
                HttpServletRequest request
        ) {
                ApiErrorResponse body = ApiErrorResponse.simple(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        exception.getMessage(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
                }

        @ExceptionHandler(ImagenProductoNoEncontradaException.class)
        public ResponseEntity<ApiErrorResponse> handleImagenProductoNoEncontrada(
                ImagenProductoNoEncontradaException exception,
                HttpServletRequest request
        ) {

                ApiErrorResponse body =
                        ApiErrorResponse.simple(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                exception.getMessage(),
                                request.getRequestURI()
                        );


                return ResponseEntity
                        .status(
                                HttpStatus.NOT_FOUND
                        )
                        .body(
                                body
                        );
        }


        @ExceptionHandler(ImagenProductoAlmacenamientoException.class)
        public ResponseEntity<ApiErrorResponse> handleImagenProductoAlmacenamiento(
                ImagenProductoAlmacenamientoException exception,
                HttpServletRequest request
        ) {

                ApiErrorResponse body =
                        ApiErrorResponse.simple(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                exception.getMessage(),
                                request.getRequestURI()
                        );


                return ResponseEntity
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                        .body(
                                body
                        );
        }


        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponse> handleArchivoDemasiadoGrande(
                MaxUploadSizeExceededException exception,
                HttpServletRequest request
        ) {

                ApiErrorResponse body =
                        ApiErrorResponse.simple(
                                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                                "Payload Too Large",
                                "La imagen no puede superar los 5 MB",
                                request.getRequestURI()
                        );


                return ResponseEntity
                        .status(
                                HttpStatus.PAYLOAD_TOO_LARGE
                        )
                        .body(
                                body
                        );
        }


        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTipoArgumentoInvalido(
                MethodArgumentTypeMismatchException exception,
                HttpServletRequest request
        ) {

                ApiErrorResponse body =
                        ApiErrorResponse.simple(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "El valor enviado para '"
                                        + exception.getName()
                                        + "' no tiene un formato válido",
                                request.getRequestURI()
                        );


                return ResponseEntity
                        .status(
                                HttpStatus.BAD_REQUEST
                        )
                        .body(
                                body
                        );
        }


}