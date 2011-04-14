package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
	
	@Before
	static void addDefaults() {
	    renderArgs.put("covamiTitle", Play.configuration.getProperty("covami.title"));
	    renderArgs.put("covamiBaseline", Play.configuration.getProperty("covami.baseline"));
	}
	
	@Before
    static void setConnectedUser(){
         if(Security.isConnected()){
             Utilisateur user = Utilisateur.find("byEmail",Security.connected()).first();
             renderArgs.put("user", user);
             renderArgs.put("security",Security.connected());
             flash.success("Connexion réussie");
         }
     }

    public static void index() {
        render();
    }
    
    public static void moncompte() {
    	if(Security.isConnected()) {
    		Utilisateur user = Utilisateur.find("byEmail", Security.connected()).first();
    		renderArgs.put("user", user);
    		renderArgs.put("security",Security.connected());
    	}
    	render();
    }
    
    public static void editermoncompte(Utilisateur usr) {
    	if(Security.isConnected()) {
    		Utilisateur user = Utilisateur.find("byEmail", Security.connected()).first();
    		//renderArgs.put("user", user);
    		renderArgs.put("security",Security.connected());
    		render(user);
    	}
    	render();
    }
    
    public static void sauvegardermoncompte(Utilisateur usr) {
    	//user.save();
    	Utilisateur user = Utilisateur.find("byId", usr.id).first();
    	System.out.println(user.email);
        user.edit("usr", params.all());
        validation.valid(user);
        if(validation.hasErrors()) {
        	editermoncompte(user);
        } else{
           user.save(); // explicit save here
           flash.success("Sauvegarde réussie");
           moncompte();
        }
    	flash.success("Sauvegarde réussie");
    	moncompte();
    }
    
    public static void inscription(Utilisateur user) {
    	if(Security.isConnected()) {
    		renderArgs.put("security",Security.connected());
    		index();
    	}
    	render(user);
    }
    
    public static void enregistrerinscription(Utilisateur user) {
    	validation.valid(user);
        if(validation.hasErrors()) {
        	/*for(play.data.validation.Error error : validation.errors()) {
                flash.error(error.message());
            }*/
        	flash.error("Erreur de validation");
        	//flash.error(validation.toString());
        	inscription(user);
        } else{
           user.save(); // explicit save here
           flash.success("Inscription réussie - Merci de vous connecter");
           try {
	   			Secure.login();
	   		} catch (Throwable e) {
	   			e.printStackTrace();
	   		}
           moncompte();
        }
    	renderArgs.put("user", user);
    	renderArgs.put("security",Security.connected());
    	index();
    }

}