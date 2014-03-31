/** * CodeController.java 
* Created on 2013-5-7 下午3:57:09 
*/

package controllers.setup;

import java.util.List;
import java.util.Map;

import models.Code;
import models.Config;
import models.vo.setup.ConfigVo;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.CrudUtil;
import views.html.setup.config;

import action.Menus;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CodeController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@With(Menus.class)
public class ConfigController extends Controller {

	/**
	 * 
	 * @return
	 */
	public static Result index() {
		return ok(config.render(""));
	}

	/**
	 * 
	 * @return
	 */
	public static Result saveOrUpdate() {
		System.out.println("^^^^^^^^^^^^^^^^^^^you have in saveOrUpdate^^^^^^^^^^^^");
		RequestBody body = request().body();
		boolean flag = false;

		StringBuffer sb = new StringBuffer();
		if (body != null && body.asJson() != null) {
			System.out.println("^^^^^body.asJson " + body.asJson());
			ConfigVo configVo = Json.fromJson(body.asJson(), ConfigVo.class);
			if (configVo.cat == null) {
				flag = true;
				sb.append(" Parameter Cat ");
			}
			if (configVo.code == null) {
				flag = true;
				sb.append(" Parameter Code");
			}
			if (configVo.desc == null) {
				flag = true;
				sb.append(" Parameter Desc");
			}
			if (configVo.value == null) {
				flag = true;
				sb.append(" Parameter Value");
			}
			if (flag) {
				return badRequest(sb.append(" cannot null").toString());
			}
			ConfigVo.setConfig(configVo);

			return ok("you hava operate success ");
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
			@SuppressWarnings("unchecked")
			Map<String,Object> searchMap = Json.fromJson(body.asJson(), Map.class);
			List<ConfigVo> configVos = ConfigVo.getConfig(searchMap);
			System.out.println("^^^^^Json.toJson(configVos) : " + Json.toJson(configVos));
			return ok(Json.toJson(configVos));
		} else {
			return badRequest("System Error");
		}

	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result delete(String id) {
		System.out.println("^^^^^^^^^^^   have in save delete method   ^^^^^parma id is :^^^^" + id);
		Config config = Ebean.find(Config.class).where().eq("deleted", false).eq("id", id).findUnique();
		CrudUtil.delete(config);
		return ok("delete success!");
	}

}
