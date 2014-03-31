import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

import play.mvc.Content;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test 
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }
    
    @Test
    public void renderTemplate() {
        Content html = views.html.index.render("Your new application is ready.");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Your new application is ready.");
    }
    @Test
    public void readTest(){
    BufferedReader br=null;
    try {
    	br= new BufferedReader(new FileReader("conf/routes"));
    	while (br.readLine()!=null) {
    		String readLine = br.readLine();
    		int indexOf = readLine.indexOf("/");
    		if(indexOf>0){
    		String substring = readLine.substring(indexOf, readLine.indexOf("\t", indexOf));
    		System.out.println(substring);
    		}
		}
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
    }
}
