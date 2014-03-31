//User.java
package models;

import javax.persistence.*;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import play.db.ebean.Model;
import security.PermissionIndex;
import utils.SessionSearchUtil;
import views.html.setup.permission;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="t_user")
public class User extends BaseModel implements Subject{

    @Column(length=40)
    public String name;
    @Column(length=40)
    public String authMethod;
    @Column(length=40)
    public String passwordHash;

    @ManyToOne
    public Ldap ldap;

    @Column(length=40)
    public String ldapUsername;
    


    @OneToOne
    public Employee employee;

    public Timestamp effectiveSince;
    public boolean locked;

    @OneToMany
    public List<UserPreference> items = new ArrayList<UserPreference>();

    public static Finder<String, User> find() {
        return new Finder<String, User>(String.class, User.class);
    }

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public List<? extends Permission> getPermissions() {
		// TODO Auto-generated method stub
		char[] summary = getSummarys();
		if(summary==null) return null;
		List<security.PermissionIndex> permissions=new ArrayList<security.PermissionIndex>();
		for (int i = 0; summary!=null&&i < summary.length; i++) {
			if(summary[i]==1){
				PermissionIndex permissionIndex = new PermissionIndex();
				permissionIndex.index=i;
				permissions.add(permissionIndex);
			}
		}
		return permissions;
	}
	
	public char[] getSummarys(){
		String summary = SessionSearchUtil.getSessionSummary();
		if(summary!=null){
			return summary.toCharArray();
		}else{
			return null;
		}
	}
	
	@Override
	public List<? extends Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}
	public static User findByUserName(String id)
	    {
			//UserWarehouse userWarehouse = UserWarehouse.find().where().eq("deleted", false).eq("user.deleted", false).eq("warehouse.deleted", false).eq("id", id).findUnique();
		return find().where().eq("deleted", false).eq("id", id).findUnique();
		
			/*if(user!=null)
				return user;
			else
				return null;*/
	    }
}

