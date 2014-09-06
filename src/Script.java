import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * Math 590 - Differential Equations
 * Phillips Exeter Academy
 * Fall 2012
 * Instructor: M. Stahr
 * Brandon Kaplowitz '13 and Sid Reddy '13
 * 
 * Implementation of Black-Scholes derivatives pricing and dynamic hedging
 * 
 * @author Siddharth G. Reddy
 *
 */
public class Script {
	
	public static String rootDir = "/Users/siddharthreddy/Documents/diffq";
	private static int timeStep = 1; //time delay between portfolio updates is at least 1 second
		
	/**
	 * 'Main' method
	 */
	public static void init(TimeSeries stockPrice, TimeSeries predOptPrice, TimeSeries mrktOptPrice, TimeSeries hedgeOptEquity, TimeSeries hedgeStockEquity) {
		Portfolio port = new Portfolio();
		int t = 0, delay;
		FileOps.writeToFile(rootDir + "/equity_output.txt", "T\tDELTA_EQUITY\tDDELTA_EQUITY\t" + port.getHeader() + "\n" + t + "\t" + port.getDeltaEquity() + "\t0\t" + port.toString() + "\t" + "\n");
		FileOps.writeToFile(rootDir + "/hedge_output.txt", "T\tINITEQUITY\tCURRENTEQUITY\t" + port.getHedgeHeader() + "\n" + t + "\t" + port.getInitHedgeEquity() + "\t" + port.getHedgeEquity() + "\t" + port.hedgeToString() + "\n");
		FileOps.writeToFile(rootDir + "/corr_output.txt", "T\t" + port.getCorrHeader() + "\n");
		t++;
		long time;
		float ddelta = 0, delta, prev = 0;
		while (true) { //infinite loop
			time = System.currentTimeMillis();
			//System.out.println("updating prices...");
			port.updatePrices();
			//System.out.println("updating positions (using Black-Scholes equation)...");
			port.stepBS();
			//System.out.println("hedging for riskless portfolio...");
			port.hedge();
			
			//add data points to dynamic graph
			stockPrice.add(new Millisecond(), port.getStockPos().get(0).getStockPrice());
			predOptPrice.add(new Millisecond(), port.getStockPos().get(0).getPredOptPrice());
			mrktOptPrice.add(new Millisecond(), port.getStockPos().get(0).getOption().getLastPrice());
			hedgeOptEquity.add(new Millisecond(), port.getStockPos().get(0).getOption().getLastPrice() * port.getStockPos().get(0).getOptionPos());
			hedgeStockEquity.add(new Millisecond(), port.getStockPos().get(0).getNumHedgeShares() * port.getStockPos().get(0).getStockPrice());
			
			delta = port.getDeltaEquity(); //get change in total equity
			if (t>1)
				ddelta = delta - prev; //change in change in total equity (dynamic hedging should cause this delta delta to converge to zero)
			System.out.println("T: " + t + "\tDELTA EQUITY: " + delta + "\tDDELTA_EQUITY: " + ddelta + "\tEQUITY: " + port.getEquity()); //dump output to console
			delay = (int) (System.currentTimeMillis() - time);
			FileOps.appendToFile(rootDir + "/equity_output.txt", t + "\t" + port.getDeltaEquity() + "\t" + ddelta + "\t" + port.toString() + "\n"); //dump output to txt file
			
			//System.out.println("T: " + t + "\t" + port.hedgeToString());
			FileOps.appendToFile(rootDir + "/hedge_output.txt", t + "\t" + port.getInitHedgeEquity() + "\t" + port.getHedgeEquity() + "\t" + port.hedgeToString() + "\n");
			
			//System.out.println("T: " + t + "\t" + port.getCorrString() + "\n");
			FileOps.appendToFile(rootDir + "/corr_output.txt", t + "\t" + port.getCorrString() + "\n");
			
			if (delay < timeStep * 1000) { //maintains minimum of 1 second delay between portfolio updates
				try {
					Thread.sleep(timeStep * 1000 - delay);
				} catch (Exception e) { e.printStackTrace(); }
			}
			t++;
			prev = delta;
		}
	}
	
	public static int getTimeStep() { return timeStep; }
	
	public static void main(String[] args) {
		long sys_time_init = System.currentTimeMillis();
		//init();
		System.out.println("Time elapsed: " + (System.currentTimeMillis()-sys_time_init)/1000 + " seconds");
	}

}
