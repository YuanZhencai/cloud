package Service;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import Service.Vo.SMaterial;


public class DateService {
    
    static String credit = "W_KT-SF" + ":" + "c0e69a9f3171720bff45ef834e6c051d";
    static String encoding = new sun.misc.BASE64Encoder().encode (credit.getBytes());
    static String URI="http://10.11.1.192:8080";

      
    
    public static Object sendHttp(String url,String Method,Object param,Object obj)
    {
        //STransactionVo result=new STransactionVo();
        Object newInstance = null ;
        try {
            newInstance = obj.getClass().newInstance();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
     try{
      URL httpurl = new URL(URI+url);
      System.out.println(httpurl);
      HttpURLConnection httpConn = (HttpURLConnection)httpurl.openConnection(); 
      httpConn.setRequestMethod(Method);
      httpConn.setRequestProperty ("Authorization", "Basic " + encoding);
      httpConn.setRequestProperty("Content-Type","application/json; charset=utf-8"); 
      httpConn.setRequestProperty("Accept","application/json, text/plain");
      httpConn.setDoInput(true);
      httpConn.setDoOutput(true);
     
      /*BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
      String line;
      while ((line = in.readLine())!= null)
      {
      System.out.println(line);
      }
      in.close();*/
     // STransactionVo request = null; // POJO with getters or public fields
      ObjectMapper mapper = new ObjectMapper(); // from org.codeahaus.jackson.map
      if(param!=null)
      mapper.writeValue(httpConn.getOutputStream(), param);
      // and read response
      //System.out.println("obj.getClass():"+obj.getClass());
      newInstance = mapper.readValue(httpConn.getInputStream(), obj.getClass());
     }catch(Exception e){
      System.out.println("没有结果！"+e);
     }
     return newInstance;
    }
    public static Object sendHttp(String url,String Method,Object param,Object obj,TypeReference type)
    {
        //STransactionVo result=new STransactionVo();
        Object newInstance = null ;
        try {
            newInstance = obj.getClass().newInstance();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
     try{
      URL httpurl = new URL(URI+url);
      System.out.println(httpurl);
      HttpURLConnection httpConn = (HttpURLConnection)httpurl.openConnection(); 
      httpConn.setRequestMethod(Method);
      httpConn.setRequestProperty ("Authorization", "Basic " + encoding);
      httpConn.setRequestProperty("Content-Type","application/json"); 
      httpConn.setRequestProperty("Accept","application/json, text/plain");
      httpConn.setDoInput(true);
      httpConn.setDoOutput(true);
     
      /*BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
      String line;
      while ((line = in.readLine())!= null)
      {
      System.out.println(line);
      }
      in.close();*/
     // STransactionVo request = null; // POJO with getters or public fields
      ObjectMapper mapper = new ObjectMapper(); // from org.codeahaus.jackson.map
      if(Method.equals("PUT"))
      mapper.writeValue(httpConn.getOutputStream(), param);
      // and read response
      //System.out.println("obj.getClass():"+obj.getClass());
      newInstance = mapper.readValue(httpConn.getInputStream(), type);
     }catch(Exception e){
      System.out.println("没有结果！"+e);
     }
     return newInstance;
    }
}
