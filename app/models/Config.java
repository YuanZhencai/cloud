//Config.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_config")
public class Config extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String configLevel;
    @Column(length=40)
    public String configKey;
    @Column(length=40)
    public String configValue;

    public static Finder<String, Config> find() {
        return new Finder<String, Config>(String.class, Config.class);
    }
}

