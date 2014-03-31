//Order.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_order")
public class Order extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String orderType;
    @Column(length=40)
    public String sourceType;
    @Column(length=40)
    public String externalOrderNo;
    @Column(length=40)
    public String internalOrderNo;
    @Column(length=40)
    public String contractNo;
    public Timestamp orderTimestamp;
    @Column(length=40)
    public String orderStatus;

    @OneToMany
    public List<OrderItem> items = new ArrayList<OrderItem>();

    public static Finder<String, Order> find() {
        return new Finder<String, Order>(String.class, Order.class);
    }
}

