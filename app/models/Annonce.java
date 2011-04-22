package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;
import play.data.validation.*;

@Entity
public class Annonce extends Model {
	@Required
	public int tarifParPersonne;

	@OneToOne
	public Utilisateur monUtilisiteur;

	@OneToOne
	public Trajet monTrajet;

	public Annonce(int tarifParPersonne, Utilisateur monUtilisiteur,
			Trajet monTrajet) {
		super();
		this.tarifParPersonne = tarifParPersonne;
		this.monUtilisiteur = monUtilisiteur;
		this.monTrajet = monTrajet;
	}

}