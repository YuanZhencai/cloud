//AffiliatedWarehouse.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_affiliated_warehouse")
public class AffiliatedWarehouse extends BaseModel {

    @ManyToOne
    @Column(nullable=false)
    public Warehouse warehouse;

    @Column(length=40)
    public String businessCode;
    @Column(length=40)
    public String companyCode;
    @Column(length=40)
    public String warehouseCode;
    @Column(length=40)
    public String warehouseName;
    public int sortNo;

    public static Finder<String, AffiliatedWarehouse> find() {
        return new Finder<String, AffiliatedWarehouse>(String.class, AffiliatedWarehouse.class);
    }
}

