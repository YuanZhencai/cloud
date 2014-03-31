//StockTransaction.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import java.util.UUID;

@Entity
@Table(name="t_stock_transaction")
public class StockTransaction extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;
    @ManyToOne
    public Stock stock;
    @ManyToOne
    public Execution execution;

    @Column(length=40)
    public String transactionCode;

    public UUID oldUomId;
    @Column(precision=18,scale=4)
    public BigDecimal oldQty;
    public UUID oldAreaId;
    public UUID oldBinId;
    public Timestamp oldArrivedAt;
    @Column(length=40)
    public String oldStatus;
    public UUID oldPalletTypeId;
    public UUID oldPalletId;
    public UUID oldTracingId;

    public UUID newUomId;
    @Column(precision=18,scale=4)
    public BigDecimal newQty;
    public UUID newAreaId;
    public UUID newBinId;
    public Timestamp newArrivedAt;
    @Column(length=40)
    public String newStatus;
    public UUID newPalletTypeId;
    public UUID newPalletId;
    public UUID newTracingId;

    public Timestamp transactionAt;
    public long seqNo;

    public static Finder<String, StockTransaction> find() {
        return new Finder<String, StockTransaction>(String.class, StockTransaction.class);
    }

}

