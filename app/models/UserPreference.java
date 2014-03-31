//UserPreference.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_user_perference")
public class UserPreference extends BaseModel {

    @ManyToOne
    public User user;

    @Column(length=40)
    public String prefKey;
    @Column(columnDefinition="text")
    public String prefValue;

    public static Finder<String, UserPreference> find() {
        return new Finder<String, UserPreference>(String.class, UserPreference.class);
    }

}

