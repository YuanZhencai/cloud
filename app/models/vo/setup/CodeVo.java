/** * CodeVo.java 
* Created on 2013-5-7 下午4:40:33 
*/

package models.vo.setup;

import java.util.ArrayList;
import java.util.List;

import models.Code;
import models.CodeCat;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CodeVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeVo {
	public String id ;
	public String codeCat;
	public String abbr;
	public String desc;
	public String parent;
	public String magic;
  /**
   * 
   * @param codeVo
   * @throws Exception
   */
	public static void setCode(CodeVo codeVo) {
		CodeCat codeCat = new CodeCat();
		Code code = new Code();
		boolean flagcat = true;
		boolean flagCode = true;
		if(codeVo.id!=null&&!"".equals(codeVo.id.trim())){
			code = Ebean.find(Code.class).where().eq("deleted", false).eq("id", codeVo.id).findUnique();
			flagCode = false;
		}
		List<CodeCat> codeCates = Ebean.find(CodeCat.class).where().eq("deleted", false).eq("catKey", codeVo.codeCat).findList();
		if (codeCates != null && !codeCates.isEmpty()) {
			codeCat = codeCates.get(0);
			flagcat = false;
		}
		
		code.codeCat = codeCat;
		// codeCat
		if (flagcat) {
			codeCat.business = null;
			codeCat.company = null;
			codeCat.warehouse = null;
			codeCat.catLevel = null;
			codeCat.catKey = codeVo.codeCat;
			codeCat.nameKey = codeVo.codeCat;
			codeCat.sysCodeCat = false;
			codeCat.remarks = null;
			codeCat.ext = null;
			// codeCat.version = 1;
			codeCat.schemaCode = null;
			CrudUtil.save(codeCat);
		}
		// code
		code.codeKey = codeVo.abbr;
		code.catKey = codeVo.codeCat;
		code.nameKey = codeVo.desc;
		code.sortNo = 1;
		code.remarks = null;
		code.ext = null;
		// code.version = 1;
		code.schemaCode = null;
		if(flagCode){
			CrudUtil.save(code);	
		}else{
			CrudUtil.update(code);
		}
	}

	  /**
	   * 
	   * @param searchVo
	   * @return
	   */
	public static List<CodeVo> getCode(SearchVo searchVo) {
		List<CodeVo> codeVos = new ArrayList<CodeVo>();
		ExpressionList<Code> el = Ebean.find(Code.class).where().eq("deleted", false);
		if (searchVo.codeCat != null && !"".equals(searchVo.codeCat.trim())) {
			el.like("catKey", "%" + searchVo.codeCat + "%");
		}
		if (searchVo.abbr != null && !"".equals(searchVo.abbr.trim())) {
			el.like("codeKey", "%" + searchVo.abbr + "%");
		}
		if (searchVo.desc != null && !"".equals(searchVo.desc.trim())) {
			el.like("nameKey", "%" + searchVo.desc + "%");
		}
		if (searchVo.parent != null && !"".equals(searchVo.parent.trim())) {
			// el.like("catKey", "'%" + searchVo.parent + "%'");
		}
		if (searchVo.magic != null && !"".equals(searchVo.magic.trim())) {
			// el.like("catKey", "'%" + searchVo.magic + "%'" );
		}
		List<Code> codes = el.order("updatedAt descending").findList();
		if (codes != null && !codes.isEmpty()) {
			for (Code code : codes) {
				CodeVo codeVo = new CodeVo();
				codeVo.id = String.valueOf(code.id);
				codeVo.abbr = code.codeKey;
				codeVo.codeCat = code.catKey;
				codeVo.desc = code.nameKey;
				codeVo.parent = null;
				codeVo.magic = null;
				codeVos.add(codeVo);
			}
		}
		return codeVos;
	}
}
