package models.vo.Menu;

import java.util.ArrayList;
import java.util.List;

import models.Menu;
import models.MenuSet;

public class MenuVo {
	public String id;
	public String nameKey;
	public int index;
	public String uri;
	public MenuVo parent;
	public List<MenuVo> MenuVos;
	public void inMenu(Menu menu){
		id=menu.id.toString();
		nameKey=menu.nameKey;
		index=menu.index;
		uri=menu.uri;
		if(menu.menuSet!=null){
			parent=inMenuSet(menu.menuSet);
			if(parent.MenuVos==null){
			parent.MenuVos=new ArrayList<MenuVo>();
			parent.MenuVos.add(this);
			}
		}
	}
	public void inMenu(MenuSet menuset){
		id=menuset.id.toString();
		nameKey=menuset.nameKey;
		if(menuset.parent!=null){
			parent=inMenuSet(menuset.parent);
			if(parent.MenuVos==null){
			parent.MenuVos=new ArrayList<MenuVo>();
			parent.MenuVos.add(this);
			}
		}
	}
	public MenuVo inMenuSet(MenuSet menuSet){
		MenuVo menuVo = new MenuVo();
		menuVo.inMenu(menuSet);
		return menuVo;
	}
}
