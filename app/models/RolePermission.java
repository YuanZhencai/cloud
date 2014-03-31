//RolePermission.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_role_permission")
public class RolePermission extends BaseModel {

    @ManyToOne
    public Role role;

    @Column(columnDefinition="text")
    public String summary;

    public static Finder<String, RolePermission> find() {
        return new Finder<String, RolePermission>(String.class, RolePermission.class);
    }
}

