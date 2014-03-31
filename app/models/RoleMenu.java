//RoleMenu.java
package models;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
@Table(name="t_role_menu")
public class RoleMenu extends BaseModel {

    @OneToOne
    public Role role;
/*    @ManyToOne
    public Menu menu;
*/ 
    @Column(columnDefinition="text",name="summary")
	public String summary;
    
    public static Finder<String, RoleMenu> find() {
        return new Finder<String, RoleMenu>(String.class, RoleMenu.class);
    }
}

