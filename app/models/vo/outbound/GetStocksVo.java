package models.vo.outbound;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import play.libs.Json;

public class GetStocksVo {
	public String piNo;
	public HashMap<String,StuffingVo> batchs;
	@JsonProperty("batchs")
	public void setbatchs(String batchs){
		 JsonNode parse = Json.parse(batchs);
		try {
			this.batchs= new ObjectMapper().readValue(parse, new TypeReference<HashMap<String,StuffingVo>>() { });
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
