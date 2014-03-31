/** * PackProductDetailsController.java 
* Created on 2013-8-2 下午3:39:46 
*/

package controllers.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.OrderItem;
import models.printVo.Cell;
import models.printVo.packProductDetail.PackProductDetailReport;
import models.printVo.packProductDetail.PackProductDetailsData;
import models.vo.report.PackProudctDetailSearchVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import security.NoUserDeadboltHandler;
import utils.ChainList;
import utils.CheckUtil;
import utils.EmptyUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import utils.enums.CodeKey;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackProductDetailsController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class PackProductDetailsController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(PackProductDetailsController.class);
	private static final String DELETED = "deleted";

	/**
	 * 
	 * @return
	 */
	@SubjectPresent(handler = NoUserDeadboltHandler.class)
	@With(Menus.class)
	public static Result index() {
		logger.info("^^^^^^^^^^^you have in index method()^^^^^^^^^^^^^^");
		return ok(views.html.report.PackProductDetails.render(""));
	}

	/**
	 * 
	 * @return
	 */
	public static Result list() {
		logger.info("^^^^^^^^^^^you have in list method()^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		PackProudctDetailSearchVo prdsVo = null;
		if (body.asJson() != null) {
			// logger.info("Query  Condition is ：" + body.asJson());
			prdsVo = Json.fromJson(body.asJson(), PackProudctDetailSearchVo.class);
		} else {
			prdsVo = new PackProudctDetailSearchVo();
		}
		List<PackProductDetailsData> prdVodList = serchList(prdsVo);
		return ok(Json.toJson(prdVodList));
	}

	private static final Form<PackProudctDetailSearchVo> requestMap = Form.form(PackProudctDetailSearchVo.class);

	/**
	 * 
	 * @return
	 */
	public static Result downLoad() {
		logger.info("^^^^^^^^^^^you have in downLoad method()^^^^^^^^^^^^^^");
		PackProudctDetailSearchVo prdsVo = null;
		List<PackProductDetailReport> ppdrs = new ArrayList<PackProductDetailReport>();
		if (!requestMap.hasErrors()) {
			prdsVo = requestMap.bindFromRequest().get();
		} else {
			prdsVo = new PackProudctDetailSearchVo();
		}
		List<PackProductDetailsData> prdVodList = serchList(prdsVo);
		// logger.info("prdVodList is : " + prdVodList);
		if (prdVodList.isEmpty()) {
			prdVodList.add(new PackProductDetailsData());
		}
		PackProductDetailReport ppdr = createReport("Pack Product Detail", new Date(), prdsVo.localOrExport);
		ppdr.setDates(prdVodList);
		ppdrs.add(ppdr);
		File file = ReadExcel.outExcel(ppdrs, ppdr.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + ppdr.getIntProdNo() + ".xlsx");
		return ok(file);
	}

	/*
	 * 样式规划
	 */
	public static PackProductDetailReport createReport(String intProdNo, Date reportDate, String isloacle) {
		PackProductDetailReport ppdr = new PackProductDetailReport();
		ppdr.setIntProdNo(intProdNo);
		ppdr.setReportDate(reportDate);
		ppdr.setStyle(isloacle);
		ppdr.setSheetName(intProdNo);
		// head
		ChainList<ExcelObj> headList = new ChainList<ExcelObj>();
		Cell cell06 = new Cell(intProdNo, 0, 6);
		headList.chainAdd(cell06);
		// foot
		ChainList<ExcelObj> footList = new ChainList<ExcelObj>();
		ppdr.setFoot(footList);
		ppdr.setHead(headList);
		return ppdr;
	}

	public static List<PackProductDetailsData> serchList(PackProudctDetailSearchVo prdsVo) {

		List<PackProductDetailsData> prdVodList = new ArrayList<PackProductDetailsData>();

		ExpressionList<OrderItem> el = Ebean.find(OrderItem.class).where().eq(DELETED, false)
				.eq("order.orderType", CodeKey.ORDER_TYPE_PRODUCE.getValue()).ne("itemStatus", CodeKey.ORDER_STATUS_NEW.toString());
		if (EmptyUtil.isNotEmtpyString(prdsVo.piNo)) {
			el.eq("order.internalOrderNo", prdsVo.piNo);
		}
		if (prdsVo.dateTimeFrom != null) {
			el.ge("createdAt", prdsVo.dateTimeFrom);
		}
		if (prdsVo.dateTimeTo != null) {
			el.le("createdAt", prdsVo.dateTimeTo);
		}
		List<OrderItem> pis = el.findList();
		logger.info("search  pi size :" + pis.size());
		boolean flag = false;
		if (EmptyUtil.isNotEmptyList(pis)) {
			for (OrderItem pi : pis) {
				flag = false;
				if ("Local".equals(prdsVo.localOrExport)) {
					if (!CheckUtil.exportCheck(pi.order.internalOrderNo)) {
						flag = true;
					}
				} else {
					if (CheckUtil.exportCheck(pi.order.internalOrderNo)) {
						flag = true;
					}
				}
				if (flag) {
					List<OrderItem> cargoPis = Ebean.find(OrderItem.class).where().eq(DELETED, false)
							.eq("order.orderType", CodeKey.ORDER_TYPE_CARGO.getValue()).eq("order.internalOrderNo", pi.order.internalOrderNo)
							.findList();
					if (EmptyUtil.isEmptyList(cargoPis)) {
						PackProductDetailsData pcdVo = new PackProductDetailsData();
						pcdVo.setPackProductDetailsVo(pi);
						prdVodList.add(pcdVo);
					}
				}
			}
		}
		// logger.info("prdVodList is : " + Json.toJson(prdVodList));
		return prdVodList;
	}

}
