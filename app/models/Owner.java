//Owner.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_owner")
public class Owner extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String ownerCode;
    @Column(length=40)
    public String ownerName;

    public static Finder<String, Owner> find() {
        return new Finder<String, Owner>(String.class, Owner.class);
    }
}

