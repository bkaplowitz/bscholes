import java.util.ArrayList;

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
public class Position {
	private int a; //# shares of stock owned (used to predict option price using BS equation)
	private int aHedge; //# shares of stock owned (used to maintain riskless position)
	private int initHedgeStockPos, initHedgeOptPos; //initial stock/option positions in dynamic hedging portfolio
	private int initPos; //initial position in stocks
	private String tickerSym, exchange; //ticker symbol (e.g. "AAPL", "GOOG", &c.) and stock exchange (e.g. "NYSE", "LSE", &c.)
	private float stockPrice, initStockPrice, predOptPrice, initOptPrice; //stock price, initial stock price, predicted option price, and initial market option price
	private float bidAskVolRatio; //(# bids / # asks)
	
	private ArrayList<Float> histPrices; //stores stock prices grabbed during runtime
	
	private int c; //# options owned
	private Option option; //stores information regarding option owned (strike price, maturity date, &c.)
	
	private int b; //# bonds owned
	private Bond bond; //stores information regarding bond owned (interest rate, price, &c.)
	
	public Position(String ticker) {
		this(ticker, "NYSE", 1); //default to exchange = New York Stock Exchange, initial position = 1 share
	}
	public Position(String tickerSym, String exchange, int initPos) {
		this.tickerSym = tickerSym;
		this.exchange = exchange;
		this.initPos = initPos;
		a = initPos;
		
		c = 300; //buy 300 options (arbitrary)
		initHedgeOptPos = c;
		option = new Option(tickerSym, exchange);
		initOptPrice = option.getLastPrice();
		
		aHedge = initPos;
		initHedgeStockPos = initPos;
		
		bond = new Bond(exchange); //bonds differ depending on which exchange a stock is traded on (USA vs. GB vs. India vs. ...)
		b = 1; //buy 1 bond (arbitrary)
		
		histPrices = new ArrayList<Float>();
		updateStockPrice();
		initStockPrice = stockPrice;
	}
	
	public void changeStockPos(int newPos) { a = newPos; }
	public void changeBondPos(int newPos) { b = newPos; }
	public void changeOptionPos(int newPos) { c = newPos; }
	public void changeHedgeStockPos(int newPos) { aHedge = newPos; }
	
	public void setPOptPrice(float newPrice) { predOptPrice = newPrice; }
	
	public float getInitStockPrice() { return stockPrice; }
	public int getBondPos() { return b; }
	public Bond getBond() { return bond; }
	public String getTicker() { return tickerSym; }
	public String getExchange() { return exchange; }
	public float getStockPrice() { return stockPrice; }
	public int getNumShares() { return a; }
	public int getOptionPos() { return c; }
	public Option getOption() { return option; }
	public int getInitPos() { return initPos; }
	public float getPredOptPrice() { return predOptPrice; }
	public float getNumHedgeShares() { return aHedge; }
	public float getBidAskVolRatio() { return bidAskVolRatio; }
	
	/**
	 * Get total equity in dynamic hedging position.
	 */
	public float getHedgeEquity() { return aHedge * stockPrice + c * predOptPrice; }
	
	public float getInitHedgeStockPos() { return initHedgeStockPos; }
	public float getInitHedgeOptionPos() { return initHedgeOptPos; }
	
	public float getInitHedgeEquity() {
		return initHedgeStockPos * initStockPrice + initHedgeOptPos * initOptPrice;
	}
	
