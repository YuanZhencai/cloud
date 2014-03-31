/** * CodeController.java 
* Created on 2013-5-7 下午3:57:09 
*/

package controllers.setup;

import java.util.List;
import java.util.Map;

import models.Code;
import models.WarehouseSequence;
import models.vo.setup.CodeVo;
import models.vo.setup.SequenceVo;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.CrudUtil;
import views.html.setup.sequence;

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
public class SequenceController extends Controller {

	/**
	 * 
	 * @return
	 */
	public static Result index() {
		return ok(sequence.render(""));
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
			SequenceVo sequenceVo = Json.fromJson(body.asJson(), SequenceVo.class);
			if (sequenceVo.code == null) {
				flag = true;
				sb.append(" Code ");
			}
			if (sequenceVo.format == null) {
				flag = true;
				sb.append(" Format ");
			}
			if (sequenceVo.startNo == null) {
				flag = true;
				sb.append(" StartNo ");
			}
			if (sequenceVo.endNo== null) {
				flag = true;
				sb.append(" EndNo ");
			}
			if (flag) {
				return badRequest(sb.append(" cannot null").toString());
			}
			SequenceVo.setSequence(sequenceVo);

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
			@SuppressWarnings("unchecked")
			Map<String,Object> searchMap = Json.fromJson(body.asJson(), Map.class);
			List<SequenceVo> sequenceVos = SequenceVo.getSequence(searchMap);
			System.out.println("^^^^^Json.toJson(sequenceVos) : " + Json.toJson(sequenceVos));
			return ok(Json.toJson(sequenceVos));
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
		WarehouseSequence sequence = Ebean.find(WarehouseSequence.class).where().eq("deleted", false).eq("id", id).findUnique();
		CrudUtil.delete(sequence);
		return ok("delete success!");
	}

}
