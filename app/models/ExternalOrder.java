//ExternalOrder.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_external_order")
public class ExternalOrder extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String orderType;
    @Column(length=40)
    public String sourceType;
    @Column(length=40)
    public String externalOrderNo;
    @Column(length=40)
    public String contractNo;
    @Column(length=40)
    public String ownerCode;
    @Column(length=40)
    public String ownerName;
    public Timestamp orderTimestamp;
    @Column(length=40)
    public String orderStatus;

    @OneToMany
    public List<ExternalOrderItem> items = new ArrayList<ExternalOrderItem>();

    public static Finder<String, ExternalOrder> find() {
        return new Finder<String, ExternalOrder>(String.class, ExternalOrder.class);
    }
}

