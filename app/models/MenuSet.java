//MenuSet.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.sql.Timestamp;

@Entity
@Table(name="t_menu_set")
public class MenuSet extends BaseModel {

    @ManyToOne
    @JoinColumn(name="parent_id")
    public MenuSet parent;

    @Column(length=40)
    public String nameKey;
    public int sortNo;

    public static Finder<String, MenuSet> find() {
        return new Finder<String, MenuSet>(String.class, MenuSet.class);
    }
}

