package org.nphc.payroll.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * EmployeeDto Data Transfer Object.
 * startDate format is “yyyy-mm-dd" or “dd-mmm-yy”
 *
 * @author Selvaraj Ramesh
 */
public class EmployeeDto implements RowMapper<Employee> {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDto.class);
    private static final DateTimeFormatter dateFormatYMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateFormatDMY = DateTimeFormatter.ofPattern("dd-MMM-yy");

    /**
     *   Get sql parameter for insert employee into the database.
     */
    public Object[] getInsertParameters(Employee employee) {
        Object[] result = new Object[5];
        result[0] = employee.getId();
        result[1] = employee.getLogin();
        result[2] = employee.getName();
        result[3] = employee.getSalary();
        result[4] = getDate(employee.getStartDate());
        return result;
    }

    /**
     *   jdbc sql data type to insert employee.
     */
    public int[] getInsertSQLType() {
        int[] result = new int[5];
        result[0] = JDBCType.VARCHAR.getVendorTypeNumber();
        result[1] = JDBCType.VARCHAR.getVendorTypeNumber();
        result[2] = JDBCType.VARCHAR.getVendorTypeNumber();
        result[3] = JDBCType.DECIMAL.getVendorTypeNumber();
        result[4] = JDBCType.DATE.getVendorTypeNumber();
        return result;
    }

    /**
     *   Get sql parameter for update employee into the database.
     */
    public Object[] getUpdateObjects(Employee employee) {
        Object[] result = new Object[5];
        result[0] = employee.getLogin();
        result[1] = employee.getName();
        result[2] = employee.getSalary();
        result[3] = getDate(employee.getStartDate());
        result[4] = employee.getId();
        return result;
    }


    /**
     *   jdbc sql data type to update employee.
     */
    public int[] getUpdateSQLType() {
        int[] result = new int[5];
        result[0] = JDBCType.VARCHAR.getVendorTypeNumber();
        result[1] = JDBCType.VARCHAR.getVendorTypeNumber();
        result[2] = JDBCType.DECIMAL.getVendorTypeNumber();
        result[3] = JDBCType.DATE.getVendorTypeNumber();
        result[4] = JDBCType.VARCHAR.getVendorTypeNumber();
        return result;
    }

    /**
     *   sql result set to employee conversion.
     */
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getString("id"));
        employee.setName(rs.getString("name"));
        employee.setLogin(rs.getString("login"));
        employee.setSalary(rs.getBigDecimal("salary"));
        employee.setStartDate(getLocalDate(rs.getDate("start_date")));
        return employee;
    }

    /**
     *   Employee list to json array conversion.
     */
    public ArrayNode getArrayNode(List<Employee> employees) {
        JsonMapper mapper = new JsonMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        employees.forEach(employee -> arrayNode.add(getObjectNode(mapper, employee)));
        return arrayNode;
    }


    /**
     *   Employee to json object conversion.
     */
    public ObjectNode getObjectNode(Employee employee) {
        return getObjectNode(new JsonMapper(), employee);
    }

    private ObjectNode getObjectNode(JsonMapper mapper, Employee employee) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", employee.getId());
        objectNode.put("login", employee.getLogin());
        objectNode.put("name", employee.getName());
        objectNode.put("salary", employee.getSalary());
        objectNode.put("startDate", dateFormatYMD.format(employee.getStartDate()));
        return objectNode;
    }

    @SuppressWarnings("unchecked")
    private <T> T getTypeSafe(JsonNode value, Class<?> dType) {
        if(String.class.equals(dType)) {
            if(JsonNodeType.STRING.equals(value.getNodeType())) {
                return (T)value.asText();
            }
        } else if(BigDecimal.class.equals(dType)) {
            try {
                if(JsonNodeType.NUMBER.equals(value.getNodeType())) {
                    return (T)BigDecimal.valueOf(value.asDouble());
                } else if(JsonNodeType.STRING.equals(value.getNodeType())) {
                    double numValue = Double.parseDouble(value.asText());
                    return (T)BigDecimal.valueOf(numValue);
                }
            } catch (Exception exp) {
                logger.debug("Type conversion error : " + exp.getMessage());
            }
            return (T)BigDecimal.ZERO;
        } else if(LocalDate.class.equals(dType)) {
            if(JsonNodeType.STRING.equals(value.getNodeType())) {
                return (T)getLocalDate(value.asText());
            }
        } else if (Boolean.class.equals(dType)) {
            if (JsonNodeType.BOOLEAN.equals(value.getNodeType())) {
                return (T) Boolean.valueOf(value.asBoolean());
            }
            return (T) Boolean.valueOf(false);
        }
        return null;
    }

    /**
     * Get valid employee from CSV records.
     */
    public Employee getValidEmployee(CSVRecord csvRecord) {
        if (csvRecord.size() != 5) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Invalid CSV format to upload as employee.");
        }
        Employee employee = new Employee();
        employee.setId(getValidText(csvRecord.get(0)));
        employee.setLogin(getValidText(csvRecord.get(1)));
        employee.setName(getValidText(csvRecord.get(2)));
        employee.setSalary(getBigDecimal(csvRecord.get(3)));
        employee.setStartDate(getLocalDate(csvRecord.get(4)));
        validate(employee);
        return employee;
    }

    /**
     * Get valid employee from JSON records.
     */
    public Employee getValidEmployee(ObjectNode objNode) {
        Employee employee = new Employee();
        employee.setId(getTypeSafe(objNode.get("id"), String.class));
        employee.setLogin(getTypeSafe(objNode.get("login"), String.class));
        employee.setName(getTypeSafe(objNode.get("name"), String.class));
        employee.setSalary(getTypeSafe(objNode.get("salary"), BigDecimal.class));
        employee.setStartDate(getTypeSafe(objNode.get("startDate"), LocalDate.class));
        validate(employee);
        return employee;
    }

    /**
     * Validate employee before update to the database.
     */
    public void validate(Employee employee) {
        if (employee.getId() == null) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Employee id should not be empty.");
        }
        if (employee.getLogin() == null) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Employee login should not be empty.");
        }
        if (employee.getName() == null) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Employee name should not be empty.");
        }
        BigDecimal salary = employee.getSalary();
        if(0 <= BigDecimal.ZERO.compareTo(salary)) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Invalid salary");
        }
        if(employee.getStartDate() == null) {
            throw new MessageException(HttpStatus.BAD_REQUEST, "Invalid date");
        }
    }

    private LocalDate getLocalDate(Date sqlDate) {
        return  sqlDate == null ? null : sqlDate.toLocalDate();
    }

    private Date getDate(LocalDate localDate) {
        return  localDate == null ? null : Date.valueOf(LocalDate.now());
    }

    private BigDecimal getBigDecimal(String text) {
        BigDecimal value = BigDecimal.ZERO;
        try {
            value = BigDecimal.valueOf(Double.parseDouble(text));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return value;
    }

    private String getValidText(String text) {
        return text == null || text.trim().isEmpty() ? null : text.trim();
    }

    private LocalDate getLocalDate(String text) {
        LocalDate localDate = null;
        if(text == null) {
            return null;
        } else if(text.length() == 9) {
            try {
                localDate = LocalDate.parse(text, dateFormatDMY);
            } catch (DateTimeParseException px1) {
                logger.debug(px1.getMessage());
            }
        } else if(text.length() == 10) {
            try {
                localDate = LocalDate.parse(text, dateFormatYMD);
            } catch (DateTimeParseException px2) {
                logger.debug(px2.getMessage());
            }
        }
        return localDate;
    }
}
