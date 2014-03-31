//Ldap.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

@Entity
@Table(name="t_ldap")
public class Ldap extends BaseModel {

    @Column(length=40)
    public String ipAddress;
    @Column(length=40)
    public String adminUser;
    @Column(length=40)
    public String adminPassword;

    public static Finder<String, Ldap> find() {
        return new Finder<String, Ldap>(String.class, Ldap.class);
    }
}

