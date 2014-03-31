//Warehouse.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_warehouse")
public class Warehouse extends BaseModel {

    @ManyToOne
    public Company company;

    @Column(length=40)
    public String warehouseCode;
    @Column(length=40)
    public String nameKey;
    public int sortNo;

    public static Finder<String, Warehouse> find() {
        return new Finder<String, Warehouse>(String.class, Warehouse.class);
    }

}

