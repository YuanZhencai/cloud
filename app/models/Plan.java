//Plan.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_plan")
public class Plan extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String planType;
    @Column(length=40)
    public String planSubtype;

    @ManyToOne
    public Order order;

    public Timestamp plannedTimestamp;
    public long seqNo;

    @ManyToOne
    @JoinColumn(name="assigned_to")
    public Employee assignedTo;

    @Column(length=40)
    public String planStatus;

    @OneToMany
    public List<PlanItem> items = new ArrayList<PlanItem>();

    public static Finder<String, Plan> find() {
        return new Finder<String, Plan>(String.class, Plan.class);
    }
}

