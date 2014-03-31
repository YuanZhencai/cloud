//StockCount.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

@Entity
@Table(name="t_stock_count")
public class StockCount extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    public Timestamp countAt;
    @Column(length=40)
    public String countType;
    @Column(length=40)
    public String countStatus;

    @ManyToOne
    @JoinColumn(name="supervisor_id")
    public Employee supervisor;

    public long seqNo;

    @OneToMany
    public List<StockCountBin> items = new ArrayList<StockCountBin>();

    public static Finder<String, StockCount> find() {
        return new Finder<String, StockCount>(String.class, StockCount.class);
    }
}

