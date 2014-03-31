//ExternalOrderItem.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_external_order_item")
public class ExternalOrderItem extends BaseModel {

    @ManyToOne
    public ExternalOrder externalOrder;
    @ManyToOne
    public ExternalMaterial externalMaterial;
    @ManyToOne
    public ExternalMaterialUom externalMaterialUom;

    @Column(precision=18,scale=4)
    public BigDecimal qty;
    @Column(precision=18,scale=4)
    public BigDecimal minPercent;
    @Column(precision=18,scale=4)
    public BigDecimal maxPercent;
    @Column(length=40)
    public String itemStatus;
    public int sortNo;

    public static Finder<String, ExternalOrderItem> find() {
        return new Finder<String, ExternalOrderItem>(String.class, ExternalOrderItem.class);
    }
}

