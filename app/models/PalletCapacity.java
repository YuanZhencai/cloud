//PalletCapacity.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_pallet_capacity")
public class PalletCapacity extends BaseModel {

    @ManyToOne
    public PalletType palletType;
    @ManyToOne
    public Pallet pallet;

    @Column(length=40)
    public String capacityType;
    @Column(precision=18,scale=4)
    public BigDecimal capacity;

    public static Finder<String, PalletCapacity> find() {
        return new Finder<String, PalletCapacity>(String.class, PalletCapacity.class);
    }
}

