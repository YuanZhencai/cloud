package controllers.setup;

import models.Permission;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.setup.permission;

import java.io.BufferedReader;
import java.io.FileReader;

import action.Menus;

import com.avaje.ebean.Ebean;
@With(Menus.class)
public class PermissionController extends Controller {

    public static Result index() {
    	System.out.println("bigin");
    	if(Permission.find().all().size()==0){
    		String from="controllers";
    		String to="(";
    	   BufferedReader br=null;
    	    try {
    	    	br= new BufferedReader(new FileReader("conf/routes"));
    	    	String readLine="";
    	    	int i=0;
    	    	Ebean.beginTransaction();
    			try{
    	    	while ((readLine=br.readLine())!=null) {
    	    		int indexOf = readLine.indexOf(from)+1;
    	    		if(indexOf>0){
    	    		if(readLine.indexOf("#")==-1){
    	    			i++;
    	    			String substring = readLine.substring(readLine.indexOf(".",readLine.indexOf(".", indexOf))+1, readLine.indexOf(to, indexOf));
    	    			while(substring.trim().length()>40){
    	    				substring=substring.trim().substring(substring.indexOf(".",1)+1);
    	    				}
    	    				Permission unique = Permission.find().where().eq("index", i).eq("deleted", false).findUnique();
    	    				if(unique!=null){
    	        					unique.code=substring.trim();
    	        				Ebean.update(unique);
    	    				}else{
    	    					Permission permission = new Permission();
    	    					permission.index=i;
    	    					permission.code=substring.trim();
    	    					Ebean.save(permission);
    	    				}
    	    			System.out.println(substring.trim()+i);
    	    		}
    	    		}
    			  }
				Ebean.commitTransaction();
    			}catch(Exception e){
    				e.printStackTrace();
    				Ebean.endTransaction();
    			}
    	  		} catch (Exception e) {
    	  			// TODO Auto-generated catch block
    	  			e.printStackTrace();
    	  		}
    	}
        return ok(permission.render(""));
    }

}
