//Area.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.sql.Timestamp;

@Entity
@Table(name="t_area")
public class Area extends BaseModel {

    @ManyToOne
    @Column(nullable=false)
    public Warehouse warehouse;

    @ManyToOne
    @Column(nullable=false)
    public StorageType storageType;

    @Column(length=40)
    public String areaCode;

    @Column(length=40)
    public String nameKey;

    public int sortNo;

    public static Finder<String, Area> find() {
        return new Finder<String, Area>(String.class, Area.class);
    }
}

