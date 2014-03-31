//Batch.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_batch")
public class Batch extends BaseModel {

    @ManyToOne
    public Batch parent;

    @ManyToOne
    public Material material;
    @Column(length=40)
    public String batchNo;

    public static Finder<String, Batch> find() {
        return new Finder<String, Batch>(String.class, Batch.class);
    }
}

