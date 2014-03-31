//Material.java
package models;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="t_material")
public class Material extends BaseModel {

    @ManyToOne
    public Owner owner;

    @Column(length=40)
    public String materialCode;
    @Column(length=100)
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
    @Column(length=40)
    public String sourceType;
    
    @OneToMany
    public List<MaterialUom> items = new ArrayList<MaterialUom>();

    public static Finder<String, Material> find() {
        return new Finder<String, Material>(String.class, Material.class);
    }
}

