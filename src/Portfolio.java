import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
public class Portfolio {
	
	//we tried implementing multi-threading, but encountered some problems
	private static final int THREAD_LIMIT = 10; //2 (cores) x 5
	private static ExecutorService exec = Executors.newFixedThreadPool(THREAD_LIMIT); //thread pool of size [THREAD_LIMIT]
	private ArrayList<Position> p; //positions in stocks (& options)
	private float initEquity; //initial equity in portfolio
	public Portfolio() {
		System.out.println("initializing portfolio...");
		p = new ArrayList<Position>();
		p.add(new Position("AAPL", "NYSE", 200)); //invest in 200 shares of Apple
		//p.add(new Position("RIO.AX", "ASX", 200));
		//p.add(new Position("SBIN", "NSE", 10000));
		//p.add(new Position("EPIS.L", "LSE", 10000));
		
		initEquity = getEquity();
	}
	
	public ArrayList<Position> getStockPos() { return p; }
	
	public float getDeltaEquity() { return getEquity() - initEquity; }
	
	/**
	 * Update position in stocks and bonds using Black-Scholes equation. **We incorrectly apply BS eqns for European options (exercised only at maturity date) to American options (exercised any time after issue).**
	 */
	public void stepBS() {
		float c, d1, d2;
		int a, b;
		int timeToMaturity; //difference between maturity date and current time (in days). NOTE: American options expire at close of NYSE (4:00PM EST)
		int currentTime;
		DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		TDistribution obj = new TDistribution(2);//initialies a TDistribution object with 2 degrees of freedom
		
		for (Position pos : p) { //iterate through different positions/stocks in portfolio
			System.out.println();
			//System.out.println(pos.getOption().getMaturityDate());
			//System.out.println(df.format(cal.getTime()));
			
			currentTime = ((((Integer.parseInt(df.format(cal.getTime()).split("/")[0]) * 30) + Integer.parseInt(df.format(cal.getTime()).split("/")[1].split(" ")[0])) * 24 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[0])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[1])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[2]);
			currentTime %= 365 * 24 * 60 * 60;
			timeToMaturity = ((Integer.parseInt(pos.getOption().getMaturityDate().split("/")[0]) * 30 + Integer.parseInt(pos.getOption().getMaturityDate().split("/")[1])) * 24 + 16) * 60 * 60
								  - currentTime;//(((Integer.parseInt(df.format(cal.getTime()).split("/")[0]) * 30 + Integer.parseInt(df.format(cal.getTime()).split("/")[1].split(" ")[0])) * 24 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[0])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[1])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[2]) % (365 * 24 * 60 * 60);
			timeToMaturity /= Script.getTimeStep();
			timeToMaturity /= 60 * 60 * 24;
									
			//System.out.println("MATURITY DATE: " + ((Integer.parseInt(pos.getOption().getMaturityDate().split("/")[0]) * 30 + Integer.parseInt(pos.getOption().getMaturityDate().split("/")[1])) * 24 + 16) * 60 * 60);
			//System.out.println("CURRENT TIME: " + currentTime);//((((Integer.parseInt(df.format(cal.getTime()).split("/")[0]) * 30) + Integer.parseInt(df.format(cal.getTime()).split("/")[1].split(" ")[0])) * 24 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[0])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[1])) * 60 + Integer.parseInt(df.format(cal.getTime()).split(" ")[1].split("\\:")[2]) % (365 * 24 * 60 * 60));
			//System.out.println("TIME TO MATURITY: " + timeToMaturity);
			
			d1 = (float)((Math.log(pos.getInitStockPrice() / pos.getOption().getStrikePrice()) + (pos.getBond().getInterestRate() + (Math.pow(pos.getVolatility(), 2) / 2)) * timeToMaturity)
						/ (pos.getVolatility() * Math.sqrt(timeToMaturity)));
			//System.out.println(pos.getInitStockPrice() + " | " + pos.getOption().getStrikePrice() + " | " + pos.getBond().getInterestRate() + " | " + pos.getVolatility() + " | " + timeToMaturity);
			//System.out.println(Math.log(pos.getInitStockPrice() / pos.getOption().getStrikePrice()) + " + " + pos.getBond().getInterestRate() + " + " + Math.pow(pos.getVolatility(), 2) / 2 + " + " + timeToMaturity
				//	+ " / " + (pos.getVolatility() * Math.sqrt(timeToMaturity)));
			//System.out.println("volatility: " + pos.getVolatility());
			System.out.println("d1: " + d1 + " | tstud(d1): " + obj.cumulative(d1));
			d2 = (float)(d1 - pos.getVolatility() * Math.sqrt(timeToMaturity));
			//System.out.println(d1 + " | " + pos.getVolatility() + " | " + Math.sqrt(timeToMaturity));
			System.out.println("d2: " + d2 + " | tstud(d2): " + obj.cumulative(d2));
			c = (float)(obj.cumulative(d1) * pos.getStockPrice() - obj.cumulative(d2) * pos.getOption().getStrikePrice() * Math.exp(-1 * pos.getBond().getInterestRate() * timeToMaturity));
			pos.setPOptPrice(c); //store predicted option price
			System.out.println("C: " + c);
			//System.out.println(N(d1) + " | " + N(d2) + " | " + Math.exp(-1 * pos.getBond().getInterestRate() * timeToMaturity));
			//System.out.println("c: " + c + " | c_mrkt: " + pos.getOption().getLastPrice());
			
			a = (int)(obj.cumulative(d1) * pos.getInitPos());
			b = (int)((a * pos.getStockPrice() - c) / pos.getBond().getBondPrice());
			System.out.println("a: " + a + " | b: " + b);
			
			pos.changeStockPos(a); //update position in stocks
			pos.changeBondPos(b); //update position in bonds			
		}
	}
	

	
	/**
	 * Cumulative distribution function for standard normal distribution (Zelen & Severo approximation).
	 */
	public float N(float x) {
		float t = (float)1 / (float)(1 + 0.2316419 * x);
		return (float)(1 - PDF(x) * (0.319381530 * t + -0.356563782 * Math.pow(t, 2) + 1.781477937 * Math.pow(t, 3) + -1.821255978 * Math.pow(t, 4) + 1.330274429 * Math.pow(t, 5)));
	}
	
	/**
	 * Probability density function for standard normal distribution.
	 */
	public float PDF(float x) {
		return (float)(1 / Math.sqrt(Math.PI) * Math.exp(-1 * Math.pow(x, 2) / 2));
	}
	
	/**
	 * Dynamic hedging to maintain riskless positions.
	 */
	public void hedge() {
		for (Position pos : p) { //iterate through positions/stocks
			pos.changeOptionPos((int)((pos.getInitHedgeEquity() - (pos.getInitHedgeStockPos() + pos.getInitHedgeOptionPos()) * pos.getStockPrice())
					/ (pos.getPredOptPrice() - pos.getStockPrice())));
			pos.changeHedgeStockPos((int)(pos.getInitHedgeStockPos() + pos.getInitHedgeOptionPos() - pos.getOptionPos()));
		}
	}
	
	/**
	 * Currency exchange (Indian rupees to US dollars).
	 */
	public static float rupeeToUSD(float rupees) throws Exception {
		//String grab = FileOps.getHTML("http://www.google.com/search?q=rupee+to+dollar");
		//return rupees * Float.parseFloat(grab.split("1 Indian rupee = ")[1].split(" US dollars")[0]);
		String grab = FileOps.getHTML("http://www.x-rates.com/calculator/?from=INR&to=USD&amount=1");
		return rupees * Float.parseFloat(grab.split("\"ccOutputRslt\">")[1].split("<")[0]);
	}
	
	/**
	 * Currency exchange (Australian dollars to US dollars).
	 */
	public static float ausDToUSD(float ausd) throws Exception {
		//String grab = FileOps.getHTML("http://www.google.com/search?q=australian+dollar+to+us+dollar");
		//return ausd * Float.parseFloat(grab.split("1 Australian dollar = ")[1].split(" US dollars")[0]);
		String grab = FileOps.getHTML("http://www.x-rates.com/calculator/?from=AUD&to=USD&amount=1");
		return ausd * Float.parseFloat(grab.split("\"ccOutputRslt\">")[1].split("<")[0]);
	}
	
	/**
	 * Currency exchange (British pounds to US dollars).
	 */
	public static float poundsToUSD(float pounds) throws Exception {
		//String grab = FileOps.getHTML("http://www.google.com/search?q=british+pounds+to+us+dollars");
		//return pounds * Float.parseFloat(grab.split("1 British pound sterling = ")[1].split(" US dollars")[0]);
		String grab = FileOps.getHTML("http://www.x-rates.com/calculator/?from=GBP&to=USD&amount=1");
		return pounds * Float.parseFloat(grab.split("\"ccOutputRslt\">")[1].split("<")[0]);
	}
	
	/**
	 * Get header for txt outputs.
	 */
	public String getHeader() { 
		String rtn = "";
		for (Position pos : p)
			rtn += pos.getTicker() + "_STOCK\t" + pos.getTicker() + "_A\t" + pos.getTicker() + "_OPTION\t" + pos.getTicker() + "_C\t" + pos.getTicker() + "_BOND\t" + pos.getTicker() + "_B\t" + pos.getTicker() + "_PREDOPTPRICE\t" + pos.getTicker() + "_MRKTOPTPRICE";
		return rtn;
	}
	
	/**
	 * Get header for correlation outputs.
	 */
	public String getCorrHeader() {
		String rtn = "";
		for (Position pos : p)
			rtn += pos.getTicker() + "_BIDASKVOLUMERATIO\t" + pos.getTicker() + "_PREDOPTPRICE\t";
		return rtn.substring(0, rtn.length() - 1);
	}
	
	/**
	 * Dumps correlations to String.
	 */
	public String getCorrString() { 
		String rtn = "";
		for (Position pos : p)
			rtn += pos.getBidAskVolRatio() + "\t" + pos.getPredOptPrice() + "\t";
		return rtn.substring(0, rtn.length() - 1);
	}
	
	/**
	 * Dumps portfolio to String for txt output.
	 */
	public String toString() { 
		String rtn = "";
		for (Position pos : p)
			rtn += (pos.getNumShares() * pos.getStockPrice()) + "\t" + pos.getNumShares() + "\t" + (pos.getOptionPos() * pos.getOption().getLastPrice()) + "\t" + pos.getOptionPos() + "\t" + (pos.getBondPos() * pos.getBond().getBondPrice()) + "\t" + pos.getBondPos() + "\t" + pos.getPredOptPrice() + "\t" + pos.getOption().getLastPrice();
		return rtn;
	}
	
	/**
	 * Get total equity in portfolio.
	 */
	public float getEquity() {
		float rtn = 0;
		for (Position pos : p) {
			rtn += pos.getNumShares() * pos.getStockPrice();
			rtn += pos.getOptionPos() * pos.getOption().getLastPrice();
			rtn += pos.getBondPos() * pos.getBond().getBondPrice();
		}
		return rtn;
	}
	
	/**
	 * Get initial equity in dynamic hedging positions.
	 */
	public float getInitHedgeEquity() {
		float rtn = 0f;
		for (Position pos : p)
			rtn += pos.getInitHedgeEquity();
		return rtn;
	}
	
	/**
	 * Get current total equity in dynamic hedging positions.
	 */
	public float getHedgeEquity() {
		float rtn = 0f;
		for (Position pos : p)
			rtn += pos.getHedgeEquity();
		return rtn;
	}
	
	/**
	 * Get header for dynamic hedging txt output.
	 */
	public String getHedgeHeader() { 
		String rtn = "";
		for (Position pos : p)
			rtn += pos.getTicker() + "_STOCKPOS\t" + pos.getTicker() + "_STOCKPRICE\t" + pos.getTicker() + "_STOCKEQUITY\t" + pos.getTicker() + "_OPTIONPOS\t" + pos.getTicker() + "_OPTIONPRICE\t" + pos.getTicker() + "_OPTIONEQUITY";
		return rtn;
	}
	
	/**
	 * Dumps dynamic hedging portfolio to String for txt output.
	 */
	public String hedgeToString() {
		String rtn = "";
		for (Position pos : p)
			rtn += pos.getNumHedgeShares() + "\t" + pos.getStockPrice() + "\t" + (pos.getNumHedgeShares() * pos.getStockPrice()) + "\t" + pos.getOptionPos() + "\t" + pos.getPredOptPrice() + "\t" + (pos.getOptionPos() * pos.getOption().getLastPrice());
		return rtn;
	}
	
	/**
	 * Update stock, bond, option prices via web-scrape.
	 */
	public void updatePrices() {
		exec = Executors.newFixedThreadPool(THREAD_LIMIT); //initalize thread pool
		DateFormat df = new SimpleDateFormat("HH");
		Calendar cal = Calendar.getInstance();
		int hour = Integer.parseInt(df.format(cal.getTime()));
		for (int i = 0; i<p.size(); i++) { //iterate through positions/stocks
			/*if ((9 <= hour && hour < 16 && pos.getExchange().equals("NYSE"))
					|| (18 <= hour && hour < 22 && pos.getExchange().equals("ASX"))
					|| (22 <= hour || hour < 3 && pos.getExchange().equals("NSE"))
					|| (3 <= hour && hour < 9 && pos.getExchange().equals("LSE"))) {*/ //24-hour trading
				//p.get(i).updateStockPrice();
				//p.get(i).getOption().updateOptionPrice();
				//p.get(i).getBond().updateBondPrice();
				exec.execute(new Updater(p.get(i)));
			//}
		}
		//shutdown threads once finished
		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS); //block until all threads finish
		} catch (Exception e) { e.printStackTrace(); }
	}
}
