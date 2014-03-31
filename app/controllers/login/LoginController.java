package controllers.login;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import models.User;
import models.Warehouse;
import models.vo.login.UserVo;
import models.vo.query.WarehouseVo;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.SessionSearchUtil;
public class LoginController extends Controller {

    //static Logger logger = new Logger();

    public static Result index() {
        return ok(views.html.login.login.render("ODC Engineer"));
    }
    public static Result login(){
    	System.out.println("login");
    	RequestBody body = request().body();
        if(body.asJson()!=null){
        	UserVo userVo = Json.fromJson(body.asJson(), UserVo.class);
        		//List<UserWarehouse> users =UserWarehouse.find().where().eq("warehouse.id",userVo.warehouseId).eq("warehouse.deleted", false).eq("user.name", userVo.name).eq("user.passwordHash", userVo.password).eq("user.locked", false).eq("user.deleted", false).eq("deleted", false).findList();
        		List<User> users = User.find().where().eq("deleted", false).eq("name", userVo.name).eq("passwordHash", userVo.password).eq("locked", false).findList();
        		if(users.size()>0){
        				//默认用户warehouse时使用
        		        session().put("user", users.get(0).id.toString());
        				List<Warehouse> warehouses = SessionSearchUtil.searchWarehouseList(users.get(0));
        				System.out.println("warehouse.Size:"+warehouses.size());
        				if(warehouses!=null&&warehouses.size()>0)
        				//System.out.println(userVo.warehouseId);
        				session().put("warehouse",warehouses.get(0).id.toString());
        				try{
        				//permission
        				//String PermissionSummary = SessionSearchUtil.binaryString2hexString(SessionSearchUtil.charToString(SessionSearchUtil.getDataPermissions()));
        				String PermissionSummary = SessionSearchUtil.binaryString2hexString("101111111111111111111111111111111111111111111111111111111111111111111111111");
        				if(PermissionSummary!=null)
        				session().put("Permission",PermissionSummary);
        				//Menus
        				String MenuSummary = SessionSearchUtil.binaryString2hexString(SessionSearchUtil.charToString(SessionSearchUtil.getDataMenus()));
        				//System.out.println(MenuSummary);
        				//String MenuSummary = SessionSearchUtil.binaryString2hexString("111111111111111111111111111111111111111111111111111111111111111111111111111");
        				if(MenuSummary!=null)
        				session().put("Menus",MenuSummary);
        				}catch(Exception e){
        				   // session().clear();
        					e.printStackTrace();
        					return badRequest("this User haven't persession");
        				}
        				return ok("");
        			}
        }
        return badRequest("Can't login");
    }
    public static Result logout(){
    	session().clear();
    	return ok();
    	//return redirect(routes.Application.index());
    }
    
	public static Result downloadChrome() {
	  File file = new File("/home/btc/chrome/27.0.1453.116_chrome_installer.exe");
	  response().setContentType("application/x-msdownload");
	  response().setHeader("Content-Disposition", "attachment; filename="+file.getName());
	  return ok(file);
	}
    
    public static Result initWarehouse(){
		 List<WarehouseVo> warehouseVos= new ArrayList<WarehouseVo>();
		 //List<Bin> bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id", warehouseId).findList();
		 List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).findList();
		 for (Warehouse warehouse : warehouses) {
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 warehouseVo.inwarehouse(warehouse);
			 warehouseVos.add(warehouseVo);
		 }
		 System.out.println(play.libs.Json.toJson(warehouseVos));
		 return ok(play.libs.Json.toJson(warehouseVos));
	 }
    
    public static Result getWarehouseList(){
    	User user = SessionSearchUtil.searchUser();
    	if(user!=null){
    	ArrayList<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
    	List<Warehouse> warehouseList = SessionSearchUtil.searchWarehouseList(user);
    	for (Warehouse warehouse : warehouseList) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			warehouseVos.add(warehouseVo);
		}
    	System.out.println("warehouseVos"+warehouseVos.size());
    	return ok(play.libs.Json.toJson(warehouseVos));
    	}
    	return badRequest("Can't find Warehouses");
    	
    }
    public static Result getWarehouse(){
    	Warehouse warehouse = SessionSearchUtil.searchWarehouse();
    	if(warehouse!=null){
    		WarehouseVo warehouseVo = new WarehouseVo();
    		warehouseVo.inwarehouse(warehouse);
    		return ok(play.libs.Json.toJson(warehouseVo));
    	}
    	return badRequest("Can't find User's Warehouse");
    }
    
    public static Result changeWarehouse(String id){
    	System.out.println("changeWarehouse");
    	User user = SessionSearchUtil.searchUser();
    	if(user!=null&&id!=null){
    		System.out.println(id);
    		List<Warehouse> warehouseList = SessionSearchUtil.searchWarehouseList(user);
    		for (Warehouse warehouse : warehouseList) {
    			System.out.println("warehouse:"+warehouse.id);
    			if(warehouse.id.toString().equals(id)){
    				//session().remove("warehouse");
    				session().put("warehouse", warehouse.id.toString());
    				return ok("success");
    			}
			}
    	}
    	return badRequest("Data don't find");
    }
}