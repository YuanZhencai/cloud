//TimingPolicy.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_timing_policy")
public class TimingPolicy extends BaseModel {

    @ManyToOne
    public ItemPolicy itemPolicy;

    public int sortNo;

    @ManyToOne
    public Material material;
    @ManyToOne
    public StorageType storageType;

    public int minHours;
    public int maxHours;

    public static Finder<String, TimingPolicy> find() {
        return new Finder<String, TimingPolicy>(String.class, TimingPolicy.class);
    }

}

