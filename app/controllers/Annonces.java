package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.TypedQuery;

import org.codehaus.groovy.runtime.ConvertedClosure;

import com.sun.jmx.snmp.Timestamp;

import models.Trajet;
import models.Troncon;
import models.Utilisateur;
import models.Annonce;
import models.Ville;
import models.Voiture;
import play.*;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.Model;
import play.mvc.*;
import play.mvc.Scope.RenderArgs;

public class Annonces extends Controller {

	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			Utilisateur user = Utilisateur
					.find("byEmail", Security.connected()).first();
			renderArgs.put("user", user);
			renderArgs.put("security", Security.connected());
		}
	}

	@Before
	static void mesDemandes() {
		if (Security.isConnected()) {
			Utilisateur user = Utilisateur
					.find("byEmail", Security.connected()).first();
			int nbDemandes = user.mesDemandes.size();
			renderArgs.put("nbDemandes", nbDemandes);
		}
	}

	public static void annonces() {

		if (Security.isConnected()) {

			List<Annonce> annonces = Annonce.findAll();

			flash.clear();
			renderArgs.put("annonces", annonces);

		}
		render();
	}

	public static void mesannonces() {
		if (Security.isConnected()) {
			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
					.first();
			List<Annonce> mesAnnonces = Annonce.find("byMonUtilisateur", moi)
					.fetch();
			flash.clear();
			render(mesAnnonces);

		}
		render();
	}

	public static void editermonannonce(long id_annonce) {
		if (Security.isConnected()) {
			Annonce annonce = Annonce.findById(id_annonce);
			List<Ville> lesVilles = Ville.find("ORDER BY nom").fetch();
			renderArgs.put("annonce", annonce);
			renderArgs.put("etapes", annonce.monTrajet.mesEtapes);
			renderArgs.put("lesVilles", lesVilles);
		}
		render();
	}

	public static Date retournerDate(String date, String heure)
			throws ParseException {
		StringTokenizer st = new StringTokenizer(date, "/");
		String[] tabDate = new String[6];
		int i = 0;
		while (st.hasMoreTokens()) {
			tabDate[i] = st.nextToken();
			i++;
		}

		StringTokenizer stH = new StringTokenizer(heure, ":");
		while (stH.hasMoreTokens()) {
			tabDate[i] = stH.nextToken();
			i++;
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return (formatter.parse(tabDate[2] + "-" + tabDate[1] + "-"
				+ tabDate[0] + " " + tabDate[3] + ":" + tabDate[4]));

	}

	public static void sauvegardermonannonce(Annonce annonce, Long depart,
			Long arrivee, @Required String date, @Required String heure,
			String mesEscales) throws ParseException {
		// Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
		// .first();
		validation.valid(date);
		validation.valid(heure);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			editermonannonce(annonce.id);
		} else {
			annonce.monTrajet.villeDepart = Ville.findById(depart);
			annonce.monTrajet.villeArrivee = Ville.findById(arrivee);
			annonce.monTrajet.dateDepart = retournerDate(date, heure);

			List<Ville> etapes = new ArrayList<Ville>();
			StringTokenizer st = new StringTokenizer(mesEscales, " ");
			while (st.hasMoreTokens()) {
				Ville v = Ville.findById(Long.parseLong(st.nextToken()));
				etapes.add(v);
			}

			annonce.monTrajet.mesEtapes.clear();
			annonce.monTrajet.mesEtapes = etapes;
			annonce.monTrajet.save();
			annonce.save();
			flash.success("Sauvegarde réussie");
			// List<Annonce> mesAnnonces = Annonce.find("byMonUtilisateur", moi)
			// .fetch();
			mesannonces();
		}
	}

	public static void details(long id_annonce) {
		if (Security.isConnected()) {
			Annonce annonce = Annonce.find("byId", id_annonce).first();
			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
					.first();

			//System.out.println("tariftotal "+annonce.calculerTarifTotal());
			boolean dejaenvoye = false;
			for(Utilisateur a : annonce.mesDemandePassagers){
				System.out.println("test");
				System.out.println(a.nom);
			}
			System.out.println(annonce.mesDemandePassagers.size());
			if(annonce.mesDemandePassagers.contains(moi)){
				
				dejaenvoye = true;
			}
			

			// System.out.println("tariftotal "+annonce.calculerTarifTotal());

			renderArgs.put("moi", moi);
			renderArgs.put("dejaenvoye", dejaenvoye);
			renderArgs.put("annonce", annonce);
			renderArgs.put("etapes", annonce.monTrajet.mesEtapes);

		}
		render();
	}

	public static void recherche(String field) {
		if (Security.isConnected()) {
			List<Annonce> pAnnonces = new ArrayList<Annonce>();
			field = "%" + field + "%";
			List<Utilisateur> utilisateurs = Utilisateur.find(
					"nom like ? or prenom like ?", field, field).fetch();

			for (Utilisateur u : utilisateurs) {
				List<Annonce> l = Annonce.find("byMonUtilisateur_id", u.id)
						.fetch();
				for (Annonce a : l) {
					pAnnonces.add(a);
				}
			}

			renderArgs.put("annonces", pAnnonces);
			renderTemplate("Annonces/annonces.html");
		}

		render();
	}

	public static void creation(Annonce annonce) {
		if (Security.isConnected()) {
			List<Ville> lesVilles = Ville.find("ORDER BY nom").fetch();
			render(annonce, lesVilles);
		}
		render();
	}

	public static void enregistrerannonce(Annonce annonce,
			@Required Long depart, @Required Long arrivee,
			@Required String dateDepart, @Required String heureDepart,
			String mesEscales) throws ParseException {

		validation.valid(dateDepart);
		validation.valid(heureDepart);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			creation(annonce);
		} else {
			Ville villeDepart = Ville.findById(depart);
			Ville villeArrivee = Ville.findById(arrivee);

			Date madate = retournerDate(dateDepart, heureDepart);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			dateFormat.format(madate);

			List<Ville> etapes = new ArrayList<Ville>();
			StringTokenizer st = new StringTokenizer(mesEscales, " ");
			while (st.hasMoreTokens()) {
				Ville v = Ville.findById(Long.parseLong(st.nextToken()));
				etapes.add(v);
			}

			Trajet monTrajet = new Trajet(madate, villeDepart, villeArrivee,
					etapes);
			annonce.monTrajet = monTrajet;
			annonce.monUtilisateur = Utilisateur.find("byEmail",
					Security.connected()).first();

			monTrajet.save();
			annonce.save();

			flash.success("Annonce enregistrée");
			mesannonces();

		}
	}

