//UomCapacityPoint.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name="t_uom_capacity_point")
public class UomCapacityPoint extends BaseModel {

    @ManyToOne
    public MaterialUom materialUom;

    @Column(length=40)
    public String capacityType;
    @Column(precision=18,scale=4)
    public BigDecimal capacityPoint;

    public static Finder<String, UomCapacityPoint> find() {
        return new Finder<String, UomCapacityPoint>(String.class, UomCapacityPoint.class);
    }
}

