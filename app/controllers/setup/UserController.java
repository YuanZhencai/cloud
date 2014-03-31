/** * UserController.java 
* Created on 2013-5-9 上午9:25:31 
*/

package controllers.setup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.BusinessUser;
import models.BusinessUserRole;
import models.Company;
import models.Role;
import models.User;
import models.UserWarehouse;
import models.Warehouse;
import models.vo.setup.CompanyVo;
import models.vo.setup.RoleVo;
import models.vo.setup.UserSearchVo;
import models.vo.setup.UserVo;
import models.vo.setup.WarehouseVo;

import org.joda.time.DateTime;

import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import security.NoUserDeadboltHandler;
import utils.CrudUtil;
import utils.DateUtil;
import utils.SessionSearchUtil;
import views.html.setup.user;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/**
 *  
* <p>Project: CloudWMS</p> 
* <p>Title: UserController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class UserController extends Controller {

	static Warehouse warehouse = SessionSearchUtil.searchWarehouse();
	static String warehouseId = warehouse.id.toString();

	public static Result index() {
		return ok(user.render(""));
	}

	@Transactional
	public static Result initRoles() {
		List<RoleVo> roleVos = new ArrayList<RoleVo>();
		List<Role> roles = new ArrayList<Role>();
		roles = Role.find().where().eq("deleted", false).findList();
		if (roles.size() > 0) {
			for (Role roleTemp : roles) {
				RoleVo roleVo = new RoleVo();
				roleVo.initRole(roleTemp);
				roleVos.add(roleVo);
			}
		}
		return ok(play.libs.Json.toJson(roleVos));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result initWarehouse() {
		List<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
		if (warehouse != null) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.initWarehouse(warehouse);
			warehouseVos.add(warehouseVo);
		}
		System.out.println("++++++++++warehouseVos+++++++++++" + warehouseVos.size());
		return ok(play.libs.Json.toJson(warehouseVos));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result initCompany() {
		List<CompanyVo> companyVos = new ArrayList<CompanyVo>();
		List<Company> companyList = new ArrayList<Company>();
		Warehouse warehouse = SessionSearchUtil.searchWarehouse();
		if (warehouse != null) {
			companyList = Company.find().where().eq("id", warehouse.company.id.toString()).eq("deleted", false).findList();
		}
		for (Company company : companyList) {
			CompanyVo companyVo = new CompanyVo();
			companyVo.initCompany(company);
			companyVos.add(companyVo);
		}
		return ok(play.libs.Json.toJson(companyVos));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result list() {
		Logger.info("================search===begin=================");
		UserSearchVo userSearchVo = new UserSearchVo();
		List<BusinessUserRole> businessUserRoleList = new ArrayList<BusinessUserRole>();
		List<UserVo> userVos = new ArrayList<UserVo>();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			userSearchVo = Json.fromJson(body.asJson(), UserSearchVo.class);
		}
		ExpressionList<BusinessUserRole> el = Ebean.find(BusinessUserRole.class).where().eq("deleted", false);
		if (userSearchVo.name != null && !"".equals(userSearchVo.name)) {
			el.like("businessUser.user.name", "%" + userSearchVo.name + "%");
		}
		if (userSearchVo.roleVo != null && !"".equals(userSearchVo.roleVo)) {
			if (userSearchVo.roleVo.id != null && !"".equals(userSearchVo.roleVo.id)) {
				el.like("role.id", "%" + userSearchVo.roleVo.id + "%");
			}
		}
		businessUserRoleList = el.order("updatedAt descending").findList();

		List<UserWarehouse> userWarehouses = new ArrayList<UserWarehouse>();
		List<BusinessUserRole> businessUserRoles = new ArrayList<BusinessUserRole>();
		List<User> users = new ArrayList<User>();
		for (BusinessUserRole businessUserRole : businessUserRoleList) {
			User user = new User();
			user = User.find().where().eq("deleted", false).eq("id", businessUserRole.businessUser.user.id.toString()).findUnique();
			if (!users.contains(user)) {
				businessUserRoles.addAll(BusinessUserRole.find().where().eq("businessUser.user.id", user.id.toString()).eq("deleted", false)
						.findList());
				users.add(user);
			}
		}
		for (BusinessUserRole bURole : businessUserRoles) {
			WarehouseVo warehouseVo = new WarehouseVo();
			CompanyVo companyVo = new CompanyVo();
			Company company = Company.find().where().eq("id", warehouse.company.id.toString()).eq("deleted", false).findUnique();
			userWarehouses = UserWarehouse.find().where().eq("user.id", bURole.businessUser.user.id.toString()).eq("deleted", false).findList();
			if (userWarehouses.size() > 0) {
				for (UserWarehouse userWarehouse : userWarehouses) {
					warehouseVo.initWarehouse(userWarehouse.warehouse);
				}
			}
			boolean ishave = false;
			if (userVos.size() > 0) {
				for (UserVo uVo : userVos) {
					if (uVo.id.equals(bURole.businessUser.user.id.toString())) {
						ishave = true;
						RoleVo roleVo2 = new RoleVo();
						if (bURole.role != null && !"".equals(bURole.role)) {
							roleVo2.initRole(bURole.role);
							if (uVo.roleNameKeys == null)
								uVo.roleNameKeys = "";
							uVo.roleNameKeys = bURole.role.nameKey + ";" + uVo.roleNameKeys;
						}
						uVo.setUser(bURole.businessUser.user);
						uVo.warehouseVo = warehouseVo;
						companyVo.initCompany(company);
						uVo.companyVo = companyVo;
						if (bURole.updatedAt != null)
							uVo.updateAt = dateToStrShortEn(DateUtil.timestampToDate(bURole.updatedAt));
						uVo.roles.add(roleVo2);
					}
				}
			}
			if (!ishave) {
				UserVo userVo2 = new UserVo();
				userVo2.setUser(bURole.businessUser.user);
				RoleVo roleVo2 = new RoleVo();
				if (bURole.role != null && !"".equals(bURole.role)) {
					roleVo2.initRole(bURole.role);
					if (userVo2.roleNameKeys == null)
						userVo2.roleNameKeys = "";
					userVo2.roleNameKeys = bURole.role.nameKey + ";" + userVo2.roleNameKeys;
				}
				userVo2.roles.add(roleVo2);
				userVo2.warehouseVo = warehouseVo;
				companyVo.initCompany(company);
				userVo2.companyVo = companyVo;
				if (bURole.updatedAt != null)
					userVo2.updateAt = dateToStrShortEn(DateUtil.timestampToDate(bURole.updatedAt));
				userVos.add(userVo2);
			}
		}
		Logger.info("================search===end=================");
		return ok(play.libs.Json.toJson(userVos));
	}

	/**
	 * @return
	 * 是否要唯一性校验？待确认
	 */
	@Transactional
	public static Result save() {
		UserVo userVo = new UserVo();
		User user = new User();
		BusinessUserRole businessUserRole = new BusinessUserRole();
		BusinessUser businessUser = new BusinessUser();
		Role role = new Role();
		Company company = new Company();
		UserWarehouse userWarehouse = new UserWarehouse();
		Warehouse warehouse = new Warehouse();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			userVo = Json.fromJson(body.asJson(), UserVo.class);
		}
		List<User> userTemps = new ArrayList<User>();
		if (userVo.name != null && !"".equals(userVo.name)) {
			user.name = userVo.name;
			userTemps = User.find().where().eq("name", userVo.name).eq("deleted", false).findList();
		}
		if (userVo.email != null && !"".equals(userVo.email))
			user.ext = userVo.returnExt();
		if (userVo.password != null && !"".equals(userVo.password) && userVo.rePassword != null && !"".equals(userVo.rePassword)) {// 密码的保存
			if (!userVo.password.equals(userVo.rePassword)) {
				return badRequest("Two  passwords must be the same,Please check !");
			} else {
				user.passwordHash = userVo.password;
			}
		}
		if (userTemps.size() > 0) {
			return badRequest("Save Failed ,The user has existed !");
		}
		CrudUtil.save(user);// 保存 user 表

		businessUser.user = user;
		if (userVo.companyVo != null && !"".equals(userVo.companyVo) && (userVo.companyVo.id != null && !"".equals(userVo.companyVo.id))) {
			company = Company.find().where().eq("id", userVo.companyVo.id).eq("deleted", false).findUnique();
		}
		if (company != null)
			businessUser.business = company.business;
		CrudUtil.save(businessUser); // 保存businessUser 表

		if (userVo.roles.size() > 0) { // role的保存
			for (RoleVo roleVoTemp : userVo.roles) {
				role = new Role();
				businessUserRole = new BusinessUserRole();
				if (roleVoTemp.id != null && !"".equals(roleVoTemp.id))
					role = Role.find().where().eq("id", roleVoTemp.id).eq("deleted", false).findUnique();
				businessUserRole.role = role;
				businessUserRole.businessUser = businessUser;
				CrudUtil.save(businessUserRole);
			}
		} else {
			businessUserRole = new BusinessUserRole();
			businessUserRole.businessUser = businessUser;
			CrudUtil.save(businessUserRole);
		}

		if (userVo.warehouseVo != null && !"".equals(userVo.warehouseVo) && (userVo.warehouseVo.id != null && !"".equals(userVo.warehouseVo.id))) {
			warehouse = Warehouse.find().where().eq("id", userVo.warehouseVo.id).eq("deleted", false).findUnique();
			if (warehouse != null)
				userWarehouse.warehouse = warehouse;
			userWarehouse.user = user;
			CrudUtil.save(userWarehouse);
		}
		return ok(play.libs.Json.toJson("Save Success!"));
	}

	/**
	 * @return
	 * 是否要唯一性校验？待确认
	 */
	@Transactional
	public static Result update() {
		UserVo userVo = new UserVo();
		List<BusinessUserRole> businessUserRoles = new ArrayList<BusinessUserRole>();
		BusinessUser businessUser = new BusinessUser();
		List<UserWarehouse> userWarehouses = new ArrayList<UserWarehouse>();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			userVo = Json.fromJson(body.asJson(), UserVo.class);
		}
		User user = new User();
		List<User> userTemps = new ArrayList<User>();
		if (userVo != null)
			user = User.find().byId(userVo.id.toString());
		if (userVo.name != null && !"".equals(userVo.name)) {
			user.name = userVo.name;
			userTemps = User.find().where().eq("name", userVo.name).eq("deleted", false).findList();
		}
		if (userVo.email != null && !"".equals(userVo.email))
			user.ext = userVo.returnExt();
		if (userVo.password != null && !"".equals(userVo.password) && userVo.rePassword != null && !"".equals(userVo.rePassword)) {
			if (!userVo.password.equals(userVo.rePassword)) {
				return badRequest("Two  passwords must be the same,Please check !");
			} else {
				user.passwordHash = userVo.password;
			}
		}
		if (userTemps.size() > 0 && !(userTemps.get(0).id.toString().equals(userVo.id.toString()))) {
			return badRequest("Update failed, The User has existed !");
		}
		CrudUtil.update(user);
		businessUser = BusinessUser.find().where()/*
												 * .eq("business.id",
												 * businessId)
												 */.eq("user.id", user.id.toString()).eq("deleted", false).findUnique();// 一个user属于一家集团
		Company company = new Company();
		if (userVo.companyVo != null && !"".equals(userVo.companyVo) && userVo.companyVo.id.toString() != null
				&& !"".equals(userVo.companyVo.id.toString())) {
			company = Company.find().where().eq("id", userVo.companyVo.id).eq("deleted", false).findUnique();
		}
		if (businessUser != null)
			businessUser.business = company.business;
		businessUser.user = user;
		CrudUtil.update(businessUser);

		businessUserRoles = BusinessUserRole.find().fetch("role").where().eq("role.deleted", false).eq("businessUser.id", businessUser.id.toString())
				.eq("deleted", false).findList();//
		List<Role> tempRoles = new ArrayList<Role>();
		if (userVo.roles.size() > 0) {
			for (RoleVo roleVoTemp : userVo.roles) {
				Role role = new Role();
				role = Role.find().where().eq("id", roleVoTemp.id).eq("deleted", false).findUnique();
				tempRoles.add(role);
			}
		} else {
			for (BusinessUserRole businessUserRole : businessUserRoles) {
				businessUserRole.role = null;
				CrudUtil.update(businessUserRole);
			}
		}
		List<String> ids = new ArrayList<String>();
		if (businessUserRoles.size() > 0) {
			for (int i = 0; i < businessUserRoles.size() && businessUserRoles.get(i) != null && businessUserRoles.get(i).role != null; i++) {
				if (userVo.roles.size() > 0 && businessUserRoles.get(i) != null && businessUserRoles.get(i).role != null) {// role的更新
					if (tempRoles.size() > 0) {
						for (int j = 0; j < tempRoles.size() && tempRoles.get(j) != null; j++) {
							if (tempRoles.get(j) == null)
								break;
							String tempid = tempRoles.get(j).id.toString();
							if ((businessUserRoles.get(i).role.id.toString()).equals(tempid)) {
								ids.add(tempid);
							}
						}
					}
				}
			}
		}
		int i = 0;
		for (BusinessUserRole businessUserRole : businessUserRoles) {
			if (businessUserRole.role != null && !ids.contains(businessUserRole.role.id.toString())) {
				i = i + 1;
				businessUserRole.deleted = true;
				CrudUtil.update(businessUserRole);
			} else {
				CrudUtil.update(businessUserRole);
			}
		}
		int j = 0;
		if (userVo.roles.size() > 0) {
			for (RoleVo roleVoTemp : userVo.roles) {
				if (!ids.contains(roleVoTemp.id)) {
					j = j + 1;
					Role role = new Role();
					role = Role.find().where().eq("id", roleVoTemp.id).eq("deleted", false).findUnique();
					BusinessUserRole businessUserRole = new BusinessUserRole();
					businessUserRole.businessUser = businessUser;
					businessUserRole.role = role;
					CrudUtil.save(businessUserRole);
				}
			}
		}

		Warehouse warehouse = new Warehouse();
		if (userVo.warehouseVo != null && !"".equals(userVo.warehouseVo) && userVo.warehouseVo.id != null && !"".equals(userVo.warehouseVo.id)) {
			userWarehouses = UserWarehouse.find().where().eq("user.id", user.id.toString()).eq("deleted", false).findList();
			warehouse = Warehouse.find().where().eq("id", userVo.warehouseVo.id).eq("deleted", false).findUnique();
		}
		if (userWarehouses.size() > 0) {
			for (UserWarehouse userWarehouse : userWarehouses) {
				userWarehouse.user = user;
				userWarehouse.warehouse = warehouse;
				CrudUtil.update(userWarehouse);
			}
		}
		return ok(play.libs.Json.toJson(userVo));
	}

	/**
	 * @param id
	 * @return
	 * 判断能删除的条件？
	 */
	@Transactional
	public static Result delete(String id) {
		Logger.info("================delete===begin=================");
		User user = User.find().where().eq("id", id).eq("deleted", false).findUnique();
		List<BusinessUserRole> businessUserRoles = new ArrayList<BusinessUserRole>();
		List<BusinessUser> businessUsers = new ArrayList<BusinessUser>();
		List<UserWarehouse> userWarehouses = new ArrayList<UserWarehouse>();
		businessUsers = BusinessUser.find().where().eq("user.id", user.id.toString()).eq("user.deleted", false).eq("deleted", false).findList();
		if (businessUsers.size() > 0) {
			for (BusinessUser businessUser : businessUsers) {
				businessUserRoles = BusinessUserRole.find().where().eq("businessUser.id", businessUser.id.toString()).eq("deleted", false).findList();// ???如果该用户被分配到了某个角色则该用户不能删除
				if (businessUserRoles.size() > 0) {
					for (BusinessUserRole businessUserRole : businessUserRoles) {
						businessUserRole.deleted = true;
						CrudUtil.update(businessUserRole);
					}
				}
				userWarehouses = UserWarehouse.find().where().eq("user.id", user.id.toString()).eq("deleted", false).findList();// 如果被分配管理某个仓库则该用户不能删除
				if (userWarehouses.size() > 0) {
					for (UserWarehouse userWarehouse : userWarehouses) {
						userWarehouse.deleted = true;
						CrudUtil.update(userWarehouse);
					}
				}
				businessUser.deleted = true;
				CrudUtil.update(businessUser);
				user.deleted = true;
				CrudUtil.update(user);
			}
		}
		Logger.info("================delete===end=================");
		return ok("delete success!");
	}

	/**
	 * 将时间格式时间转换为字符串 ddMMMyyyy（英文格式）
	 * @param date
	 * @return
	 */
	private static String TIME_PATTERN_EN = "dd/MM/yyyy HH:mm";

	public static String dateToStrShortEn(Date date) {
		DateTime dateDate = DateUtil.dateToDateTime(date);
		return dateDate.toString(TIME_PATTERN_EN, Locale.ENGLISH);
	}

}
