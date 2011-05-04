package controllers;


import java.util.List;

import models.Utilisateur;
import models.Annonce;
import play.*;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.*;
import play.mvc.Scope.RenderArgs;

public class Annonces extends Controller {
	@Before
	static void addDefaults() {
		renderArgs.put("covamiTitle",
				Play.configuration.getProperty("covami.title"));
		renderArgs.put("covamiBaseline",
				Play.configuration.getProperty("covami.baseline"));
	}

	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			Utilisateur user = Utilisateur
					.find("byEmail", Security.connected()).first();
			renderArgs.put("user", user);
			renderArgs.put("security", Security.connected());
			flash.success("Connexion réussie");
		}
	}
	public static void mesannonces(){
		if(Security.isConnected()){
			Utilisateur moi = Utilisateur.find("byEmail",Security.connected()).first();
			List <Annonce> mesannonces = Annonce.find("byMonUtilisateur_id", moi.id).fetch();
			renderArgs.put("mesannonces",mesannonces);
		}
		render();
	}
	public static void details(long id_annonce){
		if(Security.isConnected()){
			Annonce annonce = Annonce.find("byId",id_annonce).first();
			renderArgs.put("etapes", annonce.monTrajet.mesEtapes);
			renderArgs.put("annonce", annonce);
		}
		render();
	}
}