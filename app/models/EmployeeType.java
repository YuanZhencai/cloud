//EmployeeType.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_employee_type")
public class EmployeeType extends BaseModel {

    @ManyToOne
    public Company company;

    @Column(length=40)
    public String typeCode;
    @Column(length=40)
    public String nameKey;
    @Column(length=40)
    public int sortNo;

    public static Finder<String, EmployeeType> find() {
        return new Finder<String, EmployeeType>(String.class, EmployeeType.class);
    }
}

