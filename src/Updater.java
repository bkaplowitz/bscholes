/**
 * SRP009 - Senior Project
 * Phillips Exeter Academy
 * Spring 2013
 * Instructor: J. Wolfson
 * Brandon Kaplowitz '13 and Sid Reddy '13
 * 
 * Financial Market Simulator
 * 
 * @author Siddharth G. Reddy
 */
public class Updater extends Thread { //used for multi-threading
	
	Position pos;
	
	public Updater(Position param) {
		pos = param;
	}
	
	public void run() {
		//System.out.println("updating stock price...");
		pos.updateStockPrice();
		//System.out.println("updating option price...");
		pos.getOption().updateOptionPrice();
		//System.out.println("updating bond price...");
		pos.getBond().updateBondPrice();
	}

}
