//CodeCat.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_code_cat")
public class CodeCat extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String catLevel;
    @Column(length=40)
    public String catKey;
    @Column(length=40)
    public String nameKey;
    public boolean sysCodeCat;

    public static Finder<String, CodeCat> find() {
        return new Finder<String, CodeCat>(String.class, CodeCat.class);
    }
}

