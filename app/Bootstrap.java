import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class Bootstrap extends Job {

	@SuppressWarnings("deprecation")
	public void doJob() {
		if (Ville.count() == 0) {
			initialiserVilles();
		}
		if (Troncon.count() == 0) {
			initialiserTroncons();
		}
		if (Utilisateur.count() == 0) {
			Fixtures.load("initial-data.yml");
		}

	}

	public void initialiserVilles() {
		Pays france = Pays.find("byNom", "France").first();
		try {
			BufferedReader fichier = new BufferedReader(new FileReader(
					"/home/philippe/covami_workspace/covami/public/csv/villes.csv"));
			String chaine;
			while ((chaine = fichier.readLine()) != null) {
				// Sépare à l'aide du ; la ligne dans un tableau de chaines
				String[] champs = chaine.split(";");
				// Ajout de la ville en cours
				(new Ville(champs[0], champs[1], champs[2],
						Float.parseFloat(champs[3]),
						Float.parseFloat(champs[4]), france)).save();
			}
			fichier.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialiserTroncons() {
		// Sert à savoir si le champs parser est le nom (le premier champs d'une
		// ligne)
		String autoroute = "";
		try {
			BufferedReader fichier = new BufferedReader(new FileReader(
					"/home/philippe/covami_workspace/covami/public/csv/autoroutes.csv"));
			String chaine;
			while ((chaine = fichier.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(chaine, ";");
				while (st.hasMoreTokens()) {
					if (autoroute == "") {
						// Si c'est le premier champs, c'est le nom de
						// l'autoroute
						autoroute = st.nextToken();
					} else {
						// Sinon c'est la ville à ajouter à un nouveau tronçon
						Ville v = Ville.find("byCodeInsee", st.nextToken())
								.first();
						(new Troncon(autoroute, v, 0, 0)).save();
					}
				}
				// Reaffectation la var à null pour la prochaine ligne
				autoroute = "";
			}
			fichier.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}