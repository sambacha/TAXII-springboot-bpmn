package io.digitalstate.taxii.mongo.exceptions;

import io.digitalstate.taxii.exception.TaxiiException;
import org.springframework.http.HttpStatus;

public class DiscoveryDoesNotExistException extends TaxiiException {

    private static String DEFAULT_TITLE = "Discovery cannot be found or cannot be accessed.";
    private static String DEFAULT_HTTP_STATUS = String.valueOf(HttpStatus.FORBIDDEN.value());

    public DiscoveryDoesNotExistException() {
        super(null, DEFAULT_TITLE,
                null, null, null,
                DEFAULT_HTTP_STATUS,
                null, null);
    }

    public DiscoveryDoesNotExistException(String description) {
        super(null, DEFAULT_TITLE,
                description, null, null,
                DEFAULT_HTTP_STATUS,
                null, null);
    }
}
