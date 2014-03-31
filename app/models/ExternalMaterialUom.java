//ExternalMaterialUom.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_external_material_uom")
public class ExternalMaterialUom extends BaseModel {

    @ManyToOne
    public ExternalMaterial externalMaterial;

    @Column(length=40)
    public String materialCode;
    @Column(length=40)
    public String uomCode;
    @Column(length=40)
    public String uomName;
    public boolean baseUom;
    @Column(precision=18,scale=4)
    public BigDecimal qtyOfBaseNum;
    @Column(precision=18,scale=4)
    public BigDecimal qtyOfBaseDenom;

    public static Finder<String, ExternalMaterialUom> find() {
        return new Finder<String, ExternalMaterialUom>(String.class, ExternalMaterialUom.class);
    }

}

