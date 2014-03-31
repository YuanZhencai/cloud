package models.vo.query;

import models.Code;

public class TransTypeVo {
	public String nameKey;
	public String codeKey;
	public void inCode(Code code){
		nameKey=code.nameKey;
		codeKey=code.codeKey;
	}
}
