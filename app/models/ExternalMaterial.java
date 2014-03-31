//ExternalMaterial.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;

@Entity
@Table(name="t_external_material")
public class ExternalMaterial extends BaseModel {

    @ManyToOne
    public Business business;
    @ManyToOne
    public Company company;
    @ManyToOne
    public Warehouse warehouse;

    @Column(length=40)
    public String ownerCode;
    @Column(length=40)
    public String ownerName;
    @Column(length=40)
    public String materialCode;
    @Column(length=40)
    public String materialName;
    @Column(length=40)
    public String weightUnitCode;
    @Column(precision=18,scale=4)
    public BigDecimal grossWeight;
    @Column(precision=18,scale=4)
    public BigDecimal netWeight;
    public String volumnUnitCode;
    @Column(precision=18,scale=4)
    public BigDecimal volumn;

    public static Finder<String, ExternalMaterial> find() {
        return new Finder<String, ExternalMaterial>(String.class, ExternalMaterial.class);
    }
}

