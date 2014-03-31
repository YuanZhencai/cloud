//PlanItemDetail.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

@Entity
@Table(name="t_plan_item_detail")
public class PlanItemDetail extends BaseModel {

    @ManyToOne
    public PlanItem planItem;
    @ManyToOne
    public Stock stock;

    @Column(precision=18,scale=4)
    public BigDecimal palnnedQty;

    @ManyToOne
    @JoinColumn(name="from_uom_id")
    public MaterialUom fromMaterialUom;
    @ManyToOne
    @JoinColumn(name="from_area_id")
    public Area fromArea;
    @ManyToOne
    @JoinColumn(name="from_bin_id")
    public Bin fromBin;

    @ManyToOne
    @JoinColumn(name="to_uom_id")
    public MaterialUom toMaterialUom;
    @ManyToOne
    @JoinColumn(name="to_area_id")
    public Area toArea;
    @ManyToOne
    @JoinColumn(name="to_bin_id")
    public Bin toBin;

    @ManyToOne
    @JoinColumn(name="assigned_to")
    public Employee assignedTo;

    @Column(length=40)
    public String detailStatus;

    public int sortNo;

    public static Finder<String, PlanItemDetail> find() {
        return new Finder<String, PlanItemDetail>(String.class, PlanItemDetail.class);
    }
}

