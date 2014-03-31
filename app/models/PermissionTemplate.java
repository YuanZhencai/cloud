//PermissionTemplate.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_permission_template")
public class PermissionTemplate extends BaseModel {

    @Column(length=40)
    public String code;
    @Column(length=40)
    public String name;
    @Column(columnDefinition="text")
    public String summary;

    public static Finder<String, PermissionTemplate> find() {
        return new Finder<String, PermissionTemplate>(String.class, PermissionTemplate.class);
    }
}

