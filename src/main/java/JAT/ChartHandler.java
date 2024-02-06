package JAT;






















/*import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import net.jacobpeterson.alpaca.AlpacaAPI;


import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.awt.Paint;
import java.util.Date;


public class ChartHandler extends XYChart<Number, Number>{
public ChartHandler(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
        Node candleStick = createCandleStick(item);
        getPlotChildren().add(candleStick);
    }

    @Override
    protected void dataItemRemoved(Data<Number, Number> item, Series<Number, Number> series) {
        Node candleStick = item.getNode();
        getPlotChildren().remove(candleStick);
    }

    @Override
    protected void dataItemChanged(Data<Number, Number> item) {
        Node candleStick = item.getNode();
        updateCandleStick(candleStick, item);
    }

    @Override
    protected void seriesAdded(Series<Number, Number> series, int seriesIndex) {
        for (Data<Number, Number> item : series.getData()) {
            Node candleStick = createCandleStick(item);
            getPlotChildren().add(candleStick);
        }
    }

    @Override
    protected void seriesRemoved(Series<Number, Number> series) {
        for (Data<Number, Number> item : series.getData()) {
            Node candleStick = item.getNode();
            getPlotChildren().remove(candleStick);
        }
    }

    @Override
    protected void layoutPlotChildren() {
        for (Series<Number, Number> item : getData()) {
            Node candleStick = item.getNode();
            layoutCandleStick(candleStick, item);
        }
    }

    private Node createCandleStick(Data<Number, Number> item) {
        Group group = new Group();

        Line highLowLine = new Line();
        Rectangle openCloseRectangle = new Rectangle();

        group.getChildren().addAll(highLowLine, openCloseRectangle);

        item.setNode(group);

        return group;
    }

    private void updateCandleStick(Node candleStick, Data<Number, Number> item) {
        // TODO: Update the candlestick based on the item's data
    }

    private void layoutCandleStick(Node candleStick, Series<Number, Number> item) {
        // TODO: Layout the candlestick based on the item's data
    }





}
    public JFreeChart newCChart( OHLCSeries series) {
        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
    if (series == null) {
        series = generatePlaceholderData("Placeholder");
    }   dataset.addSeries(series);
            JFreeChart chart = ChartFactory.createCandlestickChart(
                "Candlestick Chart", // title
                "Time", // x-axis label
                "Value", // y-axis label
                dataset, // data
                false // create legend?
            );
        
            XYPlot plot = (XYPlot) chart.getPlot();
            plot.getRangeAxis().setAutoRange(false);  // This line is important
            plot.setBackgroundPaint(null);
            plot.setDomainAxis(new DateAxis("Time")); // Set the x-axis to display dates
            plot.setRenderer(new CandlestickRenderer()); // Use a candlestick renderer
            plot.setDomainGridlinesVisible(true);
            plot.setRangeGridlinesVisible(true);
        
        return chart;
    }

    public void updateCChart(JFreeChart chart, OHLCSeries series, Canvas chartCanvas) {
    XYPlot plot = (XYPlot) chart.getPlot();
    OHLCSeriesCollection dataset = (OHLCSeriesCollection) plot.getDataset();

    // Remove all series from the dataset
    while (dataset.getSeriesCount() > 0) {
        dataset.removeSeries(0);
    }

    // Add the new series to the dataset
    if (series != null) {
        dataset.addSeries(series);
    }
    
    }

    public OHLCSeries generatePlaceholderData(String seriesKey) {
    OHLCSeries series = new OHLCSeries(seriesKey);

    // Generate data for the past 30 days
    for (int i = 29; i >= 0; i--) {
        RegularTimePeriod period = new Day(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(i)));
        double open = Math.random() * 100;
        double high = open + Math.random() * 10;
        double low = open - Math.random() * 10;
        double close = low + Math.random() * (high - low);
        series.add(period, open, high, low, close);
    }

    return series;
}
}*/
