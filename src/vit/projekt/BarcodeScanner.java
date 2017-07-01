package vit.projekt;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.utility.Delay;

/*
 * 0,0527777777778 cm/°
 */

public class BarcodeScanner
{
	float caliGrenze; // Pauschal: 0 schwarz, 1 weiß
	float samples[] = new float[1];
	//Object[] rueckgabe = new Object[2]; // Evtl nur fürs Debugging gebraucht
	EV3ColorSensor light;
	long toleranzBlock;
	//int degreeBlock;
	long block;
	boolean zeit;
	boolean debug;
	int zeile=0;
	boolean start;
	boolean ziel=false;
	String dunkele=""; 
	String strichcode="";  
	int anzahlBloeckeRead;
	String dunkel;
	Fortbewegung fort;
	Anzeige anzeigen;
	
	//640 32
	//499 25
	//709 35
	
	// class Rueckgabe
	// {
	// float aktWert; //TODO: SChmittigenauigkeit: privat?
	// long timeBlock; //Wie lange braucht Robi für einen Block -
	// durchgetTachocount ersetzt

	/*
	 * nicht mehr gebraucht, da die Blöcke in der Methode erkenneStart gemesssen
	 * werden. public Rueckgabe(float aktWert, long timeBlock) {
	 * this.aktWert=aktWert; this.timeBlock=timeBlock; }
	 */
	// }
	
	BarcodeScanner(boolean zeit, boolean debug)
	{
		//Motor.A.setSpeed(50);
		//Motor.D.setSpeed(50);
		light = new EV3ColorSensor(SensorPort.S4);
		light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt		
		fort= new Fortbewegung(500,50);
		anzeigen = new Anzeige();
		
		
		this.zeit=zeit;
		this.debug=debug;
		this.ziel=ziel;
		this.dunkel=dunkel;
		this.start=true;
		
		
	}
	
	public void warte(int sekunden)
	{
		anzeigen.clearLCD();
		anzeigen.drawString("Starte in "+sekunden+" Sekunden",3);
		Delay.msDelay(sekunden*1000); //Damit der Roboter nicht vom (Be)diener beeinflusst wird
		anzeigen.clearLCD();		
	}
	
