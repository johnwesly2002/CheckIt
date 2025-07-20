package com.ecommerce.checkIt.exceptions;

public class ResourceNotFoundException extends  RuntimeException{
    String resourceName;
    String fieldName;
    String field;
    Long fieldId;

    public ResourceNotFoundException(String resourceName, String fieldName, String field, Long fieldId) {
        super(String.format("%s with %s %s not found", resourceName, fieldName, field));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldId = fieldId;
    }
    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super(String.format("%s with %s %s not found", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.fieldId = fieldId;
        this.field = field;
    }


    public ResourceNotFoundException() {
    }

}
