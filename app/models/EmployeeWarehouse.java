//EmployeeWarehouse.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_employee_warehouse")
public class EmployeeWarehouse extends BaseModel {

    @ManyToOne
    public Employee employee;
    @ManyToOne
    public Warehouse warehouse;

    public static Finder<String, EmployeeWarehouse> find() {
        return new Finder<String, EmployeeWarehouse>(String.class, EmployeeWarehouse.class);
    }
}