	public void pruefeBeginnRichtigSteht(boolean dunkel)
	{
		float a = this.ersterScan(); 
		
		if(dunkel && (a > caliGrenze))
		{
			anzeigen.drawString("Bitte auf Schwarz stellen");
		}
		else if ((!dunkel) && (a < (caliGrenze-(caliGrenze/2))))
		{
			anzeigen.drawString("Bitte auf Weiß stellen");
		}
		if((!dunkel && (a < (caliGrenze-(caliGrenze/2)))) || (dunkel && (a > caliGrenze)))
		{	
			anzeigen.drawString("und ENTER drücken");
			while (Button.ENTER.isUp());
			this.warte(3); //FIXME starte in 3 Sekunden
			this.pruefeBeginnRichtigSteht(dunkel);
		}
		if(debug)
		{
			anzeigen.drawString("pruefeBeginnRichtigSteht bestanden");
		}
		//LENNI: Einfach schwarz erkennen; fährt bis weiß und los gehts.		
	}

	
	public static void main(String[] args)
	{	
		boolean debug = true;
		boolean zeit = false; //Zeit oder Grad zur Messung verwenden?
		BarcodeScanner myLineReader = new BarcodeScanner(zeit, debug); 
		myLineReader.calibrate();
		
		myLineReader.dunkel = myLineReader.erkenneStart("1010");
		//Fortbewegung fort = new Fortbewegung(500,50);
		while(myLineReader.ziel!=true && Button.ESCAPE.isUp())//(i < 10 && Button.ESCAPE.isUp()) 
		{	
			//myLineReader.dunkel=myLineReader.berechneBlockgroesse(myLineReader.dunkel);
			//anzeigen.drawString(""+this.erkenneFarbe(dunkel));
      //dunkel=gegenTeilString(dunkel);
//			this.dunkel=this.gegenTeilString(dunkel);
			if(myLineReader.anzahlBloeckeRead!=0)
			{
				//myLineReader.drawString("Blocke "+myLineReader.anzahlBloeckeRead);
				myLineReader.convertiereStrichcode(myLineReader.dunkel, myLineReader.anzahlBloeckeRead);
			}
			else
			{
				myLineReader.dunkel=myLineReader.berechneBlockgroesse(myLineReader.dunkel); //TODO Variante 1
				//myLineReader.drawString(myLineReader.dunkel);
			}
		}		
		//myLineReader.caliGrenze = 0.4f; TODO Sei nicht so Faul du  Penner
		//LCD.clear();
		//myLineReader.antiRecursion();
		//anzeigen.drawString(""+this.suche(block, startString.substring(3).equals("1"))); //TODO Variante 2
		
		
		//34
		//35
		//31
		//  Steueung 15
		
		
		
		//fort.stoppe(); TODO rausnehmen, wenn nicht mehr benötigt.
		//myLineReader.drawString("Fertig");
		
		while (Button.ENTER.isUp()); // TODO KILLME
		//LCD.clear();
	}
	/*public void berechneStartBlockgroesse()
	{
		int anzahlBloecke=this.berechneBlockgroesse(false);
		if(anzahlBloecke==1)
		{
			//berechneBlockgroesse(true); //TODO Nach Test einkommentieren
			anzeigen.drawString("Weiss nur Start");
		}
		else
		{
			anzeigen.drawString(+(anzahlBloecke-1)+" Bloecke weiss");
			if((anzahlBloecke-1) > 3)
			{
				//0
			}
		}	
		this.start=false;
	}*/
	
		
		/* TODO Müll entfernen wenn sicher ist, dass es müll ist
		 
		 if(debug)
		{
// Der 4. Block des Starts (weiß) beginnt hoffentlich hier
			// Rueckgabe ergebnis4 = this.erkenneFarbe(false);
			anzeigen.drawString("AktWert: " + this.erkenneFarbe(false));
			// LCD.drawString("TBlock: "+ergebnis4.timeBlock,0,2);
			// while (Button.ENTER.isUp());
		}
		else
		{
// Der 4. Block des Starts (weiß) beginnt hoffentlich hier
			this.erkenneFarbe(false);
		}
		/*
		 * TODO Ende Methode entwickeln
		 */
	
	
	/**
	 * TODO Kann hier noch optimiert werden?
	 */
	public float scanne(float letzterWert)
	{
		return (this.ersterScan()+letzterWert)/2;
	}
	public float ersterScan()
	{
		light.fetchSample(samples, 0);
		while (samples[0] == 0)
		{
			light.fetchSample(samples, 0);
		}
		return samples[0];
	}
	/*
	 * light.fetchSample(samples, 0);
		float rueckgabeWert=0;
		//for(float sample: samples)
		for(int i = 0; i < samples.length; i++)
		{	
			while (samples[i] == 0)
			{
				light.fetchSample(samples, i);
			}
			rueckgabeWert += samples[i];
		}	
		return rueckgabeWert / samples.length;
		*/

	

	
	
