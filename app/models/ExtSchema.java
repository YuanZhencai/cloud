//ExtSchema.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_ext_schema")
public class ExtSchema extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String schemaLevel;
    @Column(length=40)
    public String schemaKey;
    @Column(length=40)
    public String schemaValue;

    public static Finder<String, ExtSchema> find() {
        return new Finder<String, ExtSchema>(String.class, ExtSchema.class);
    }
}

