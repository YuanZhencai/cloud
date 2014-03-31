package models.printVo.planAndRealization;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import play.libs.Json;
public class SearchVo {
	public String printDetial;
	public PrintDetial  getVo(){
		JsonNode parse = play.libs.Json.parse(printDetial);
		return Json.fromJson(parse,PrintDetial.class);
	}
}