	/**
	 * Calibriert "Hell" und "Dunkel" TODO Kontrollieren
	 */
	public void calibrate()
	{
		float samples[] = new float[light.sampleSize()]; // wird in dieser
														// Methode mehrfach
														// verwendet
		float caliHell = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
							// sein kann, 2 als initialsierung genommen
		float caliDunkel = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
								// sein kann, 2 als initialsierung genommen
		// Ab hier wird losgemessen
		
		while (Button.ENTER.isUp()) 
		{
			anzeigen.clearLCD();
			anzeigen.drawString("Helle Fleche stellen");
			anzeigen.drawString("druecken sie ENTER");
			while (Button.ENTER.isUp());
			Delay.msDelay(1000);
			caliHell = this.ersterScan();
			/*
			 * While schleife wird durch die Methode scannen ersetzt
			 */
			// while (sample[0]==0 || caliHell==2) //TODO: Nice to have: abfangen
			// wenn Hell abgefragt aber auf dunkel gestellt
			// {
			// light.fetchSample(sample, 0);
			// caliHell = sample[0];
			// //Delay.msDelay(5000);
			// }
			anzeigen.drawString("HelleFläche: " + caliHell);
			// Delay.msDelay(5000); //TODO KILLME
			while (Button.ENTER.isDown()); // verhindert das Hell und Dunkel gleichzeitig gesetzt werden
			anzeigen.clearLCD();
			// TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst
			// ENTER
			// Delay.msDelay(5000); //TODO KILLME
			anzeigen.drawString("Dunkle Fleche stellen");
			anzeigen.drawString("druecken sie ENTER");
			while (Button.ENTER.isUp());
			Delay.msDelay(1000);
			caliDunkel = this.ersterScan();
			/*
			 * While schleife wird durch die Methode scannen ersetzt
			 */
			// while (sample[0]==0 || caliDunkel==2) //TODO: Nice to have: abfangen
			// wenn Dunkel abgefragt aber auf hell gestellt
			// {
			// light.fetchSample(sample, 0);
			// caliDunkel = sample[0];
			// //Delay.msDelay(5000);
			// }
			//LCD.clear();
			anzeigen.drawString("Hell: " + caliHell);
			anzeigen.drawString("Dunkel: " + caliDunkel);
			caliGrenze = caliDunkel + ((caliHell - caliDunkel) / 2); // Achtung,
																		// beachtet
																		// nicht
																		// Punkt vor
																		// Strich
																		// Rechnung!
			anzeigen.drawString("Grenze: " + caliGrenze);
			while (Button.ENTER.isDown()); // verhindert das die Kalibrierung versehentlich zu früh beendet wird.
			anzeigen.drawString("");
			anzeigen.drawString("Bitte an den Start stellen");
			anzeigen.drawString("druecken sie ENTER");
			anzeigen.drawString("oder ESC");
			while (Button.ENTER.isUp() && Button.ESCAPE.isUp());
		}
			this.warte(3);
	
	}
	
// String Methoden beginnen hier
	
	//public void antiRecursion()
	//{
//		String dunkel = this.erkenneStart("1010");
//		int i = 0;
//		while(i < 10 && Button.ESCAPE.isUp()) //(this.ziel!=true && Button.ESCAPE.isUp())
//		{	
//			dunkel=this.berechneBlockgroesse(dunkel);
////		anzeigen.drawString(""+this.erkenneFarbe(dunkel));
////		dunkel=gegenTeilString(dunkel);
////			this.dunkel=this.gegenTeilString(dunkel);
////			/*if(this.anzahlBloeckeRead!=0)
////			{
////				anzeigen.drawString("Blocke "+this.anzahlBloeckeRead);
////				this.anzahlBloeckeRead=this.convertiereStrichcode(this.dunkel, this.anzahlBloeckeRead);
////			}
////			else
////			{
//				//test=this.berechneBlockgroesse(test); //TODO Variante 1
////				anzeigen.drawString(""+test);
////			}*/
//			i++;
//		}
	//}
	
