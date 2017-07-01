package vit.projekt;
/**
 * Zaehler macht die komplizierte Sache. Sie vereinigt die Methoden der Klasse
 * Zahl. Man ruft Zaehler.suche auf, damit die Magie passiert. Sie gibt den
 * Buchstaben zurück.
 * 
 * @author Lennart.Spiekermann
 *
 */
public class Zaehler {
	
	Fortbewegung fort;
	Messung messen;

	/**
	 * Suche sucht im Gegebenen Bereich nach einem Farbwechsel. Gibt wahr
	 * zurück, wenn ein Wechsel vorliegt.
	 * 
	 * @param blockgroesse
	 * @param farbe
	 * @return
	 */
	public char suche(long blockgroesse, boolean farbe) {
		Zahl Kette;
		long toleranz = blockgroesse / 4;
		boolean wechsel = fahr(farbe, blockgroesse + toleranz);
		Kette = new Zahl(wechsel);
		for (int i = 1; i < 4; i++) {
			Kette.add(fahr(farbe, blockgroesse));
		}
		return Kette.verwandle();
	}

	public boolean fahr(boolean farbe, double strecke) {
		double gefahren = fort.getTachoCount();
		boolean wechsel = false;
		// Schwarz ist wahr
		while ((fort.getTachoCount() - gefahren) < strecke) {
			wechsel = wechsel
					|| (farbe == messen.ersterScan() < messen.getCaliGrenze());
		}
		return wechsel;
	}

}

/**
 * Zahl verwaltet einen array aus Boolean-Werten. Es können genau vier Werte
 * eingefügt werden. Diese vier Werte können, wenn sie denn vollständig sind,
 * nach der gegebenen Semantik ausgewertet werden.
 * 
 * @author Lennart.Spiekermann
 *
 */
class Zahl {
	boolean[] bloecke = new boolean[4];
	int zaehler = 1;

	public Zahl(boolean eins) {
		this.bloecke[0] = eins;
	}

	public void add(boolean block) { // TODO Fehleranfällig! Sichern
		bloecke[zaehler] = block;
		zaehler++;
	}

	public char verwandle() {
		if (zaehler == 4) {
			return Zahl.parse(bloecke[0], bloecke[1], bloecke[2], bloecke[3]);
		} else
			return '-';
	}

	/**
	 * Parse: Verwandelt ein hell-dunkel-Quartett in einen character. Die 3 fehlt.
	 * 
	 * @param a
	 *            Der Wert des einsten Bereichsabschnitts, der umgewandelt
	 *            werden soll. Wahr entspricht dunkel.
	 * @param b
	 *            Der Wert des zweiten Bereichsabschnitts, der umgewandelt
	 *            werden soll. Wahr entspricht dunkel.
	 * @param c
	 *            Der Wert des dritten Bereichsabschnitts, der umgewandelt
	 *            werden soll. Wahr entspricht dunkel.
	 * @param d
	 *            Der Wert des vierten Bereichsabschnitts, der umgewandelt
	 *            werden soll. Wahr entspricht dunkel.
	 * @return Der Quartettwert als character. Ziffern werden als
	 *         Ziffercharacter zurückgegeben; Start und Ende als a oder z.
	 */
	public static char parse(boolean a, boolean b, boolean c, boolean d) {
		char r;
		if (a) {// a+ schwarz
			if (b) {// b+
				r = '5';
			} else {// b-
				if (c) {// c+
					if (d) {// d+
						r = '9';
					} else {
						r = 'a';
					}
				} else {// c-
					if (d) {// d+
						r = '8';
					} else {// d-
						r = '1';
					}
				}
			}
		} else {// a-
			if (b) {// b+
				if (c) {// c+
					r = '6';
				} else {// c-
					if (d) {// d+
						r = 'z';
					} else {// d-
						r = '2';
					}
				}
			} else {// b-
				if (c) {// c+
					r = '7';
				} else {// c-
					if (d) {// d+
						r = '4';
					} else {// d-
						r = '0';
					}
				}
			}
		}
		return r;
	}

}