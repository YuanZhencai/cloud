//PalletType.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.sql.Timestamp;

@Entity
@Table(name="t_pallet_type")
public class PalletType extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String typeCode;
    @Column(length=40)
    public String nameKey;

    public static Finder<String, PalletType> find() {
        return new Finder<String, PalletType>(String.class, PalletType.class);
    }
}