<<<<<<< HEAD
	public static void chercheretapes(Long depart, Long arrivee,
			List<Long> mesAnciennesEtapes) {
		System.out.println(mesAnciennesEtapes);
=======
	
	public static void demandeparticipation(long id){
		Annonce annonce = Annonce.find("byId",id).first();
		Utilisateur passager = Utilisateur.find("byEmail", Security.connected()).first();
		annonce.mesDemandePassagers.add(passager);
		annonce.save();
		flash.success("Demande envoyée");
		details(id);
		
	}
	

	public static void chercheretapes(Long depart, Long arrivee) {
		// Ville villeDepart = Ville.findById(depart);
		// mesEtapes.add(villeDepart);
		//
		// Ville villeArrivee = Ville.findById(arrivee);
		// mesEtapes.add(villeArrivee);

		// System.out.println(depart + "test" + arrivee);

>>>>>>> d24c729293114436d5a2c7b94bc3166e1c6fba54
		// Création d'une liste de noeud AStar temporaire
		ArrayList<Noeud> fileAStar = new ArrayList<Noeud>();
		// Création de la liste des étapes
		List<Ville> mesEtapes = new ArrayList<Ville>();
		// Recuperation des villes
		Ville villeDepart = Ville.findById(depart);
		Ville villeArrivee = Ville.findById(arrivee);
		// Recupération des troncons
		List<Troncon> lesTroncons = Troncon.find("").fetch();

		if (depart != arrivee) {
			Noeud n = new Noeud(villeDepart,
					villeDepart.calculerDistanceVers(villeArrivee), 0, null);
			Noeud.insererNoeudHeuristique(n, fileAStar);
			while (!fileAStar.isEmpty()
					&& !fileAStar.get(0).getVille().equals(villeArrivee)) {
				Noeud nP = fileAStar.get(0);
				fileAStar.remove(0);
				lesTroncons.clear();
				lesTroncons = Troncon.find("byVilleActuelle", nP.getVille())
						.fetch();
				for (Troncon t : lesTroncons) {
					if (t.villeSuivante != nP.getVille()) {
						Noeud.insererNoeudHeuristique(
								new Noeud(t.villeSuivante, t.villeSuivante
										.calculerDistanceVers(villeArrivee)
										+ nP.getDistReelle() + t.nbKms, t.nbKms
										+ nP.getDistReelle(), nP), fileAStar);
					}
				}
				// System.out.println(nP.getVille().nom);
			}

			// Recuperation du trajet optimal avec les noeuds parents
			if (!fileAStar.isEmpty()) {
				Noeud noeud = fileAStar.get(0);
				while (noeud != null) {
					mesEtapes.add(noeud.getVille());
					noeud = noeud.getNoeudParent();
				}
				Collections.reverse(mesEtapes);
			}
		} else {
			mesEtapes.add(villeDepart);
			mesEtapes.add(villeArrivee);
		}
		render(mesEtapes, depart, arrivee, mesAnciennesEtapes);
	}

	public static List<Ville> chercherchemin(Annonce annonce) {
		List<Ville> trajet = new ArrayList<Ville>();
		if (!annonce.monTrajet.villeDepart
				.equals(annonce.monTrajet.villeArrivee)) {
			// todo

		}
		return trajet;
	}
}