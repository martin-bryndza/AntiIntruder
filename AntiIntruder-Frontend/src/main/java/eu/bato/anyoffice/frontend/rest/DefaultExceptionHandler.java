package eu.bato.anyoffice.frontend.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public class BadRequestException extends RuntimeException {
        //
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        //
    }

//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler({MissingServletRequestParameterException.class,
//        UnsatisfiedServletRequestParameterException.class,
//        HttpRequestMethodNotSupportedException.class,
//        ServletRequestBindingException.class
//    })
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    public @ResponseBody
//    Map<String, Object> handleRequestException(Exception ex) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Request Error");
//        map.put("cause", ex.getMessage());
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    public @ResponseBody
//    Map<String, Object> handleValidationException(ConstraintViolationException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Validation Failure");
//        map.put("violations", convertConstraintViolation(ex.getConstraintViolations()));
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    public @ResponseBody
//    Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Validation Failure");
//        map.put("violations", convertConstraintViolation(ex));
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(ObjectRetrievalFailureException.class)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    public @ResponseBody
//    Map<String, Object> handleValidationException(ObjectRetrievalFailureException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Entity Not Found");
//        map.put("cause", ex.getMessage());
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    @ResponseStatus(value = HttpStatus.CONFLICT)
//    public @ResponseBody
//    Map<String, Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Data Integrity Error");
//        map.put("cause", ex.getCause().getCause().getLocalizedMessage());
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(DataAccessException.class)
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    public @ResponseBody
//    Map<String, Object> handleDataAccessException(DataAccessException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Data Error");
//        map.put("cause", ex.getCause().getMessage());
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
//    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
//    public @ResponseBody
//    Map<String, Object> handleUnsupportedMediaTypeException(HttpMediaTypeNotSupportedException ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Unsupported Media Type");
//        map.put("cause", ex.getLocalizedMessage());
//        map.put("supported", ex.getSupportedMediaTypes());
//        return map;
//    }
//
//    @RequestMapping(produces = {Versions.V1_0})
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    public @ResponseBody
//    Map<String, Object> handleUncaughtException(Exception ex) throws IOException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("error", "Unknown Error");
//        if (ex.getCause() != null) {
//            map.put("cause", ex.getCause().getMessage());
//        } else {
//            map.put("cause", ex.getMessage());
//        }
//        return map;
//    }
//
//    private Map<String, Map<String, Object>> convertConstraintViolation(Set<ConstraintViolation<?>> constraintViolations) {
//        Map<String, Map<String, Object>> result = new HashMap<>();
//        for (ConstraintViolation constraintViolation : constraintViolations) {
//            Map<String, Object> violationMap = new HashMap<>();
//            violationMap.put("value", constraintViolation.getInvalidValue());
//            violationMap.put("type", constraintViolation.getRootBeanClass());
//            violationMap.put("message", constraintViolation.getMessage());
//            result.put(constraintViolation.getPropertyPath().toString(), violationMap);
//        }
//        return result;
//    }
//
//    private Map<String, Map<String, Object>> convertConstraintViolation(MethodArgumentNotValidException ex) {
//        Map<String, Map<String, Object>> result = new HashMap<>();
//        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
//            Map<String, Object> violationMap = new HashMap<>();
//            violationMap.put("target", ex.getBindingResult().getTarget());
//            violationMap.put("type", ex.getBindingResult().getTarget().getClass());
//            violationMap.put("message", error.getDefaultMessage());
//            result.put(error.getObjectName(), violationMap);
//        }
//        return result;
//    }
}
