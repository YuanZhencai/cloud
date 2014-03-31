package models.printVo.planAndRealization;

import java.util.Date;

import models.vo.query.BinVo;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import play.libs.Json;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintDetial {
	public Date dateFrom;
	public Date dateTo;
	public Date printDate;
	public String intProdNo;
	public String style;
}
