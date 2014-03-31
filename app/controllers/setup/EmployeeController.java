/** * EmployeeController.java 
* Created on 2013-5-7 下午3:49:49 
*/

package controllers.setup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Company;
import models.Employee;
import models.EmployeeType;
import models.EmployeeWarehouse;
import models.Warehouse;
import models.vo.setup.CompanyVo;
import models.vo.setup.EmployeeSearchVo;
import models.vo.setup.EmployeeTypeVo;
import models.vo.setup.EmployeeVo;
import models.vo.setup.WarehouseVo;

import org.joda.time.DateTime;

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
import views.html.setup.employee;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/**
 *  
* <p>Project: CloudWMS</p> 
* <p>Title: EmployeeController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class EmployeeController extends Controller {

	public static Result index() {
		return ok(employee.render(""));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result initPlant() {
		List<CompanyVo> companyVos = new ArrayList<CompanyVo>();
		// List<Company> companyList = Company.find().where().eq("deleted",
		// false).findList();// 目前是查询全部公司
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
	public static Result initEmployeeType() {
		List<EmployeeTypeVo> employeeTypeVos = new ArrayList<EmployeeTypeVo>();
		List<EmployeeType> employeeTypeList = EmployeeType.find().where().eq("deleted", false).findList();// .eq("company.id",companyId)//
																											// 查询所有EmployeeType
		for (EmployeeType employeeType : employeeTypeList) {
			EmployeeTypeVo employeeTypeVo = new EmployeeTypeVo();
			employeeTypeVo.initEmployeeType(employeeType);
			employeeTypeVos.add(employeeTypeVo);
		}
		return ok(play.libs.Json.toJson(employeeTypeVos));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result initWarehouse() {
		List<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
		// List<Warehouse> warehouseList =
		// Warehouse.find().where().eq("company.id", companyId).eq("deleted",
		// false).findList();
		Warehouse warehouse = SessionSearchUtil.searchWarehouse();
		if (warehouse != null) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.initWarehouse(warehouse);
			warehouseVos.add(warehouseVo);
		}
		return ok(play.libs.Json.toJson(warehouseVos));
	}

	/**
	 * @return
	 */
	@Transactional
	public static Result list() {
		EmployeeSearchVo employeeSearchVo = new EmployeeSearchVo();
		List<Employee> employeeList = new ArrayList<Employee>();
		List<EmployeeVo> employeeVos = new ArrayList<EmployeeVo>();

		RequestBody body = request().body();
		if (body.asJson() != null) {
			employeeSearchVo = Json.fromJson(body.asJson(), EmployeeSearchVo.class);
		}

		ExpressionList<Employee> el = Ebean.find(Employee.class).where().eq("deleted",Boolean.FALSE);
		if (employeeSearchVo.companyVo != null && !"".equals(employeeSearchVo.companyVo)) {
			if (employeeSearchVo.companyVo.id != null && !"".equals(employeeSearchVo.companyVo.id))
				el.like("company.id", employeeSearchVo.companyVo.id.toString());
		}
		/*if (employeeSearchVo.active != null && !"".equals(employeeSearchVo.active))
			el.eq("deleted", Boolean.valueOf(employeeSearchVo.active));*/
		if (employeeSearchVo.employeeName != null && !"".equals(employeeSearchVo.employeeName))
			el.like("employeeName", "%" + employeeSearchVo.employeeName + "%");
		if (employeeSearchVo.employeeTypeVo != null && !"".equals(employeeSearchVo.employeeTypeVo)) {
			if (employeeSearchVo.employeeTypeVo.id != null && !"".equals(employeeSearchVo.employeeTypeVo.id))
				el.like("employeeType.id", employeeSearchVo.employeeTypeVo.id.toString());
		}
		employeeList = el.order("updatedAt descending").findList();
		if (employeeList.size() > 0) {
			for (Employee employee : employeeList) {
				EmployeeVo voTemp = new EmployeeVo();
				CompanyVo companyVo = new CompanyVo();
				EmployeeTypeVo employeeTypeVo = new EmployeeTypeVo();
				WarehouseVo warehouseVo = new WarehouseVo();
				companyVo.id = employee.company.id.toString();
				companyVo.nameKey = employee.company.nameKey;
				voTemp.companyVo = companyVo;
				List<EmployeeWarehouse> employeeWarehouses = new ArrayList<EmployeeWarehouse>();
				employeeWarehouses = EmployeeWarehouse.find().where().eq("employee.id", employee.id.toString()).findList()/*
																														 * .
																														 * eq
																														 * (
																														 * "deleted"
																														 * ,
																														 * false
																														 * )
																														 */;
				if (employeeWarehouses.size() > 0) {
					for (EmployeeWarehouse employeeWarehouse : employeeWarehouses) {
						warehouseVo.id = employeeWarehouse.warehouse.id.toString();
						warehouseVo.nameKey = employeeWarehouse.warehouse.nameKey;
						voTemp.warehouseVo = warehouseVo;
					}
				}
				voTemp.employeeName = employee.employeeName;
				voTemp.employeeCode = employee.employeeCode;
				employeeTypeVo.id = employee.employeeType.id.toString();
				employeeTypeVo.nameKey = employee.employeeType.nameKey;
				voTemp.employeeTypeVo = employeeTypeVo;
				voTemp.delete = employee.deleted;
				if (employee.updatedAt != null)
					voTemp.updateAt = dateToStrShortEn(DateUtil.timestampToDate(employee.updatedAt));
				voTemp.id = employee.id.toString();
				if (employee.deleted == true) {
					voTemp.delete = true;
					if (voTemp.delete) {
						voTemp.active = "No";
					}
				}
				employeeVos.add(voTemp);
			}
		}
		return ok(play.libs.Json.toJson(employeeVos));
	}

	/**
	 * @return
	 * 是否要唯一性校验？待确认
	 */
	@Transactional
	public static Result save() {
		EmployeeVo employeeVo = new EmployeeVo();
		Employee employee = new Employee();
		Company company = new Company();
		Warehouse warehouse = new Warehouse();
		EmployeeWarehouse employeeWarehouse = new EmployeeWarehouse();
		EmployeeType employeeType = new EmployeeType();
		List<Employee> employees = new ArrayList<Employee>();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			employeeVo = Json.fromJson(body.asJson(), EmployeeVo.class);
		}
		if (employeeVo.companyVo != null && !"".equals(employeeVo.companyVo)) {
			company = Company.find().where().eq("id", employeeVo.companyVo.id).eq("deleted", false).findUnique();
		}
		if (company != null)
			employee.company = company;
		if (employeeVo.employeeName != null && !"".equals(employeeVo.employeeName))
			employee.employeeName = employeeVo.employeeName;
		if (employeeVo.employeeCode != null && !"".equals(employeeVo.employeeCode)) {
			employee.employeeCode = employeeVo.employeeCode;
			employees = Employee.find().where().eq("employeeCode", employeeVo.employeeCode).eq("deleted", false).findList();
		}
		if (employees.size() > 0) {
			return badRequest("Save Employee Failed,EmployeeCode has existed ! ");
		}
		if (employeeVo.employeeTypeVo != null && !"".equals(employeeVo.employeeTypeVo))
			employeeType = EmployeeType.find().where().eq("id", employeeVo.employeeTypeVo.id).eq("deleted", false).findUnique();
		if (employeeType != null)
			employee.employeeType = employeeType;
		// if (employeeVo.delete != null && !"".equals(employeeVo.delete))
		employee.deleted = Boolean.valueOf(employeeVo.delete);
		CrudUtil.save(employee);

		if (employeeVo.warehouseVo != null && !"".equals(employeeVo.warehouseVo)) {
			warehouse = Warehouse.find().where().eq("id", employeeVo.warehouseVo.id).eq("deleted", false).findUnique();
		}
		if (warehouse != null)
			employeeWarehouse.warehouse = warehouse;
		employeeWarehouse.employee = employee;
		CrudUtil.save(employeeWarehouse);

		return ok(play.libs.Json.toJson("Save Success!"));
	}

	/**
	 * @return
	 * 是否要唯一性校验？待确认
	 */
	@Transactional
	public static Result update() {
		EmployeeVo employeeVo = new EmployeeVo();
		List<Employee> employees = new ArrayList<Employee>();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			employeeVo = Json.fromJson(body.asJson(), EmployeeVo.class);
		}
		Employee employee = new Employee();
		if (employeeVo != null)
			employee = Employee.find().byId(employeeVo.id.toString());
		Company company = new Company();
		EmployeeType employeeType = new EmployeeType();
		if (employeeVo.companyVo != null && !"".equals(employeeVo.companyVo)) {
			company = Company.find().where().eq("id", employeeVo.companyVo.id).eq("deleted", false).findUnique();
		}
		if (company != null)
			employee.company = company;
		if (employeeVo.employeeName != null && !"".equals(employeeVo.employeeName))
			employee.employeeName = employeeVo.employeeName;
		if (employeeVo.employeeCode != null && !"".equals(employeeVo.employeeCode)) {
			employee.employeeCode = employeeVo.employeeCode;
			employees = Employee.find().where().eq("employeeCode", employeeVo.employeeCode).eq("deleted", false).findList();
		}
		if ("Yes".equals(employeeVo.active)) {
			employeeVo.delete = false;
		} else {
			employeeVo.delete = true;
		}
		if (employees.size() > 0) {
			for (Employee em : employees) {
				if (!(em.id.toString().equals(employeeVo.id.toString())) && !employeeVo.delete)
					return badRequest("Update Employee Failed,EmployeeCode has existed ! ");
			}
		}
		if (employeeVo.employeeTypeVo != null && !"".equals(employeeVo.employeeTypeVo))
			employeeType = EmployeeType.find().where().eq("id", employeeVo.employeeTypeVo.id).eq("deleted", false).findUnique();
		if (employeeType != null)
			employee.employeeType = employeeType;
		if ("Yes".equals(employeeVo.active)) {
			employeeVo.delete = false;
		} else {
			employeeVo.delete = true;
		}
		employee.deleted = employeeVo.delete;
		CrudUtil.update(employee);

		Warehouse warehouse = new Warehouse();
		List<EmployeeWarehouse> employeeWarehouses = new ArrayList<EmployeeWarehouse>();
		employeeWarehouses = EmployeeWarehouse.find().where().eq("employee.id", employee.id.toString()).eq("deleted", false).findList();
		if (employeeVo.warehouseVo != null && !"".equals(employeeVo.warehouseVo)) {
			warehouse = Warehouse.find().where().eq("id", employeeVo.warehouseVo.id).eq("deleted", false).findUnique();
		}
		if (employeeWarehouses.size() > 0) {
			for (EmployeeWarehouse employeeWarehouse : employeeWarehouses) {
				if (warehouse != null)
					employeeWarehouse.warehouse = warehouse;
				employeeWarehouse.employee = employee;
				if ("Yes".equals(employeeVo.active)) {
					employeeVo.delete = false;
				} else {
					employeeVo.delete = true;
				}
				employeeWarehouse.deleted = employeeVo.delete;
				CrudUtil.update(employeeWarehouse);
			}
		}

		return ok(play.libs.Json.toJson(employeeVo));
	}

	/**
	 * @param id
	 * @return
	 * 判断能删除的条件？
	 */
	@Transactional
	public static Result delete(String id) {
		Employee employee = Employee.find().where().eq("id", id).eq("deleted", false).findUnique();
		List<EmployeeWarehouse> employeeWarehouses = EmployeeWarehouse.find().where().eq("employee.id", employee.id.toString()).eq("deleted", false)
				.findList();
		if (employeeWarehouses.size() > 0) {
			for (EmployeeWarehouse employeeWarehouse : employeeWarehouses) {
				if (employee != null)
					employee.deleted = true;
				CrudUtil.update(employee);
				employeeWarehouse.deleted = true;
				CrudUtil.update(employeeWarehouse);
			}
		}
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
