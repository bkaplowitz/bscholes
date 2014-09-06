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
public class Bond {
	private float interestRate, price;
	private String exchange;
	public Bond(String exchange) {
		this.exchange = exchange;
		updateBondPrice();
	}
	
	public float getInterestRate() { return interestRate; }
	public float getBondPrice() { return price; }
	public String getExchange() { return exchange; }
	
	public void setBondPrice(float param) { price = param; }

	/**
	 * Update bond price / interest rate by programmatically visiting web page and storing most recent figures.
	 */
	public void updateBondPrice() {
		String grab, tmp, urlExt = "";
		try {
			if (exchange.equals("NSE")) { //National Stock Exchange of India (Bombay)
				grab = FileOps.getHTML("http://www.forexpros.com/rates-bonds/india-2-year-bond-yield");
				price = Float.parseFloat(grab.split("float_lang_base_2 bold\">")[2].split("</span")[0]);
				interestRate = Float.parseFloat(grab.split("float_lang_base_2 bold\">")[3].split("</span")[0]) * 0.01f; 
			}
			else {
				if (exchange.equals("NYSE")) //New York Stock Exchange
					urlExt = "us";
				else if (exchange.equals("ASX")) //Australian Securities Exchange (Sydney)
					urlExt = "australia";
				else if (exchange.equals("LSE")) //London Stock Exchange
					urlExt = "uk";
				grab = FileOps.getHTML("http://online.wsj.com/mdc/public/page/2_3020-treasury.html");//"http://www.bloomberg.com/markets/rates-bonds/government-bonds/" + urlExt);				
				//tmp = grab.split("<td class=\"name\">2-Year</td>")[1].split(" / ")[0].split(">")[grab.split("<td class=\"name\">2-Year</td>")[1].split(" / ")[0].split(">").length - 1].replace("-", "");
				//tmp = tmp.replace("+", "");
				//tmp = "900"; //DEBUG: having some trouble finding bond prices on the new Bloomberg page... :/
				tmp = grab.split("<td class=\"text\">")[33].split("<td class=\"num\">")[2].split("</td>")[0];
				interestRate = Float.parseFloat(grab.split("<td class=\"text\">")[33].split("<td class=\"num\">")[1].split("</td>")[0]) * 0.01f;
				try {
					/*if (tmp.indexOf(".")!=-1)
						price = Float.parseFloat(tmp.substring(0, 3) + "." + tmp.substring(3, 4));
					else*/
						price = Float.parseFloat(tmp);
					
					if (exchange.equals("ASX"))
						price = Portfolio.ausDToUSD(price);
					else if (exchange.equals("NSE"))
						price = Portfolio.rupeeToUSD(price);
					else if (exchange.equals("LSE"))
						price = Portfolio.poundsToUSD(price);
					//System.out.println("bond: " + price);
				} catch (Exception e) { e.printStackTrace(); System.out.println(tmp); }
				//interestRate = Float.parseFloat(grab.split("<td class=\"name\">2-Year</td>")[1].split("\"value\">")[1].split("<")[0]) * 0.01f;
				//interestRate = Float.parseFloat(grab.split("<td class=\'name\'>")[1].split("<td>")[1].split("\\%")[0]) * 0.01f;
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}

