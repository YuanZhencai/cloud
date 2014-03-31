package models.vo.Result;

import org.codehaus.jackson.JsonNode;

import play.api.libs.json.Json;

public class ResultVo {
	public String Type="success";
	public Object Data;
	public String Message;
	public ResultVo(String Type,Object Data,String Message){
		this.Type=Type;
		this.Data=Data;
		this.Message=Message;
	}
	public ResultVo(Object Data){
		this.Data=Data;
	}
	public ResultVo(String Type,String Message){
		this.Type=Type;
		this.Message=Message;
	}
}
