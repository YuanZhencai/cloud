//MaterialUom.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_material_uom")
public class MaterialUom extends BaseModel {

    @ManyToOne
    public Warehouse warehouse;
    @ManyToOne
    public Material material;

    @Column(length=40)
    public String uomCode;
    @Column(length=40)
    public String uomName;

    public boolean baseUom;
    @Column(precision=18,scale=4)
    public BigDecimal qtyOfBaseNum;
    @Column(precision=18,scale=4)
    public BigDecimal qtyOfBaseDenom;

    public static Finder<String, MaterialUom> find() {
        return new Finder<String, MaterialUom>(String.class, MaterialUom.class);
    }

}

