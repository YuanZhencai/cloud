//StockCountItem.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name="t_stock_count_item")
public class StockCountItem extends BaseModel {

    @ManyToOne
    public StockCountBin stockCountBin;
    @ManyToOne
    public Material material;
    @ManyToOne
    public MaterialUom materialUom;
    @ManyToOne
    public Batch batch;

    @Column(precision=18,scale=4)
    public BigDecimal expectedQty;
    @Column(precision=18,scale=4)
    public BigDecimal actualQty;
    public Timestamp countedAt;

    @ManyToOne
    @JoinColumn(name="operator_id")
    public Employee operator;

    public static Finder<String, StockCountItem> find() {
        return new Finder<String, StockCountItem>(String.class, StockCountItem.class);
    }

}

