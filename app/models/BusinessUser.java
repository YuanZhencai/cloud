//BusinessUser.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.sql.Timestamp;

@Entity
@Table(name="t_business_user")
public class BusinessUser extends BaseModel {

    @ManyToOne
    public User user;
    @ManyToOne
    public Business business;
    @ManyToOne
    public Employee employee;

    public Timestamp effectiveSince;
    public boolean locked;

    public static Finder<String, BusinessUser> find() {
        return new Finder<String, BusinessUser>(String.class, BusinessUser.class);
    }

}

