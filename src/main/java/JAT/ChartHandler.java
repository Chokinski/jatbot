package JAT;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;

import javafx.scene.paint.Color;
import net.jacobpeterson.alpaca.AlpacaAPI;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.awt.Paint;
import java.util.Date;
import org.jfree.data.time.Second;

public class ChartHandler {
    AlpacaController ac = new AlpacaController();
    public AlpacaAPI alpacaAPI = ac.connect();
    public void createCandlestickChart() {
        OHLCSeries series = new OHLCSeries("Series Name");

        // Add data to the series
        // For example, add a new bar every second
        for (int i = 0; i < 60; i++) {
            RegularTimePeriod date = new Second(new Date());
            double open = Math.random() * 100;
            double close = open + Math.random() * 10 - 5;
            double high = Math.max(open, close) + Math.random() * 10;
            double low = Math.min(open, close) - Math.random() * 10;
            double volume = Math.random() * 100;
            series.add(date, open, high, low, close);
        }

        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(series);
    
        JFreeChart chart = ChartFactory.createCandlestickChart(
            "Candlestick Chart", // title
            "Time", // x-axis label
            "Value", // y-axis label
            dataset, // data
            false // create legend?
        );
    
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(new DateAxis("Time")); // Set the x-axis to display dates
        plot.setRenderer(new CandlestickRenderer()); // Use a candlestick renderer
    
        ChartPanel panel = new ChartPanel(chart);
        // add panel to your JavaFX or Swing container
    }
    public JFreeChart getChart() {
        //Get candlestick chart
        return null;

    }
    public JFreeChart newCChart() {
        OHLCSeries series = new OHLCSeries("Series Name");
    
        // Add data to the series
        // For example, add a new bar every millisecond
        for (int i = 0; i < 60; i++) {
            try {
                Thread.sleep(1); // wait for 1 millisecond
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            RegularTimePeriod date = new Millisecond(new Date());
            double open = Math.random() * 100;
            double close = open + Math.random() * 10 - 5;
            double high = Math.max(open, close) + Math.random() * 10;
            double low = Math.min(open, close) - Math.random() * 10;
            double volume = Math.random() * 100;
            series.add(date, open, high, low, close);
        }
    
        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(series);
    
        JFreeChart chart = ChartFactory.createCandlestickChart(
            "Candlestick Chart", // title
            "Time", // x-axis label
            "Value", // y-axis label
            dataset, // data
            false // create legend?
        );
    
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setDomainAxis(new DateAxis("Time")); // Set the x-axis to display dates
        plot.setRenderer(new CandlestickRenderer()); // Use a candlestick renderer
        
    
        return chart;
    }
}
