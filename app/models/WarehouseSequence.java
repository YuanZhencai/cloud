//WarehouseSequence.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_warehouse_sequence")
public class WarehouseSequence extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String sequenceKey;
    public long startNo;
    public long currentNo;

    public static Finder<String, WarehouseSequence> find() {
        return new Finder<String, WarehouseSequence>(String.class, WarehouseSequence.class);
    }

}

