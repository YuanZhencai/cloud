//Bin.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="t_bin")
public class Bin extends BaseModel {

    @ManyToOne
    public Area area;

    @Column(length=40)
    public String binCode;
    @Column(length=40)
    public String nameKey;
    public int sortNo;

    @OneToMany
    public List<BinCapacity> binCapacity = new ArrayList<BinCapacity>();

    public static Finder<String, Bin> find() {
        return new Finder<String, Bin>(String.class, Bin.class);
    }
}