	/*
	 * ersetzt durch erkenneFarbe
	 * 
	 * 
	 * public Rueckgabe erkenneSchwarz() { //LCD.clear(); long timeBlock=
	 * -System.nanoTime(); float aktWert = this.scanne(); while(aktWert <
	 * caliGrenze && Button.ENTER.isUp()) //schwarz { aktWert = this.scanne();
	 * //fort.fahre; LCD.drawString("erkenneSchwarz",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep();
	 * //Sound.beep(); timeBlock += System.nanoTime(); //Object[] rueckgabe = {
	 * aktWert, timeBlock}; //return rueckgabe; return new Rueckgabe(aktWert,
	 * timeBlock); } /**
	 * 
	 * 
	 * //public float erkenneWeiß() old public Rueckgabe erkenneWeiß() {
	 * //LCD.clear(); long timeBlock= -System.nanoTime(); float aktWert =
	 * this.scanne(); while(aktWert > caliGrenze && Button.ENTER.isUp()) //weiß
	 * { aktWert = this.scanne(); //fort.fahre;
	 * LCD.drawString("erkenneWeiss",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep(); timeBlock +=
	 * System.nanoTime(); //Object[] rueckgabe = { aktWert, timeBlock}; //return
	 * rueckgabe; return new Rueckgabe(aktWert, timeBlock); }
	 */
	/**
	 * Soll den Start erkennen und die Abstände eines Blockes calibrieren.
	 * Probleme hierbei könnte die Startlinie machen
	 */
	public String erkenneStart(String startString)//startString = z.b. 1010
	{
		if (startString.length()!=4)
		{
			anzeigen.drawString("Es werden genau 4 Werte benötigt",3);
			anzeigen.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		if(startString.substring(0, 1).equals("1"))
		{
			this.pruefeBeginnRichtigSteht(false);
		}
		else if(startString.substring(0, 1).equals("0"))
		{
			this.pruefeBeginnRichtigSteht(true);
		}
		else
		{
			anzeigen.drawString("Nur 0 oder 1",3);
			anzeigen.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		boolean restart=true;
		while(restart && Button.ESCAPE.isUp())
		{	
			restart=false;
			if(startString.substring(0, 1).equals("1"))
			{	
				fort.fahre();
				this.erkenneFarbe("0");
			}
			else if(startString.substring(0, 1).equals("0"))
			{
				fort.fahre();
				this.erkenneFarbe("1");
			}	
			else
			{
				anzeigen.drawString("Nur 0 oder 1",3);
				anzeigen.drawString("ESC zum beenden",4);
				System.exit(1);
			}
			if(this.zeit)
			{
				block = -System.currentTimeMillis();
			}
			else
			{	
				block = -fort.getTachoCount();
			}	
			for(int i = 0; i < 3; i++)
			{	
				long strecke = this.erkenneFarbe(startString.substring(i, i+1));
				if(debug)
				{
					//anzeigen.drawString(startString.substring(i, i+1));
					anzeigen.drawString("Strecke: " +strecke);
				}
				if(strecke==0)
				{
					restart=true;
				}
			/*if(i==3)//Nach dem 3. Durchgang (Die 0 zählt mit!) Zeitmessung stoppen
			{
				/*if(debug)
				{
					anzeigen.drawString("Block:" + block);
					if(this.zeit)
					{
						anzeigen.drawString("GesamtStr:" + (System.currentTimeMillis() - Streckenanfang));
					}
					else
					{	
						anzeigen.drawString("GesamtStr:" + (this.getTachoCount() - Streckenanfang));
					}
				}*/
				// TODO ist Integer/long gut? denk dran Nachkommastellen werden abgeschnitten
				
				
				// Toleranz von einem 1/4.
				/*if(debug)
				{
					anzeigen.drawString("Toleranz:" + toleranzBlock); TODO reinnehmen
					LCD.drawString("TBlock: "+ergebnis3.timeBlock,0,2);
					while (Button.ENTER.isUp());
				}
			}*/
			}
			if(restart)
			{
				fort.fahreZurueckStart();
			}
		}	
		if(this.zeit)
		{
			this.block = (this.block + System.currentTimeMillis())/3;
		}
		else
		{	
			this.block = (this.block + fort.getTachoCount()) / 3; 
		}
		toleranzBlock = (block / 4);
		anzeigen.drawString(""+block);
		return startString.substring(3);	
	}
		/*		
		if(debug)
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier			
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("1"));
			// aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider
			// LCD.drawString("TBlock: "+ergebnis1.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			// Rueckgabe ergebnis2 = this.erkenneFarbe(false);
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("0"));
			// LCD.drawString("TBlock: "+ergebnis2.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			// Rueckgabe ergebnis3 = this.erkenneFarbe(true);
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("1"));
		}
		else
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier	
			this.erkenneFarbe("1");
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			//anzeigen.drawString(""); //FIXME Ohne das hier keine erkenneWeiß auf dem Display oO
			this.erkenneFarbe("0");			
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			this.erkenneFarbe("1");
		}*/		
		
	
	public long erkenneFarbe(String dunkel)
	{
		long aktBlock;
		if(this.zeit)
		{
			aktBlock = -System.currentTimeMillis();
		}
		else
		{	
			aktBlock = -fort.getTachoCount();
		}
		// //LCD.clear();
		// long timeBlock= -System.nanoTime();
		float aktWert = this.ersterScan();
		if (dunkel.equals("1"))
		{
			while (aktWert < (caliGrenze) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
				// fort.fahre;
			}
			//anzeigen.drawString("erkenneSchwarz");
		} 
		else
		{
			//Grenze für weiß erhöht, da sonst zu schnell schwarz erkannt wird
			//while (aktWert > (caliGrenze-(caliGrenze/2)) && Button.ENTER.isUp()) // TODO: An die Blockgröße anpassen - Wird bei 1cm nicht benötigt!
			while (aktWert > (caliGrenze) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
				// fort.fahre;
			}
			//anzeigen.drawString("erkenneWeiss");
			//Sound.beep();
		}
		/*
		 * Crazy Schleife welche aus 2 Schleifen eine Schleife machen würde,
		 * aber relativ kompliziert ist. float wert1; float wert2;
		 * if(dunkel==true) { wert1 = aktWert; wert2 = caliGrenze;
		 * LCD.drawString("erkenneSchwarz",0,0); } else { wert1 = caliGrenze;
		 * wert2 = aktWert; LCD.drawString("erkenneWeiß",0,0); } while(wert1 <
		 * wert2) //aktWert < caliGrenze - schwarz //caliGrenze < aktWert - weiß
		 * { aktWert = this.scanne(); LCD.drawString("AktWert: "+aktWert,0,1); }
		 */
		if(debug)
		{
			//anzeigen.drawString("AktWert: " + aktWert);
		}
		// timeBlock += System.nanoTime(); // Wird in der Methode erkenne start
		// gemacht
		// return new Rueckgabe(aktWert, timeBlock);
		if(this.zeit)
		{
			return aktBlock + System.currentTimeMillis();
		}
		else
		{	
			return aktBlock + fort.getTachoCount();
		}
		//return aktDegreeBlock + this.getTachoCount();
	}
	
	public int dunkeleAuswerten(String volldunkele)
	{	
	 final String[] Muster = {
	            "0000", "1000", "0100", "0010", "0001", //Zahlen 0-4
	            "1100", "0110", "0011", //Zahlen 5-7 
	            "1001", "1011", //Zahlen 8-9
	            "0101" // Ziel  (10)
	        };

	        for (int i=0; i<Muster.length; i++)
	        {	
	            if (volldunkele.equals(Muster))
	            {	
	                return i;
	            }
	        }
	        return 110; //Fehler - TODO fahre zurück zum Start...
	}   
	/*
	 * Wenn voll, dann hier leeren
	 */
	public void dunkeleUebertragen(String volldunkele)
	{
		int strichcodeZahl = dunkeleAuswerten(volldunkele);
		if( strichcodeZahl < 10 )
		{
			strichcode += " "+strichcodeZahl; //hinten?
			anzeigen.drawString(strichcode);
		}
		else if (strichcodeZahl == 10)
		{
			Sound.beep();
			anzeigen.drawString("Die Zahl lautet", 3);
			anzeigen.drawString(strichcode, 4);
			this.ziel=true;
			Sound.beep();
		}
		else
		{
			//Fehler - TODO fahre zurück zum Start...
			anzeigen.drawString("Verzeihe mir Meister"); 
			Sound.beep();
			fort.stoppe();
			while (Button.ESCAPE.isUp());
			System.exit(1);
		}
		anzeigen.drawString(strichcode);
	}
	public void dunkeleLeer(String dunkel, int anzahl)
	{
		if (anzahl>3) // Mindestens 4 boolean Werte welche nur hell (dunkel=false) sein können
		{
			strichcode+="0";
			if((anzahl -= 4) != 0)
			{
				this.convertiereStrichcode(dunkel, anzahl);
			}				
		}
		else
		{	
			for(int i=0; i < anzahl;i++)
			{
				dunkele+=dunkel;
			}
		}
	}
	
	public void convertiereStrichcode(String dunkel, int anzahl)
	{		
		//anzeigen.drawString("F:"+dunkel+" A:"+anzahl);
		if(dunkele.isEmpty()) // Wenn strichcode leer ist
		{
			dunkeleLeer(dunkel, anzahl);
			this.anzahlBloeckeRead=anzahl;
		}
		else
		{
			while((anzahl > 0) && (dunkele.length() <= 4))
			{		
				dunkele+=dunkel;
				anzahl--;
			}	
			if(dunkele.length() == 4)
			{
				anzeigen.drawString(dunkele);
				dunkeleUebertragen(dunkele);
			}
			if(anzahl > 0)
			{
				this.dunkel=dunkel;
				this.anzahlBloeckeRead=anzahl;
			}
			else
			{
				this.anzahlBloeckeRead=0;
			}
		}
	}
	
	public String gegenTeilString(String dunkel)
	{
		if(dunkel.equals("1"))
		{
			return "0";
		}
		else
		{
			return "1";
		}
	}
			
	
	public String berechneBlockgroesse(String dunkel)
	{	
		/*Fahr zu Anfang weiß
Strecke entspricht x
finde n, für das gilt:
nBlockgröße < x < nBlockgröße + Toleranz
Sag wie viele Blöcke dieselbe Farbe hatten
Miss den nächsten Block (andere Farbe) genau so
Finde Ende*/
		long aktStrecke = this.erkenneFarbe(dunkel);
		
		int anzahlBloecke = (int) (aktStrecke/this.block);
		//float rest = aktStrecke % this.block;
		if(aktStrecke % this.block>=toleranzBlock)
		{
			if(this.debug)
			{
				//anzeigen.drawString(aktStrecke % this.block+"Ueber="+anzahlBloecke);
			}
			anzahlBloecke++;			
		}
		//else if(aktStrecke % this.block<=(anzahlBloecke*toleranzBlock))
		else
		{
			if(this.debug)
			{
				//anzeigen.drawString(aktStrecke % this.block+"Inner="+anzahlBloecke);
			}
		}	
		//anzeigen.drawString(""+block);
		if(this.start)
		{
			if(anzahlBloecke!=1)
			{
				if(this.debug)
				{
					anzeigen.drawString((anzahlBloecke-1)+" mehr als Start");
				}	
				anzahlBloecke--;
				//"Overhead" weitergeben
				convertiereStrichcode(dunkel, (anzahlBloecke-1));
			}	
			else
			{
				if(this.debug)
				{
					anzeigen.drawString("Weiss nur Start");
				}				
			}
			this.start=false;
			//berechneBlockgroesse(gegenTeilString(dunkel));//Stackoverflow-Vermeidung
		}
		else
		{
			convertiereStrichcode(dunkel, anzahlBloecke);
			//berechneBlockgroesse(gegenTeilString(dunkel)); Stackoverflow-Vermeidung
		}
		return gegenTeilString(dunkel);
	}		
	/**
	 * Zaehler macht die komplizierte Sache. Sie vereinigt die Methoden der Klasse
	 * Zahl. Man ruft Zaehler.suche auf, damit die Magie passiert. Sie gibt den
	 * Buchstaben zurück.
	 * 
	 * @author Lennart.Spiekermann
	 *
	 */
	//class Zaehler {

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
						|| (farbe == this.ersterScan() < this.caliGrenze);
			}
			return wechsel^farbe;
		}

	//}

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
				return this.parse(bloecke[0], bloecke[1], bloecke[2], bloecke[3]);
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
		public char parse(boolean a, boolean b, boolean c, boolean d) {
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
	

}