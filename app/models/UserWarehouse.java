//UserWarehouse.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_user_warehouse")
public class UserWarehouse extends BaseModel {

    @ManyToOne
    public User user;
    @ManyToOne
    public Warehouse warehouse;

    public static Finder<String, UserWarehouse> find() {
        return new Finder<String, UserWarehouse>(String.class, UserWarehouse.class);
    }

}

