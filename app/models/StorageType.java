//StorageType.java
package models;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import play.db.ebean.Model;

@Entity
@Table(name="t_storage_type")
public class StorageType extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String nameKey;
    public int sortNo;

    public static Finder<String, StorageType> find() {
        return new Finder<String, StorageType>(String.class, StorageType.class);
    }
}

