/** * GoodsTransferDetailsController.java 
* Created on 2013-8-12 下午3:06:07 
*/

package controllers.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Execution;
import models.StockTransaction;
import models.printVo.Cell;
import models.printVo.goodsTransferDetails.GoodsTransferDetailsData;
import models.printVo.goodsTransferDetails.GoodsTransferDetailsRePort;
import models.printVo.goodsTransferDetails.GoodsTransferDetailsSearchVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.ChainList;
import utils.EmptyUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import utils.enums.CodeKey;
import utils.exception.CustomException;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: GoodsTransferDetailsController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class GoodsTransferDetailsController extends Controller {
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
		return ok(views.html.report.GoodsTransferDetails.render(""));
	}

	/**
	 * 
	 * @return
	 */
	public static Result list() {
		logger.info("^^^^^^^^^^^you have in list method()^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		GoodsTransferDetailsSearchVo gtdVo = null;
		if (body.asJson() != null) {
			//logger.info("Query  Condition is ：" + body.asJson());
			gtdVo = Json.fromJson(body.asJson(), GoodsTransferDetailsSearchVo.class);
		} else {
			gtdVo = new GoodsTransferDetailsSearchVo();
		}
		List<GoodsTransferDetailsData> gtdVodList = serchList(gtdVo);

		return ok(Json.toJson(gtdVodList));
	}

	private static final Form<GoodsTransferDetailsSearchVo> requestMap = Form.form(GoodsTransferDetailsSearchVo.class);

	/**
	 * 
	 * @return
	 */
	public static Result downLoad() {
		logger.info("^^^^^^^^^^^you have in downLoad method()^^^^^^^^^^^^^^");
		GoodsTransferDetailsSearchVo gtdVo = null;
		List<GoodsTransferDetailsRePort> ppdrs = new ArrayList<GoodsTransferDetailsRePort>();
		if (!requestMap.hasErrors()) {
			gtdVo = requestMap.bindFromRequest().get();
		} else {
			gtdVo = new GoodsTransferDetailsSearchVo();
		}
		List<GoodsTransferDetailsData> gtddList = serchList(gtdVo);
		//logger.info("prdVodList is : " + gtddList);
		if (gtddList.isEmpty()) {
			gtddList.add(new GoodsTransferDetailsData());
		}
		GoodsTransferDetailsRePort gtdr = createReport("Goods Transfer Details", new Date());
		gtdr.setDates(gtddList);
		ppdrs.add(gtdr);
		File file = ReadExcel.outExcel(ppdrs, gtdr.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + gtdr.getIntProdNo() + ".xlsx");
		return ok(file);
	}

	/*
	 * 样式规划
	 */
	public static GoodsTransferDetailsRePort createReport(String intProdNo, Date reportDate) {
		GoodsTransferDetailsRePort gtdr = new GoodsTransferDetailsRePort();
		gtdr.setIntProdNo(intProdNo);
		gtdr.setReportDate(reportDate);

		gtdr.setSheetName(intProdNo);
		// head
		ChainList<ExcelObj> headList = new ChainList<ExcelObj>();
		Cell cell06 = new Cell(intProdNo, 0, 3);
		headList.chainAdd(cell06);
		// foot
		ChainList<ExcelObj> footList = new ChainList<ExcelObj>();
		gtdr.setFoot(footList);
		gtdr.setHead(headList);
		return gtdr;
	}

	/**
	 * 
	 * @param gtdVo
	 * @return
	 */
	public static List<GoodsTransferDetailsData> serchList(GoodsTransferDetailsSearchVo gtdVo) {

		List<GoodsTransferDetailsData> gtdVodList = new ArrayList<GoodsTransferDetailsData>();
		ExpressionList<StockTransaction> el = Ebean.find(StockTransaction.class)
			.where().join("stock", "ext")
			.where().join("stock.batch")
			.where().join("execution.fromMaterialUom", "uomCode")
			.where().join("execution.material", "materialName")
			.where().join("execution.fromArea", "areaCode")
			.where().join("execution.fromBin", "binCode")
			.where().join("execution.toArea", "areaCode")
			.where().join("execution.toBin", "binCode")
			.where().eq(DELETED, false).eq("execution.executionType", CodeKey.EXECUTION_TYPE_TRANSFER.getValue()); 
		if (gtdVo.dateTimeFrom != null) {
			el.ge("createdAt", gtdVo.dateTimeFrom);
		}
		if (gtdVo.dateTimeTo != null) {
			el.le("createdAt", gtdVo.dateTimeTo);
		}
 
		GoodsTransferDetailsData gtdData = null;
		List<StockTransaction> stockTransactions = el.findList();
 
		if (EmptyUtil.isNotEmptyList(stockTransactions)) {
			for (StockTransaction st : stockTransactions) {
				gtdData = new GoodsTransferDetailsData();
				try {
					gtdData.setGoodsTransferDetailsData(st);
					gtdVodList.add(gtdData);
				} catch (CustomException e) {
				}
			}
		}
		//logger.info("gtdVodList is : " + Json.toJson(gtdVodList));
		return gtdVodList;
	}

}
