//Role.java
package models;

import javax.persistence.*;

@Entity
@Table(name="t_role")
public class Role extends BaseModel{

    @Column(length=40)
    public String nameKey;

    public static Finder<String, Role> find() {
        return new Finder<String, Role>(String.class, Role.class);
    }
}

