//BaseUnit.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name="t_base_unit")
public class BaseUnit extends BaseModel {

    @Column(length=40)
    public String unitType;

    public boolean baseUnit;

    @Column(length=40)
    public String unitCode;

    @Column(length=40)
    public String nameKey;

    @Column(precision=18,scale=4)
    public BigDecimal rate;

    public static Finder<String, BaseUnit> find() {
        return new Finder<String, BaseUnit>(String.class, BaseUnit.class);
    }
}

