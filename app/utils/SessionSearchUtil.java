package utils;

import java.util.ArrayList;
import java.util.List;

import models.Business;
import models.BusinessUser;
import models.BusinessUserRole;
import models.Menu;
import models.RoleMenu;
import models.RolePermission;
import models.User;
import models.UserWarehouse;
import models.Warehouse;
import static play.mvc.Controller.session;

public class SessionSearchUtil {
	public static Warehouse searchWarehouse(){
		String id =session().get("warehouse");
		Warehouse warehouse = Warehouse.find().fetch("company.business").where().eq("id",id).eq("deleted", false).findUnique();
		//UserWarehouse userWarehouse = UserWarehouse.find().where().eq("id",id).eq("deleted", false).eq("user.deleted", false).eq("warehouse.deleted", false).findUnique();
		return warehouse;
	}
	public static List<Warehouse> searchWarehouseList(User user){
		List<UserWarehouse> userWarehouses = UserWarehouse.find().where().eq("warehouse.deleted", false).eq("user",user).eq("deleted", false).findList();
		List<Warehouse> Warehouses=new ArrayList<Warehouse>();
		for (UserWarehouse userWarehouse : userWarehouses) {
			Warehouses.add(userWarehouse.warehouse);
		}
		return Warehouses;
	}
	public static User searchUser(){
		String id = play.mvc.Controller.session().get("user");
		User user = User.find().where().eq("id",id).eq("deleted", false).eq("locked", false).findUnique();
		//UserWarehouse userWarehouse = UserWarehouse.find().where().eq("id",id).eq("deleted", false).eq("user.deleted", false).eq("warehouse.deleted", false).findUnique();
		return user;
	}
	
	public static char[] getDataMenus(){
		Warehouse warehouse = searchWarehouse();
		User user = searchUser();
		Business business=warehouse.company.business;
		List<BusinessUserRole> BusinessUserRoles = searchBusinessUserRole(user,business);
		char[] summary=null;
		for (BusinessUserRole businessUserRole : BusinessUserRoles) {
			List<RoleMenu> roleMenus = RoleMenu.find().where().eq("deleted", false).eq("role", businessUserRole.role).findList();
			for (RoleMenu roleMenu : roleMenus) {
				if(summary==null){
				    if(!roleMenu.summary.equals("0"))
				        
					summary = hexString2binaryString(roleMenu.summary).toCharArray();
				}else{
				    if(!roleMenu.summary.equals("0"))
					summary =SessionSearchUtil.combine(summary,hexString2binaryString(roleMenu.summary).toCharArray());
				}
			}
		}
		return summary;
	}
	
	public static String getSessionMenus(){
		return session().get("Menus");
	}
	
	public static List<BusinessUserRole> searchBusinessUserRole(User user,Business business){
	    System.out.println(user.id+":"+business.id);
		List<BusinessUser> businessUsers = BusinessUser.find().where().eq("deleted", false).eq("user.id", user.id.toString()).eq("user.deleted", false).eq("business.id", business.id.toString()).eq("business.deleted", false).findList();
		if(businessUsers.size()>0){
		BusinessUser businessUser = businessUsers.get(0);
		    return  BusinessUserRole.find().where().eq("deleted", false).eq("businessUser", businessUser).eq("role.deleted", false).findList();
		}else{
		    return new ArrayList<BusinessUserRole>();
		}
	}
	
	public static char[] combineSummary(List<char[]> summarys){
		char[] summary=null;
		for (char[] TempSummary : summarys) {
			if(summary==null)
				summary=TempSummary;
			else
				combine(summary,TempSummary);
		}
		return summary;
	}
	
	public static char[] combine(char[] summary1,char[] summary2){
		int length=summary1.length>summary2.length?summary2.length:summary1.length;
		for (int i = 0; i < length; i++) {
			summary1[i]=(char) (summary1[i]|summary2[i]);
		}
		return summary1;
	}
	
	public static char[] getDataPermissions(){
		User user = SessionSearchUtil.searchUser();
		Warehouse warehouse = SessionSearchUtil.searchWarehouse();
		if(user==null||warehouse==null||warehouse.company==null||warehouse.company.business==null){
			return null;
		}
		List<BusinessUserRole> Roles = SessionSearchUtil.searchBusinessUserRole(user,warehouse.company.business);
		//List<models.BusinessUserRole> Roles = models.BusinessUserRole.find().where().eq("role.deleted", false).eq("deleted",false).eq("businessUser.user.name", name).eq("businessUser.user.deleted", false).findList();
		char[] summary=null;
		for (BusinessUserRole businessUserRole : Roles) {
			List<RolePermission> rolePermissions = RolePermission.find().where().eq("deleted", false).eq("role", businessUserRole.role).findList();
			for (RolePermission rolePermission : rolePermissions) {
				if(summary==null){
					summary = rolePermission.summary.toCharArray();
				}else{
					summary =SessionSearchUtil.combine(summary, rolePermission.summary.toCharArray());
				}
			}
		}
		return summary;
	}
	
	public static String getSessionSummary(){
		return session().get("Permission");
	}
	
	public static String charToString(char[] charList){
		//char[] summary = getUserSummary();
		if(charList==null) 
			return null;
		else 
			return String.valueOf(charList);
	}
	/*
	 * 二进制转16进制
	 */
	public static String binaryString2hexString(String bString)  
    {  
        if (bString == null || bString.equals(""))  
            return null; 
        StringBuffer tmp = new StringBuffer();  
        int iTmp = 0;  
        if(bString.length()%4!=0){
        	 iTmp+=Integer.parseInt(bString.substring(0 ,bString.length()%4),2); 
        	 tmp.append(Integer.toHexString(iTmp));  
        }
        for (int i = bString.length()%4; i < bString.length(); i += 4)  
        {  
            iTmp = 0;  
            for (int j = 0; j < 4; j++)  
            {  
            	//System.out.println(Integer.valueOf(bString.substring(i + j, i + j + 1),2));
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);  
            }  
            tmp.append(Integer.toHexString(iTmp));  
        }  
        return tmp.toString();  
    }  
	/*
	 * 16进制转二进制
	 */
	 public static String hexString2binaryString(String hexString)  
	    {  
		 	//System.err.println(hexString);
	        if (hexString == null )  
	            return null;  
	        String bString = "", tmp;  
	        /*if(hexString.length()%2!=0){
	        	tmp="00" + Integer.toBinaryString(Integer.parseInt(hexString.substring(0,1), 16));
	        	bString += tmp.substring(tmp.length() - 2);  
	        }*/
	        for (int i = 0; i < hexString.length(); i++)  
	        {  
	            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));  
	            bString += tmp.substring(tmp.length() - 4);  
	        }
	        //System.out.println(bString);
	        for (int i = 0; i < bString.length(); i++) {
				if(bString.charAt(i)=='1'){
					bString=bString.substring(i,bString.length());
					break;
				}
			}
	        System.out.println(bString);
	        return bString;  
	    }  
}
