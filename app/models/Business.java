//Business.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Entity
@Table(name="t_business")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Business extends BaseModel {

    @Column(length=40)
    public String businessCode;
    @Column(length=40)
    public String nameKey;

    @OneToMany(mappedBy = "business")
    public List<Company> companies = new ArrayList<Company>();

    public static Finder<String, Business> find() {
        return new Finder<String, Business>(String.class, Business.class);
    }
}

