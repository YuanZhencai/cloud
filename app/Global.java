

import controllers.routes;
import play.*;
import play.mvc.*;
import play.mvc.Http.RequestHeader;
import static play.mvc.Results.*;
public class Global extends GlobalSettings{

    @Override
    public Result onHandlerNotFound(RequestHeader arg0) {
        //System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
        return notFound(views.html.pageNotFound.render(arg0.uri())); /*controllers.Application.PageNotFound(arg0.uri());*/
    }

    @Override
    public Result onError(RequestHeader arg0, Throwable arg1) {
        // TODO Auto-generated method stub
        return super.onError(arg0, arg1);
    }

    @Override
    public Result onBadRequest(RequestHeader arg0, String arg1) {
        // TODO Auto-generated method stub
      //  System.out.println("(((((((((((((((((((((((((((((((((((((((((((((((((((((((");
        return super.onBadRequest(arg0, arg1);
    }
    
}
