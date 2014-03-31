//Menu.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_menu")
public class Menu extends BaseModel {

    @ManyToOne
    public MenuSet menuSet;
    public int index;
    @Column(length=40)
    public String nameKey;
    @Column(length=40)
    public String uri;
    public int sortNo;

    public static Finder<String, Menu> find() {
        return new Finder<String, Menu>(String.class, Menu.class);
    }
}

