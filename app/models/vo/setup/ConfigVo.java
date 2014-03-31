/** * ConfigVo.java 
* Created on 2013-5-8 下午3:38:38 
*/

package models.vo.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Config;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: ConfigVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigVo {
	public String id;
	public String cat;
	public String code;
	public String desc;
	public String value;
	public String remarks;

	public static List<ConfigVo> getConfig(Map<String,Object> searchMap) {
		ArrayList<ConfigVo> configVos = new ArrayList<ConfigVo>();
		ExpressionList<Config> el = Ebean.find(Config.class).where().eq("deleted", false);
		if (searchMap.get("code") != null && !"".equals(String.valueOf(searchMap.get("code")).trim())) {
		el.like("configKey", "%" + String.valueOf(searchMap.get("code")).trim() + "%");
		}
		if (searchMap.get("value") != null && !"".equals(String.valueOf(searchMap.get("value")).trim())) {
			el.like("configValue", "%" + String.valueOf(searchMap.get("value")).trim() + "%");
		}
		List<Config> configs = el.order("updatedAt descending").findList();
		if (configs != null && !configs.isEmpty()) {
			for (Config config : configs) {
				ConfigVo configVo = new ConfigVo();
				configVo.id = String.valueOf(config.id);
				configVo.cat = null;
				configVo.code = config.configKey;
				configVo.desc = null;
				configVo.value = config.configValue;
				configVo.remarks = config.remarks;
				configVos.add(configVo);
			}
		}
		
		
		
		return configVos;
	}

	public static void setConfig(ConfigVo configVo) {
		Config config = new Config();
		boolean flag = true;
		if (configVo.id != null && !"".equals(configVo.id.trim())) {
			config = Ebean.find(Config.class).where().eq("deleted", false).eq("id", configVo.id).findUnique();
			flag = false;
		}
		config.business = null;
		config.company = null;
		config.warehouse = null;
		config.configLevel = null;
		config.configKey = configVo.code;
		config.configValue = configVo.value;
		config.remarks = configVo.remarks;
		config.ext = null;
		// config.version = 1;
		config.schemaCode = null;
		if (flag) {
			CrudUtil.save(config);
		} else {
			CrudUtil.update(config);
		}
	}
}
