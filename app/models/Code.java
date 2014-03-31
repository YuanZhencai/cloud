//Code.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_code")
public class Code extends BaseModel {

    @ManyToOne
    public CodeCat codeCat;

    @Column(length=40)
    public String codeKey;
    @Column(length=40)
    public String catKey;
    @Column(length=40)
    public String nameKey;
    public boolean sysCode;
    public int sortNo;

    public static Finder<String, Code> find() {
        return new Finder<String, Code>(String.class, Code.class);
    }

}

