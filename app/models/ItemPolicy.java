//ItemPolicy.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_item_policy")
public class ItemPolicy extends BaseModel {

    @ManyToOne
    public OrderItem orderItem;

    public int sortNo;
    @Column(length=40)
    public String policyType;
    @Column(columnDefinition="text")
    public String policyDescription;

    public static Finder<String, ItemPolicy> find() {
        return new Finder<String, ItemPolicy>(String.class, ItemPolicy.class);
    }

}

