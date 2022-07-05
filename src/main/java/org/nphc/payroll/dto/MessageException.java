package org.nphc.payroll.dto;

import org.springframework.http.HttpStatus;


/**
 * Customized exception handle.
 *
 * @author Selvaraj Ramesh
 */
public class MessageException extends RuntimeException {
    private final HttpStatus status;

    public MessageException(String message) {
        super(message);
        status = HttpStatus.BAD_REQUEST;
    }

    public MessageException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }
}
