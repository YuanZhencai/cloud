//BinCapacity.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_bin_capacity")
public class BinCapacity extends BaseModel {

    @ManyToOne
    public Bin bin;

    @Column(length=40)
    public String capacityType;

    @Column(precision=18,scale=4)
    public BigDecimal capacity;

    public static Finder<String, BinCapacity> find() {
        return new Finder<String, BinCapacity>(String.class, BinCapacity.class);
    }
}

