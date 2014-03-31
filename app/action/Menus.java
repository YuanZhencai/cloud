package action;

import java.util.List;

import models.vo.Menu.MenuVo;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utils.MenuUtil;

	public class Menus extends Action.Simple {

	    public Result call(Http.Context ctx) throws Throwable {
	        ctx.args.put("menus", MenuUtil.getMenus());
	        return delegate.call(ctx);
	    }

	    public static List<MenuVo> current() {
	        return (List<MenuVo>)Http.Context.current().args.get("menus");
	    }
	  

	}
