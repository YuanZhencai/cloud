/** * CodeController.java 
* Created on 2013-5-7 下午3:57:09 
*/

package controllers.setup;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import action.Menus;

import com.avaje.ebean.Ebean;
 

import models.Code;
import models.CodeCat;
import models.Warehouse;
import models.vo.setup.CodeVo;
import models.vo.setup.SearchVo;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utils.CrudUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelHelper;
import views.html.setup.code;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CodeController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@With(Menus.class)
public class CodeController extends Controller {

	/**
	 * 
	 * @return
	 */
	public static Result index() {
		return ok(code.render(""));
	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	public static Result saveOrUpdate() {
		System.out.println("^^^^^^^^^^^^^^^^^^^you have in saveOrUpdate^^^^^^^^^^^^");
		RequestBody body = request().body();
		boolean flag = false;

		StringBuffer sb = new StringBuffer();
		if (body != null && body.asJson() != null) {
			System.out.println("^^^^^body.asJson " + body.asJson());
			CodeVo codeVo = Json.fromJson(body.asJson(), CodeVo.class);
			if (codeVo.codeCat == null) {
				flag = true;
				sb.append(" CodeCat ");
			}
			if (codeVo.abbr == null) {
				flag = true;
				sb.append(" Abbr ");
			}
			if (codeVo.desc == null) {
				flag = true;
				sb.append(" Desc ");
			}
			if (flag) {
				return badRequest(sb.append(" cannot null").toString());
			}
			CodeVo.setCode(codeVo);

			return ok("you hava  success operate");
		} else {
			return badRequest("System Error");
		}

	}

	/**
	 * 
	 * @return
	 */
	public static Result list() {
		System.out.println("************* you have in list method ****************");
		RequestBody body = request().body();
		if (body != null && body.asJson() != null) {
			System.out.println("^^^^^body.asJson " + body.asJson());
			SearchVo searchVo = Json.fromJson(body.asJson(), SearchVo.class);
			List<CodeVo> codeVos = CodeVo.getCode(searchVo);
			return ok(Json.toJson(codeVos));
		} else {
			return badRequest("System Error");
		}

	}
	@Transactional
	public static Result upload() {
		System.out.println("************* you have in upload method ****************");
		MultipartFormData body = request().body().asMultipartFormData();
			FilePart file = body.getFile("file");
			if (file != null) {
				String fileName = file.getFilename();
				String contentType = file.getContentType();
				File excel = file.getFile();
				System.out.println("file:  "+file+"  fileName: "+ fileName+"  contentType: "+contentType);
				Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String,Object>>>();
				try {
					map = ExcelHelper.exportDataFromExcel(file, null);
					System.out.println("++++++++++++++++++++++++++++++++++"+map);
					//1、 校验excel中的值是否正确
					// validateData(map);
					//2、唯一性检查 ，看数据库中是否有相同的数据
					//uniqueCheckData(map);
					//3 保存进数据库
					 setCodes(map);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return redirect(routes.MaterialMasterController.index());
			} else {
				return redirect(routes.MaterialMasterController.index());
			}
	}
	/**
	 * 
	 * @param map中的数据保存到Code和CodeCat
	 */
	public static void setCodes(Map<String, List<Map<String, Object>>> map){
		if(map!=null&&!map.isEmpty()){
			CodeCat codeCat = null;
			Boolean flag = true;
			Warehouse wh = SessionSearchUtil.searchWarehouse();
			for(Map.Entry<String, List<Map<String, Object>>> entry :map.entrySet()){
				List<Map<String, Object>> list = entry.getValue();
				if(list!=null&&!list.isEmpty()){
					for(Map<String, Object> subMap:list){
						flag = true;
						List<CodeCat> codeCates = Ebean.find(CodeCat.class).where().eq("deleted", false).eq("catKey", String.valueOf(subMap.get("CodeCat")).trim()).findList();
						if (codeCates != null && !codeCates.isEmpty()) {
							codeCat = codeCates.get(0);
							flag = false;
						}
					   if(flag){ 
						   codeCat = new CodeCat();
						    codeCat.business = wh.company.business;
							codeCat.company = wh.company;
							codeCat.warehouse = wh;
							codeCat.catLevel = null;
							codeCat.catKey = String.valueOf(subMap.get("CodeCat"));
							codeCat.nameKey = String.valueOf(subMap.get("CodeCat"));
							codeCat.sysCodeCat = false;
							codeCat.remarks = null;
							codeCat.ext = null;
							// codeCat.version = 1;
							codeCat.schemaCode = null;
							CrudUtil.save(codeCat);
						}else{
							//如果数据库中对应CodeCat有此条数据则推出这次循环
							List<Code> codes = Ebean.find(Code.class).fetch("codeCat").where().eq("deleted", false).eq("codeCat", codeCat).eq("codeKey",String.valueOf(subMap.get("CodeCat")).trim()).findList();
							if(codes!=null&&!codes.isEmpty()){
								continue;
							}
							
						}
						// code
					   Code code = new Code(); 
						code.codeKey =String.valueOf(subMap.get("CodeKey"));
						code.catKey =  String.valueOf(subMap.get("CodeCat"));
						code.nameKey = String.valueOf(subMap.get("NameKey"));
						code.sortNo = 1;
						code.remarks = String.valueOf(subMap.get("Remarks"));
						code.ext = null;
						// code.version = 1;
						code.schemaCode = null;
						CrudUtil.save(code);	
					}
				}
			}
			
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result delete(String id) {
		System.out.println("^^^^^^^^^^^   have in save delete method   ^^^^^parma id is :^^^^" + id);
		Code code = Ebean.find(Code.class).where().eq("deleted", false).eq("id", id).findUnique();
		CrudUtil.delete(code);
		return ok("delete success!");
	}

}
