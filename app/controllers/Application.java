package controllers;

import play.mvc.*;
import security.NoUserDeadboltHandler;
import views.html.*;


import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class Application extends Controller {
    public static Result index() {
        return ok(example.render("ODC Engineer"));
    }
    public static Result PageNotFound(String url){
        return ok(pageNotFound.apply(url));
    }
}
