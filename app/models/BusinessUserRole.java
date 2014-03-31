//BusinessUserRole.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_business_user_role")
public class BusinessUserRole extends BaseModel {

    @ManyToOne
    public BusinessUser businessUser;
    @ManyToOne
    public Role role;

    public static Finder<String, BusinessUserRole> find() {
        return new Finder<String, BusinessUserRole>(String.class, BusinessUserRole.class);
    }
}

