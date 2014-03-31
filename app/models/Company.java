//Company.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_company")
public class Company extends BaseModel {

    @ManyToOne
    public Business business;

    @Column(length=40)
    public String companyCode;
    @Column(length=40)
    public String nameKey;

    public static Finder<String, Company> find() {
        return new Finder<String, Company>(String.class, Company.class);
    }
}

