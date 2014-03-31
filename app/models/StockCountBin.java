//StockCountBin.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_stock_count_bin")
public class StockCountBin extends BaseModel {

    @ManyToOne
    public StockCount stockCount;
    @ManyToOne
    public Area area;
    @ManyToOne
    public Bin bin;

    @Column(length=40)
    public String countStatus;

    @OneToMany
    public List<StockCountItem> items = new ArrayList<StockCountItem>();

    public static Finder<String, StockCountBin> find() {
        return new Finder<String, StockCountBin>(String.class, StockCountBin.class);
    }
}

