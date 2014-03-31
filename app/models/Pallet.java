//Pallet.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.sql.Timestamp;

@Entity
@Table(name="t_pallet")
public class Pallet extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;
    @ManyToOne
    public Area area;
    @ManyToOne
    public Bin bin;
    @ManyToOne
    public PalletType palletType;

    @Column(length=40)
    public String serialNo;
    public Timestamp inUseSince;
    @Column(length=40)
    public String palletStatus;

    public static Finder<String, Pallet> find() {
        return new Finder<String, Pallet>(String.class, Pallet.class);
    }

}

