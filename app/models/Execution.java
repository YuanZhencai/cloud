//Execution.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_execution")
public class Execution extends BaseModel {
	
	public static final String EXECUTION_TYPE_RECEIVE 	= "T001";
	public static final String EXECUTION_TYPE_ISSUE  	= "T003";

    @ManyToOne
    public PlanItem planItem;
    @ManyToOne
    public PlanItemDetail planItemDetail;

    @Column(length=40)
    public String executionType;
    @Column(length=40)
    public String executionSubtype;
    public boolean reverse;
    public boolean reversed;
    
    @ManyToOne
    @JoinColumn(name="ref_execution_id")
    public Execution refExecution;

    @ManyToOne
    @JoinColumn(name="executed_by")
    public User executedBy;
    public Timestamp executedAt;
    @Column(precision=18,scale=4)
    public BigDecimal executedQty;

    @ManyToOne
    public Material material;

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
    @JoinColumn(name="from_pallet_type_id")
    public PalletType fromPalletType;
    @ManyToOne
    @JoinColumn(name="from_pallet_id")
    public Pallet fromPallet;

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
    @JoinColumn(name="to_pallet_type_id")
    public PalletType toPalletType;
    @ManyToOne
    @JoinColumn(name="to_pallet_id")
    public Pallet toPallet;

    public long seqNo;

    @OneToMany
    public List<StockTransaction> items = new ArrayList<StockTransaction>();

    public static Finder<String, Execution> find() {
        return new Finder<String, Execution>(String.class, Execution.class);
    }
}

