package org.nphc.payroll.embeded;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nphc.payroll.dto.MessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.sql.SQLException;

@ControllerAdvice
public class MessageExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MessageException.class)
    public @ResponseBody ResponseEntity<Object> handleMessageException(MessageException mex) {
        JsonMapper mapper = new JsonMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("message", mex.getMessage());
        return new ResponseEntity<>(objectNode, mex.getHttpStatus());
    }

    @ExceptionHandler(SQLException.class)
    public @ResponseBody ResponseEntity<Object> handleSQLException(SQLException sxn) {
        JsonMapper mapper = new JsonMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("message", sxn.getMessage());
        return new ResponseEntity<>(objectNode, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public @ResponseBody ResponseEntity<Object> handleIOException(IOException sxn) {
        JsonMapper mapper = new JsonMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("message", sxn.getMessage());
        return new ResponseEntity<>(objectNode, HttpStatus.BAD_REQUEST);
    }
}
