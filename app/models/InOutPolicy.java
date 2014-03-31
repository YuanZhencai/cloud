//InOutPolicy.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_inout_policy")
public class InOutPolicy extends BaseModel {

    @ManyToOne
    public ItemPolicy itemPolicy;

    public int sortNo;
    @Column(length=40)
    public String inOutType;

    public static Finder<String, InOutPolicy> find() {
        return new Finder<String, InOutPolicy>(String.class, InOutPolicy.class);
    }

}

