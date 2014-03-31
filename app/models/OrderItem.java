//OrderItem.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_order_item")
public class OrderItem extends BaseModel {

    @ManyToOne
    public Order order;
    @ManyToOne
    public ExternalOrder externalOrder;
    @ManyToOne
    public ExternalOrderItem externalOrderItem;
    @ManyToOne
    public Material material;
    @ManyToOne
    public MaterialUom settlementUom;
    @Column(precision=18,scale=4)
    public BigDecimal settlementQty;
    @ManyToOne
    public MaterialUom materialUom;
    @Column(precision=18,scale=4)
    public BigDecimal qty;
    @Column(precision=18,scale=4)
    public BigDecimal minPercent;
    @Column(precision=18,scale=4)
    public BigDecimal maxPercent;
    @Column(length=40)
    public String itemStatus;
    public int sortNo;

    public static Finder<String, OrderItem> find() {
        return new Finder<String, OrderItem>(String.class, OrderItem.class);
    }
}

