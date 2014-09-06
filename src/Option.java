import java.util.HashMap;

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
public class Option {
	private String tickerSym, exchange;
	private float strikePrice, lastPrice, bid, ask;
	private String maturityDate; //format --> "**/**/**
	
	//needed when converting from web-scraped dates to usable 'maturityDate' values
	private static HashMap<String, String> mmap = new HashMap<String, String>();
	static {
		mmap.put("January", "01");
		mmap.put("February", "02");
		mmap.put("March", "03");
		mmap.put("April", "04");
		mmap.put("May", "05");
		mmap.put("June", "06");
		mmap.put("July", "07");
		mmap.put("August", "08");
		mmap.put("September", "09");
		mmap.put("October", "10");
		mmap.put("November", "11");
		mmap.put("December", "12");
	};
	private static HashMap<String, String> mmap2 = new HashMap<String, String>();
	static {
		mmap.put("JAN", "01");
		mmap.put("FEB", "02");
		mmap.put("MAR", "03");
		mmap.put("APR", "04");
		mmap.put("MAY", "05");
		mmap.put("JUN", "06");
		mmap.put("JUL", "07");
		mmap.put("AUG", "08");
		mmap.put("SEP", "09");
		mmap.put("OCT", "10");
		mmap.put("NOV", "11");
		mmap.put("DEC", "12");
	};	
	public Option(String ticker) {
		this(ticker, "", "NYSE", -1);
	}
	public Option(String ticker, String exchange) {
		this(ticker, "", exchange, -1);
	}
	public Option(String ticker, String mDate, String exchange, float sPrice) {
		tickerSym = ticker;
		maturityDate = mDate;
		strikePrice = sPrice;
		this.exchange = exchange;
		updateOptionPrice();
	}
	
	public float getStrikePrice() { return strikePrice; }
	public float getLastPrice() { return lastPrice; }
	
	public String getMaturityDate() { return maturityDate; }
	public String getTicker() { return tickerSym; }
	public String getExchange() { return exchange; }
	
	/**
	 * Update option price / strike price / maturityDate by programmatically visiting web page and storing most recent figures.
	 */
	public void updateOptionPrice() {
		String grab, tmp;
		String[] split, temp;
		int count;
		try {
			if (exchange.equals("NYSE")) { //New York Stock Exchange
				if (maturityDate.equals("")) {
					grab = FileOps.getHTML("http://finance.yahoo.com/q/op?s=" + tickerSym);
					temp = grab.split("Expire at close ")[1].split("</td>")[0].split(" ");
					maturityDate = mmap.get(temp[1]) + "/" + temp[2].split(",")[0] + "/" + temp[3].substring(temp[3].length() - 2, temp[3].length());
				}
				else
					grab = FileOps.getHTML("http://finance.yahoo.com/q/op?s=" + tickerSym + "&m=20" + maturityDate.split("/")[2] + "-" + maturityDate.split("/")[0]);
				
				split = grab.split("<strong>");
	
				if (strikePrice<0) {
					lastPrice = Float.parseFloat(split[2].split("<b>")[1].split("</b>")[0]);
					strikePrice = Float.parseFloat(split[2].split("</strong>")[0]);
				}
				else
					lastPrice = Float.parseFloat(grab.split("<strong>" + sFormat(strikePrice) + "</strong>")[1].split("<b>")[1].split("</b>")[0]);
			}
			else if (exchange.equals("ASX")) { //Australian Securities Exchange (Sydney)
				grab = FileOps.getHTML("http://www.asx.com.au/asx/markets/optionPrices.do?by=underlyingCode&underlyingCode=" + tickerSym.split("\\.")[0]);
				tmp = grab.split("<th scope=\"row\" class=\"row\">")[3].split("</td>")[0].split("<td>")[1]; //first option usually has weird strike price / is a european-style call option
				maturityDate = tmp.split("/")[1] + "/" + tmp.split("/")[0] + "/" + tmp.substring(tmp.length() - 2);
				strikePrice = Portfolio.ausDToUSD(Float.parseFloat(grab.split("<th scope=\"row\" class=\"row\">")[3].split("<td>")[3].split("</td>")[0]));
				lastPrice = Portfolio.ausDToUSD(Float.parseFloat(grab.split("<th scope=\"row\" class=\"row\">")[3].split("<td>")[4].split("</td>")[0]));
			}
			else if (exchange.equals("NSE")) { //National Stock Exchange of India (Bombay)
				grab = FileOps.getHTML("http://www.nseindia.com/content/fo/fomktwtch_OPTSTK.htm");
				split = grab.split(">" + tickerSym + "</a>");
				count = 1;
				while (count < split.length && split[count].indexOf(">CE</a></td>")==-1)
					count++;
				//ignore case where there are no options listed for 'tickerSym' / no calls (only puts) are listed
				temp = split[count].split("</a></td>")[0].split(">");
				tmp = temp[temp.length - 1];
				if ((int)tmp.charAt(1) <= 9)
					maturityDate = mmap2.get(tmp.substring(2, 2+3)) + "/" + tmp.substring(0, 2) + "/" + tmp.substring(tmp.length() - 2);
				else
					maturityDate = mmap2.get(tmp.substring(1, 1+3)) + "/" + tmp.substring(0, 1) + "/" + tmp.substring(tmp.length() - 2);
				temp = split[count].split("</a></td>")[2].split(">");
				strikePrice = Portfolio.rupeeToUSD(Float.parseFloat(temp[temp.length - 1]));
				lastPrice = Portfolio.rupeeToUSD(Float.parseFloat(split[count].split("<td class=t1>")[1].split("<")[0]));
			}
			else if (exchange.equals("LSE")) { //London Stock Exchange
				//...can't find any option listings for LSE stocks
			}
			//after NYSE, I stopped caring about scraping for specified maturity dates / strike prices (since Black-Scholes specifies neither)
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Helper method for web-scraping in updateOptionPrice()
	 */
	public static String sFormat(float param) {
		String rtn = Float.toString(param);
		while(rtn.split("\\.")[1].length() < 2)
			rtn += "0";
		return rtn;
	}
}
