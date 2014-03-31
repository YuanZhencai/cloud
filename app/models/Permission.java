package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_permission")
public class Permission extends BaseModel{

    public int index;
    @Column(length=40)
    public String code;
    @Column(length=40)
    public String name;

    public static Finder<String, Permission> find() {
        return new Finder<String, Permission>(String.class, Permission.class);
    }
}

