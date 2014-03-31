//Name.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_name")
public class Name extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String nameLevel;
    @Column(length=40)
    public String nameKey;
    @Column(length=40)
    public String language;
    @Column(columnDefinition="text")
    public String nameValue;

    public static Finder<String, Name> find() {
        return new Finder<String, Name>(String.class, Name.class);
    }
}