	/**
	 * Get volatility of stock price.
	 */
	public float getVolatility() {		
		float rtn = 0f;
		if (exchange.equals("NYSE")) { //New York Stock Exchange
			String grab;
			try {
				grab = FileOps.getHTML("http://quote.morningstar.com/Option/Options.aspx?ticker=" + tickerSym);
				rtn = Float.parseFloat(grab.split("Statistical</td>")[1].split("class=Text>")[5].split("</td>")[0]) * 0.01f; //1-month volatility
				//System.out.println("sigma: " + rtn);
			} catch (Exception e) { System.out.println("error: failed to retrieve annualized volatility for " + tickerSym); }//FIX
		}
		else if (exchange.equals("ASX")) { //Australian Securities Exchange (Sydney)
			String grab;
			try {
				grab = FileOps.getHTML("http://www.macroaxis.com/invest/market/RIO.AX--volatility--Rio_Tinto_Ltd");
				rtn = Float.parseFloat(grab.split("(volatility)")[1].split("&nbsp;")[1]); //???
			} catch (Exception e) { e.printStackTrace(); System.out.println("error: failed to retrieve annualized volatility for " + tickerSym); }
			rtn = 0.0167f;
		
		}
		else if (exchange.equals("LSE")) { //London Stock Exchange
			
		}
		else if (exchange.equals("NSE")) { //National Stock Exchange of India (Bombay)
			
		}
		return rtn;
		
		/*float rtn = 0, mean = 0;
		for (int i = 1; i<histPrices.size(); i++)
			mean += (float)Math.log(histPrices.get(i) / initStockPrice) / (float)(i);
		mean /= histPrices.size();
		for (int i = 0; i<histPrices.size(); i++)
			rtn += Math.pow(mean - (float)Math.log(histPrices.get(i) / initStockPrice) / (float)(i + 1), 2);
		rtn = (float)Math.sqrt(rtn / histPrices.size());
		rtn /= Math.sqrt(252); //_annualized_ volatility
		rtn *= 100; //DEBUG
		System.out.println("sigma: " + rtn);*/
		
		//BRANDON----
		/*double rtn = 0f;
		double alpha = 20;
		double time = histPrices.size();
		String grab;
		try {
			grab = FileOps.getHTML("http://quote.morningstar.com/Option/Options.aspx?ticker=" + tickerSym);
			rtn = Double.parseDouble(grab.split("Statistical</td>")[1].split("class=Text>")[5].split("</td>")[0]) * 0.01; //1-year volatility
			//System.out.println("sigma: " + rtn);
		 rtn=rtn*Math.sqrt(252);
		 rtn*= Math.pow(time, 1/alpha);
		} catch (Exception e) {System.out.println("error: failed to retrieve annualized volatility for " + tickerSym); }
		return rtn;//Play around with... maybe try out with time to maturity or days?*/
		//-----------
	}
	
	/**
	 * Update stock price by programmatically visiting web page and storing most recent price.
	 */
	public void updateStockPrice() {
		String grab;
		try {
			grab = FileOps.getHTML("http://download.finance.yahoo.com/d/quotes.csv?s=" + tickerSym + "&f=sd1t1l1va2abc1ghk3ops7&e=.csv");
			//System.out.println(grab);
			stockPrice = Float.parseFloat(grab.split(",")[3]);
			if (exchange.equals("NSE"))
				stockPrice = Portfolio.rupeeToUSD(stockPrice);
			else if (exchange.equals("LSE"))
				stockPrice = Portfolio.poundsToUSD(stockPrice);
			else if (exchange.equals("ASX"))
				stockPrice = Portfolio.ausDToUSD(stockPrice);
			stockPrice += (Math.random() * 0.1 - 0.05) * stockPrice; //give stock price an artificial, random variance of 5% (because natural jitters aren't exciting enough for our demo)
			//System.out.println("stock (" + tickerSym + "): " + stockPrice);
			histPrices.add(stockPrice);
			grab = FileOps.getHTML("http://finance.yahoo.com/q?s=" + tickerSym);
			bidAskVolRatio = Float.parseFloat(grab.split("<small> x <span")[1].split("\">")[1].split("</span>")[0]) 
								/ Float.parseFloat(grab.split("<small> x <span")[2].split("\">")[1].split("</span>")[0]);
		} catch (Exception e) { e.printStackTrace(); System.out.println("error: failed to retrieve stock price for " + tickerSym); }
	}
}
