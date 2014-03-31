//Employee.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_employee")
public class Employee extends BaseModel {

    @ManyToOne
    public Company company;
    @ManyToOne
    public EmployeeType employeeType;

    @Column(length=40)
    public String employeeCode;
    @Column(length=40)
    public String employeeName;

    public static Finder<String, Employee> find() {
        return new Finder<String, Employee>(String.class, Employee.class);
    }
}

