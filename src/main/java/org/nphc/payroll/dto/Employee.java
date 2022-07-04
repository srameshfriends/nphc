package org.nphc.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity holding employee details represent to the sql table.
 * startDate format is “yyyy-mm-dd" or “dd-mmm-yy”
 *
 * @author Selvaraj Ramesh
 */
public class Employee {
    private String id, login, name;
    private BigDecimal salary;
    private LocalDate startDate;

    public Employee() {
    }

    public Employee(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
