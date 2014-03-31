package utils;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: SendEmailUtil.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class SendEmailUtil {
	/**
	 * 发送邮件端口
	 * @param subject：主题
	 * @param to：收件人邮件地址
	 * @param from：发件人邮件地址
	 * @param content：邮件内容
	 */
	public static void sendEmail(String subject, String to, String from, String content) {
		try {
			MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
			mail.setSubject(subject);
			mail.addRecipient(to);
			mail.addFrom(from);
			mail.send("A text only message", content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
