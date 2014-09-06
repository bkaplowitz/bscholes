import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

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
public class Grapher extends ApplicationFrame implements ActionListener {

    private TimeSeries stockPrice, predOptPrice, mrktOptPrice, hedgeStockEquity, hedgeOptEquity;

    public Grapher(String title) {
        super(title);
        stockPrice = new TimeSeries("Stock Price", Millisecond.class);
        predOptPrice = new TimeSeries("Predicted Option Price", Millisecond.class);
        mrktOptPrice = new TimeSeries("Market Option Price", Millisecond.class);
        hedgeStockEquity = new TimeSeries("Equity in Stock", Millisecond.class);
        hedgeOptEquity = new TimeSeries("Equity in Options", Millisecond.class);

        JPanel content = new JPanel(new GridLayout(0, 1));
        
        TimeSeriesCollection dataset = new TimeSeriesCollection(stockPrice);
        dataset.addSeries(predOptPrice);
        JFreeChart chart = createChart(dataset, "", 0, 700);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 300));
        content.add(chartPanel);
        
        dataset = new TimeSeriesCollection(predOptPrice);
        dataset.addSeries(mrktOptPrice);
        chart = createChart(dataset, "", 200, 400);//0, 200);//200, 400);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 300));
        content.add(chartPanel);
        
        dataset = new TimeSeriesCollection(hedgeStockEquity);
        dataset.addSeries(hedgeOptEquity);
        chart = createChart(dataset, "", 50000, 150000);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 300));
        content.add(chartPanel);
        
        setContentPane(content);
    }

    JFreeChart createChart(XYDataset dataset, String chartTitle, int yMin, int yMax) {
        JFreeChart result = ChartFactory.createTimeSeriesChart(
            chartTitle, 
            "Time", 
            "Value (USD)",
            dataset, 
            true, 
            true, 
            false
        );
        XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000 * 5);//60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(yMin, yMax);
        return result;
    }
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public TimeSeries getStockPrice() { return stockPrice; }
	public TimeSeries getPredOptPrice() { return predOptPrice; }
	public TimeSeries getMrktOptPrice() { return mrktOptPrice; }
	public TimeSeries getHedgeStockEquity() { return hedgeStockEquity; }
	public TimeSeries getHedgeOptEquity() { return hedgeOptEquity; }

    public static void main(String[] args) {
        Grapher demo = new Grapher("Black-Scholes Options Pricing Prediction");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        Script.init(demo.getStockPrice(), demo.getPredOptPrice(), demo.getMrktOptPrice(), demo.getHedgeStockEquity(), demo.getHedgeOptEquity());
    }
}