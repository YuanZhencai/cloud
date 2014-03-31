package models.vo.outbound;

import models.Code;

public class OrderStatus {
	public String nameKey;
	public String codeKey;
	public void inCode(Code code){
		nameKey=code.nameKey;
		codeKey=code.codeKey;
	}
}
