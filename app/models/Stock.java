//Stock.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.sql.Timestamp;

@Entity
@Table(name="t_stock")
public class Stock extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;
    @ManyToOne
    public Material material;
    @ManyToOne
    public MaterialUom materialUom;
    @ManyToOne
    public Batch batch;

    @Column(precision=18,scale=4)
    public BigDecimal qty;

    @ManyToOne
    public PalletType palletType;
    @ManyToOne
    public Pallet pallet;

    @ManyToOne
    public UUID tracingId;

    @ManyToOne
    public Area area;
    @ManyToOne
    public Bin bin;

    public Timestamp receivedAt;
    public Timestamp arrivedAt;
    public Timestamp issuedAt;

    @Column(length=40)
    public String stockStatus;

    public static Finder<String, Stock> find() {
        return new Finder<String, Stock>(String.class, Stock.class);
    }
}

