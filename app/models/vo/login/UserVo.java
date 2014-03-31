package models.vo.login;

import models.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVo {
	public String id;
	public String name;
	public String password;
	public void inUser(User user){
		id=user.id.toString();
		name = user.name;
		//password=user.passwordHash;
	}
	public User outUser(User user){
		user.name=name;
		if(password!=null)
		user.passwordHash=password;
		return user;
	}
}
