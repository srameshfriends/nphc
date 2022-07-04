package org.nphc.payroll.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.nphc.payroll.dto.Employee;
import org.nphc.payroll.dto.EmployeeDto;
import org.nphc.payroll.dto.MessageException;
import org.nphc.payroll.repository.EmployeeJdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *  NPHC Assignment
 *  Employee controller handle request from http service.
 *  Upload URL : /users/upload, Method : POST, Fields : id, login, name, salary, startDate
 *  Query URL : /users, Method : GET,  filter by salary, order by all fields, pagination limit and offset
 *  Update URL : /users/{id}, Method : PUT/PATCH, Employee updated by the id, request content json format.
 *  Delete URL : /users/{id}, Method : DELETE, Employee deleted by the id.
 *  @author Ramesh
*/
@RestController
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private static final String[] HEADERS = new String[]{"id", "login", "name", "salary", "start_date"};

    @Autowired
    private EmployeeJdbc employeeJdbc;

    /**
     * Employee list optional parameters are min & max salary, offset, page limit, order by default id
     * */
    @GetMapping("users")
    public @ResponseBody ResponseEntity<ObjectNode> search(
            @RequestParam("minSalary") Optional<BigDecimal>  minSalary,
            @RequestParam("maxSalary") Optional<BigDecimal> maxSalary,
            @RequestParam("offset") Optional<Integer> offset,
            @RequestParam("limit") Optional<Integer> limit,
            @RequestParam("sorting") Optional<String> sorting,
            @RequestParam("desc") Optional<Boolean> isDesc) {
        List<Employee> employeeList = employeeJdbc.search(minSalary.orElse(null), maxSalary.orElse(null),
                sorting.orElse("id"), limit.orElse(0), offset.orElse(0), isDesc.orElse(false));
        EmployeeDto dto = new EmployeeDto();
        JsonMapper mapper = new JsonMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.set("results", dto.getArrayNode(employeeList));
        return new ResponseEntity<>(objectNode, HttpStatus.OK);
    }

    /**
     * Find employee by id.
     * */
    @GetMapping("users/{id}")
    public @ResponseBody ResponseEntity<ObjectNode> findById(@PathVariable("id") String id) {
        if(id == null || id.isEmpty()) {
            throw new MessageException("Bad input - no such employee");
        }
        Employee employee = employeeJdbc.findById(id);
        if(employee == null) {
            throw new MessageException(HttpStatus.CREATED, "employee not found.");
        }
        return new ResponseEntity<>(new EmployeeDto().getObjectNode(employee), HttpStatus.OK);
    }

    /**
     * Upload the employee information
     * */
    @PostMapping("users/upload")
    public @ResponseBody ResponseEntity<ObjectNode> uploadEmployee(@RequestParam("file") MultipartFile file) {
        CSVFormat.Builder csvBuilder = CSVFormat.DEFAULT.builder();
        csvBuilder.setHeader(HEADERS).setIgnoreEmptyLines(true).setIgnoreSurroundingSpaces(true);
        CSVFormat csvFormat = csvBuilder.setSkipHeaderRecord(true).build();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> csvRecords = csvFormat.parse(bufferedReader).stream().iterator();
            List<Employee> employeeList = getEmployeeList(csvRecords);
            if (employeeList.isEmpty()) {
                return getMessageResult("No records to upload.", HttpStatus.OK);
            }
            String duplicateName = findDuplicate(employeeList);
            if (duplicateName != null) {
                throw new MessageException(HttpStatus.BAD_REQUEST, "Duplicate employee not accepted.");
            }
            List<String> oldList = employeeJdbc.findIds(employeeList);
            if (oldList.isEmpty()) {
                employeeJdbc.insert(employeeList);
                return getMessageResult(employeeList.size() + ", employee(s) are added.", HttpStatus.CREATED);
            }
            List<Employee> insertList = new ArrayList<>(), updateList = new ArrayList<>();
            employeeList.forEach(employee -> {
                if (oldList.contains(employee.getId())) {
                    updateList.add(employee);
                } else {
                    insertList.add(employee);
                }
            });
            String msg = "";
            if (!updateList.isEmpty()) {
                msg = updateList.size() + ", employees are updated.";
                employeeJdbc.update(updateList);
            }
            if (!insertList.isEmpty()) {
                msg = msg.isEmpty() ? (insertList.size() + ", employees are added.") : ", and added (" + insertList.size() + ")";
                employeeJdbc.insert(insertList);
            }
            return getMessageResult(msg, HttpStatus.CREATED);
        } catch (IOException ex) {
            throw new MessageException(HttpStatus.EXPECTATION_FAILED, ex.getMessage());
        }
    }

    private ResponseEntity<ObjectNode> getMessageResult(String msg, HttpStatus status) {
        JsonMapper mapper = new JsonMapper();
        ObjectNode msgNode = mapper.createObjectNode();
        msgNode.put("message", msg);
        return new ResponseEntity<>(msgNode, status);
    }

    private String findDuplicate(List<Employee> employeeList) {
        Set<String> employeeIdSet = new HashSet<>(), loginIdSet = new HashSet<>();
        for (Employee employee : employeeList) {
            if (employeeIdSet.contains(employee.getId())) {
                return employee.getName();
            }
            if (loginIdSet.contains(employee.getLogin())) {
                return employee.getName();
            }
            employeeIdSet.add(employee.getId());
            loginIdSet.add(employee.getLogin());
        }
        return null;
    }

    /**
     * @NotNull csvRecords
     * CSV to employee type conversion, It will ignore if char start with # tag lines.
     */
    private List<Employee> getEmployeeList(Iterator<CSVRecord> csvRecords) {
        EmployeeDto dto = new EmployeeDto();
        List<Employee> employeeList = new ArrayList<>();
        csvRecords.forEachRemaining(csv -> {
            if (!csv.get(0).trim().startsWith("#")) {
                employeeList.add(dto.getValidEmployee(csv));
            }
        });
        return employeeList;
    }

    /**
     * Employee list optional parameters are min & max salary, offset, page limit, order by default id
     * */
    @RequestMapping(value = "/users", method = RequestMethod.POST, consumes="application/json")
    public @ResponseBody ResponseEntity<ObjectNode> insertEmployee(@RequestBody  ObjectNode objectNode) {
        Employee employee = new EmployeeDto().getValidEmployee(objectNode);
        if(employeeJdbc.isIdExists(employee)) {
            throw new MessageException("Employee ID already exists");
        }
        if(employeeJdbc.isLoginExits(employee)) {
            throw new MessageException("Employee login not unique");
        }
        employeeJdbc.insert(Collections.singletonList(employee));
        return getMessageResult("Successfully created", HttpStatus.OK);
    }

    /**
     * Update employee information by id (PUT)
     * */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT, consumes="application/json")
    public @ResponseBody ResponseEntity<ObjectNode> putEmployee(
            @PathVariable("id") String id, @RequestBody  ObjectNode objectNode) {
        objectNode.put("id", id);
        return updateEmployee(objectNode);
    }

    /**
     * Update employee information by id (PATCH)
     * */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.PATCH, consumes="application/json")
    public @ResponseBody ResponseEntity<ObjectNode> patchEmployee(
            @PathVariable("id") String id, @RequestBody  ObjectNode objectNode) {
        objectNode.put("id", id);
        return updateEmployee(objectNode);
    }

    private ResponseEntity<ObjectNode> updateEmployee(ObjectNode objectNode) {
        Employee employee = new EmployeeDto().getValidEmployee(objectNode);
        Employee old = employeeJdbc.findById(employee.getId());
        if(old == null) {
            throw new MessageException("Bad input - no such employee");
        }
        if(employeeJdbc.isLoginExits(employee)) {
            throw new MessageException("Employee login not unique");
        }
        employeeJdbc.update(Collections.singletonList(employee));
        return getMessageResult("Successfully updated", HttpStatus.OK);
    }

    /**
     * Delete employee information by id.
     * */
    @DeleteMapping("users/{id}")
    public @ResponseBody ResponseEntity<ObjectNode> deleteById(@PathVariable("id") String id) {
        if(id == null || id.isEmpty()) {
            throw new MessageException("Bad input - no such employee");
        }
        int count = employeeJdbc.deleteById(id);
        if(count == 0) {
            throw new MessageException("Bad input - no such employee.");
        }
        logger.info("Employee has deleted : " + id);
        return getMessageResult("Employee deleted", HttpStatus.OK);
    }
}
