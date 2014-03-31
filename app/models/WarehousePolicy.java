//WarehousePolicy.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_warehouse_policy")
public class WarehousePolicy extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;
    @ManyToOne
    public StorageType storageType;
    @ManyToOne
    public Area area;
    @ManyToOne
    public Bin bin;

    public int sortNo;

    @Column(length=40)
    public String policyType;
    @Column(columnDefinition="text")
    public String policyDescription;

    public static Finder<String, WarehousePolicy> find() {
        return new Finder<String, WarehousePolicy>(String.class, WarehousePolicy.class);
    }

}

