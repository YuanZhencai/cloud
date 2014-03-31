package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Menu;
import models.MenuSet;
import models.vo.Menu.MenuVo;
import play.mvc.*;
import security.NoUserDeadboltHandler;
import views.html.*;


import be.objectify.deadbolt.java.actions.SubjectPresent;
public class MenuUtil {
    public static List<MenuVo> getMenus() {
       String sessionMenus = SessionSearchUtil.getSessionMenus();
       // System.out.println("sessionMenus:"+sessionMenus);
       if(sessionMenus!=null){
    	   char[] charArray = SessionSearchUtil.hexString2binaryString(sessionMenus).toCharArray();
    	  // System.out.println("length:"+charArray.length);
    	   List<MenuVo> MenuVos=new ArrayList<MenuVo>();
    	  for (int i = 0; i < charArray.length; i++) {
			if(charArray[i]=='1'){
				Menu menu = Menu.find().fetch("menuSet").where().eq("deleted", false).eq("index", i).findUnique();
				if(menu!=null){
				MenuVo menuVo = new MenuVo();
				menuVo.inMenu(menu);
				MenuVos=findMenuList(MenuVos,menuVo);
				}
			}
    	  }
    	  // System.out.println();
    	  return MenuVos;
       }
	return null;
    }
    public static List<MenuVo> findMenuList(List<MenuVo> MenuVos,MenuVo menuVo){
    	MenuVo superFather = getSuperFather(menuVo);
    	boolean ishave=false;
    	for (MenuVo menuVo2 : MenuVos) {
			if(menuVo2.id.equals(superFather.id)&&superFather.MenuVos!=null){
				menuVo2.MenuVos=setChild(menuVo2.MenuVos,superFather.MenuVos.get(0));
				ishave=true;
			}
		}
    	if(!ishave)
    		MenuVos.add(superFather);
    	
    	return MenuVos;
    }
    public static List<MenuVo> setChild(List<MenuVo> MneuVos,MenuVo menuVo){
    	boolean isSave=false;
    	for (MenuVo menuVotemp : MneuVos) {
    		if(menuVotemp.id.equals(menuVo.id)&&menuVo.MenuVos!=null){
    			MneuVos=setChild(menuVotemp.MenuVos, menuVo.MenuVos.get(0));
    			isSave=true;
    		}
		}
    	if(!isSave)
    		MneuVos.add(menuVo);
    	return MneuVos;
    }
    public static MenuVo getSuperFather(MenuVo menuVo){
    	if(menuVo.parent==null)
    		return menuVo;
    	else{
    		// System.out.println(menuVo.parent.nameKey);
    		return getSuperFather(menuVo.parent);
    	}
    }
}
