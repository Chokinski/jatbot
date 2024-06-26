package JAT;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import javafx.application.Platform;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import java.awt.geom.Rectangle2D;


import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;

import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import javafx.stage.Stage;

import net.jacobpeterson.alpaca.model.endpoint.clock.Clock;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.jat.PlotHandler;
import com.jat.OHLCChart;
import com.jat.OHLCData;


/**
 * The DashController class is responsible for controlling the dashboard view of
 * the application.
 * It handles the initialization of UI elements, loading and saving properties,
 * connecting to the Alpaca API,
 * and managing the stream listener.
 */
public class DashController {

    @FXML
    private VBox vbDash;

    @FXML
    private AnchorPane gradientSeparator;
    @FXML
    private AnchorPane nodeChart;
    @FXML
    private AnchorPane nodeToggle;
    @FXML
    private ScrollPane parentNode;

    @FXML
    private MenuButton menuChart;

    @FXML
    private javafx.scene.canvas.Canvas chartCanvas;

    @FXML
    private Label lblAPIstatus;

    @FXML
    private Label lblCryptoStreamStatus;

    @FXML
    private Label lblStockStreamStatus;
    @FXML
    private Label lblChecking;
    @FXML
    private Label lblMarketTime;

    @FXML
    private Label lblTimeStatus;

    @FXML
    private Label lblMarketIndicator;

    @FXML
    private Label lblMarketIndicator1;

    @FXML
    private Button btnCheckStatus;
    @FXML
    private Button btnTryLogin;
    @FXML
    private Button btnDisconnect;
    @FXML
    private Button btnReconnect;
    @FXML
    private Button btnGetCrypto;
    @FXML
    private Button btnGetStock;
    @FXML
    private Button btnLogData;
    @FXML
    private Button btnBacktest;

    @FXML
    private Slider slideQuantity;

    @FXML
    private ToggleButton tbtnDef1D;

    @FXML
    private ToggleButton tbtnDef1MIN;

    @FXML
    private ToggleButton tbtnDef1MON;

    @FXML
    private ToggleButton tbtnDef1W;

    @FXML
    private ToggleButton tbtnDef1Y;

    @FXML
    private ToggleButton tbtnDef4H;

    @FXML
    private ToggleButton tbtnDef4MON;
    
    @FXML
    private ToggleButton tbtnHourly;
    @FXML
    private ToggleButton tbtnToggleStream;
    @FXML
    private Button btnSetTExt;

    @FXML
    private ListView<String> lvAccTypes;
    @FXML
    private ListView<String> lvAccValues;
    @FXML
    private ListView<String> lvDataDisplay;

    @FXML
    private TextField tfKey_ID;

    @FXML
    private PasswordField tfSec_ID;

    @FXML
    private TextField tfSymboltoGet;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;
    
    
    private double xOffset;
    private double yOffset;
    // Variables to store the initial position of a drag event
    private double dragStartX;
    private double dragStartY;
    
    // Variables to store the current translation of the chart
    private double translateX = 0.0;
    private double translateY = 0.0;
    
    // Variables to store the current scale of the chart
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private String streamChoice = "Stocks";
    private BarTimePeriod selectedTimePeriod = BarTimePeriod.DAY; // Default value
    private int selectedDuration = 1; // Default value
    private StreamListener streamListener;
    private AlpacaController ac = new AlpacaController();
    private OHLCChart chart;
    private PlotHandler ph = new PlotHandler();
    private Backtesting bt = new Backtesting(ac, 500);
    // private JFreeChart chart;

    @FXML
    public void initialize() throws IOException {
        try {
            this.streamListener = new StreamListener();
            
            
        } catch (Exception e) {
            JATbot.botLogger.error("Error initializing DashController: " + e.getMessage());
        }
        
        // Add the strings to the ListView when the scene is loaded
        
        Platform.runLater(() -> {
            ac = new AlpacaController();
            lblChecking.setText("");
            lvDataDisplay.getItems().addAll("");
            lvAccTypes.getItems().addAll(
                    "Account ID", "Portfolio Value", "Account Created", "Account Status",
                    "Account Cash", "Buying Power", "Day Trade Count", "Day Trade Limit",
                    "Equity", "Initial Margin", "Last Equity", "Last Maintenance Margin");
                    lvAccValues.getItems().addAll(ac.logAccID(), ac.logPortValue(),
                    ac.logCreateDate(), ac.logAccStatus(),
                    ac.logAccCash(), ac.logBuyingPower(),
                    ac.logDayTradeCount(), ac.logDayTradeLimit(),
                    ac.logEquity(), ac.logInitialMargin(),
                    ac.logLastEquity(), ac.logLastMaintenanceMargin(),
                    "");
                    //nodeChart.setMouseTransparent(true);
                    //redrawChart();
                    
                    // Redraw the chart when the canvas size changes
                    //chartCanvas.widthProperty().addListener(obs -> redrawChart());
                    //chartCanvas.heightProperty().addListener(obs -> redrawChart());
                    startGradientAnimation();
                    animateBackgroundColor();
                    startMarketTimeUpdate();});

    }

    private void redrawChart() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, chartCanvas.getWidth(), chartCanvas.getHeight());
        gc.save();
        gc.translate(translateX, translateY);
        gc.scale(scaleX, scaleY);
        Rectangle2D drawArea = new Rectangle2D.Double(0, 0, chartCanvas.getWidth(), chartCanvas.getHeight());
        // chart.draw(new FXGraphics2D(gc), drawArea);
        gc.restore();
    }

    @FXML
    public void onClose() {

        streamListener.disconnectAlpacaAPI();

    }

    @FXML
    void onDragged(MouseEvent event) {
        mainWindow.setX(event.getScreenX() - xOffset);
        mainWindow.setY(event.getScreenY() - yOffset);
    }

    @FXML
    void onPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    void onBtnRetryLogin(ActionEvent event) {

        String keyID = tfKey_ID.getText();
        String secretKey = tfSec_ID.getText();
        // connect to Alpaca
        ac.connect();
        this.onReconnect(event);

    }

    @FXML
    void onReconnect(ActionEvent event) {
        streamListener.disconnectStream();
        if ("Stocks".equals(this.streamChoice)) {
            streamListener.connectStockStream(); // Connect the stock stream when the button is not selected
        } else {
            streamListener.connectCryptoStream();
        }
        onCheckStatus(event);

    }

    @FXML
    void onDisconnectStream(ActionEvent event) {
        streamListener.disconnectStream();
        onCheckStatus(event);
    }

    @FXML
    void onStreamConnect(ActionEvent event) {
        onDisconnectStream(event);
        streamListener.connectCryptoStream();
    }

    @FXML
    void onCheckStatus(ActionEvent event) {
        final String[] dots = new String[] { "", ".", "..", "..." };
        final AtomicInteger counter = new AtomicInteger(0);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.06), evt -> {
            int index = counter.getAndIncrement() % dots.length;
            lblChecking.setText("Checking" + dots[index]);
        }));

        timeline.setCycleCount(80);
        timeline.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            timeline.stop();
            lblChecking.setText("");

            if (ac.getAccount() != null) {
                JATbot.botLogger.info("Account is connected");
                lblAPIstatus.setText("Connected");
                lblAPIstatus.setTextFill(Color.GREEN);
            } else {
                JATbot.botLogger.info("Account is disconnected");
                lblAPIstatus.setText("Disconnected");
                lblAPIstatus.setTextFill(Color.RED);
            }

            if (streamListener.areStreamsConnected() == null) {
                JATbot.botLogger.info("Both streams are disconnected");
                lblStockStreamStatus.setText("Disconnected");
                lblStockStreamStatus.setTextFill(Color.RED);
                lblCryptoStreamStatus.setText("Disconnected");
                lblCryptoStreamStatus.setTextFill(Color.RED);
            } else {
                if (streamListener.areStreamsConnected()[0]) {
                    JATbot.botLogger.info("Stock stream is connected");
                    lblStockStreamStatus.setText("Connected");
                    lblStockStreamStatus.setTextFill(Color.GREEN);
                } else {
                    JATbot.botLogger.info("Stock stream is disconnected");
                    lblStockStreamStatus.setText("Disconnected");
                    lblStockStreamStatus.setTextFill(Color.RED);
                }

                if (streamListener.areStreamsConnected()[1]) {
                    JATbot.botLogger.info("Crypto stream is connected");
                    lblCryptoStreamStatus.setText("Connected");
                    lblCryptoStreamStatus.setTextFill(Color.GREEN);
                } else {
                    JATbot.botLogger.info("Crypto stream is disconnected");
                    lblCryptoStreamStatus.setText("Disconnected");
                    lblCryptoStreamStatus.setTextFill(Color.RED);
                }
            }
        });
        pause.play();
    }

    @FXML
    void onGetStock(ActionEvent event) {
        streamListener.listenToStock(Arrays.asList(tfSymboltoGet.getText()));
    }

    public void updateTokenDisplay(String text) {
        Platform.runLater(() -> {
            lvDataDisplay.getItems().addAll(text);
        });
    }

    @FXML
    void onGetCrypto() {
        streamListener.listenToCoin(Arrays.asList(tfSymboltoGet.getText()));
    }

    // Sets the text of the button
    @FXML
    void setText(ActionEvent event) {
        String sym = tfSymboltoGet.getText();
        String startDate = dpStartDate.getValue().toString();
        String endDate = dpEndDate.getValue().toString();

        try {
            LocalDate startParse = LocalDate.parse(startDate);
            LocalDate endParse = LocalDate.parse(endDate);

            int startYear = startParse.getYear();
            int startMonth = startParse.getMonthValue();
            int startDay = startParse.getDayOfMonth();

            int endYear = endParse.getYear();
            int endMonth = endParse.getMonthValue();
            int endDay = endParse.getDayOfMonth();

            // Debugging statements
            JATbot.botLogger.info("Start Date: " + startParse);
            JATbot.botLogger.info("End Date: " + endParse);
            JATbot.botLogger.info("Start Year: " + startYear);
            JATbot.botLogger.info("Start Month: " + startMonth);
            JATbot.botLogger.info("Start Day: " + startDay);
            JATbot.botLogger.info("End Year: " + endYear);
            JATbot.botLogger.info("End Month: " + endMonth);
            JATbot.botLogger.info("End Day: " + endDay);

            ObservableList<OHLCData> series = ac.getBarsData(sym, startYear, startMonth, startDay, endYear, endMonth,
                    endDay, selectedTimePeriod, selectedDuration);
            this.ph.showOHLCChart(this.parentNode, this.nodeChart, true, 0, series);
            this.chart = ph.getOHLCChart();
            chart.setTitle(sym.toString());
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date: " + e.getMessage());
            // Handle the parsing error appropriately, e.g., show an error message to the
            // user
        } catch (Exception e) {
            e.printStackTrace();
            // Handle other potential exceptions
        }
    }

    // Method to update the label with the market time
    public void updateMarketTimeLabel() {
        //System.out.println("\nInside updateMarrkeTimeLabel.");
        Clock clock = ac.getMarketTime();
        String formattedTime = formatMarketTime(clock); // Format the market time as needed
        Platform.runLater(() -> {
            lblMarketTime.setText(formattedTime);
            if(clock.getIsOpen()) {
                lblMarketIndicator1.setText("OPEN");
                lblMarketIndicator1.setTextFill(Color.GREEN);
                lblTimeStatus.setText("Closes at: " + formattoHHMM(clock.getNextClose()));
            } else {
                lblMarketIndicator1.setText("CLOSED");
                lblMarketIndicator1.setTextFill(Color.RED);
                lblTimeStatus.setText("Opens at: " + formattoHHMM(clock.getNextOpen()));
            }
        }); // Update label on JavaFX Application Thread
        

        
    }

    // Method to format the market time as needed
    private String formatMarketTime(Clock clock) {
        // System.out.println("\n\nInside formatter....");
        ZonedDateTime cur = clock.getTimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = formatter.format(cur);
        //System.out.println("\n\n\nReceived and formatted to: " + formattedTime);
        return formattedTime;
    }

    private String formattoHHMM(ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = formatter.format(time);
        return formattedTime;
    }

    // Other code...

    // Method to start updating the market time label periodically
    public void startMarketTimeUpdate() {
        //System.out.println("\nStarting market time update.");
        // Create a scheduled executor to periodically update the market time label
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updateMarketTimeLabel, 0, 1, TimeUnit.SECONDS); // Update every second
    }

    private Stage mainWindow;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void startGradientAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gradientSeparator.backgroundProperty(), createGradient(0))),
                new KeyFrame(Duration.seconds(4),
                        new KeyValue(gradientSeparator.backgroundProperty(), createGradient(50))),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(gradientSeparator.backgroundProperty(), createGradient(100))));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void animateBackgroundColor() {
        // Find the node with the id nodeToggle
        AnchorPane nodeToggle = (AnchorPane) vbDash.lookup("#nodeToggle");

        // Create a timeline for color animation
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(nodeToggle.backgroundProperty(), createGradient(0))),
                new KeyFrame(Duration.seconds(4), new KeyValue(nodeToggle.backgroundProperty(), createGradient(50))),
                new KeyFrame(Duration.seconds(8), new KeyValue(nodeToggle.backgroundProperty(), createGradient(100))));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private Background createGradient(double stripePercentage) {
        double stripePosition = stripePercentage / 100.0;

        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(stripePosition, Color.web("#f6f6f492")),
                new Stop(stripePosition, Color.web("#331872")),
                new Stop(1.0, Color.web("#f6f6f492")));

        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }

    private Background createGradient(int stripePercentage) {
        double stripePosition = stripePercentage / 100.0;

        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(stripePosition, Color.web("#6931ec")),
                new Stop(stripePosition, Color.web("#2E3436")),
                new Stop(1.0, Color.web("#F6F6F4")));

        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }

    @FXML
    public void onToggleStream(ActionEvent event) {
        ToggleButton selectedButton = (ToggleButton) event.getSource();
        String buttonId = selectedButton.getId();

        switch (buttonId) {
            case "tbtnToggleStream":
                if (selectedButton.isSelected()) {
                    animateBackgroundColor();
                    this.streamChoice = "Crypto";

                } else {
                    this.streamChoice = "Stocks";
                    
                }
                break;
            default:
                break;
            // Add more cases if you have more toggle buttons
        }
    }

    @FXML
    public void onTimePeriodToggle(ActionEvent event) {
        ToggleButton selectedButton = (ToggleButton) event.getSource();
        String buttonId = selectedButton.getId();

        switch (buttonId) {
            case "tbtnDef1D":
                selectedTimePeriod = BarTimePeriod.DAY;
                selectedDuration = 1;
                JATbot.botLogger.info("1D selected");
                break;
            case "tbtnDef1MIN":
                selectedTimePeriod = BarTimePeriod.MINUTE;
                JATbot.botLogger.info("1MIN selected");
                break;
            case "tbtnDef1MON":
                selectedTimePeriod = BarTimePeriod.MONTH;
                JATbot.botLogger.info("1MON selected");
                break;
            case "tbtnDef1W":
                selectedTimePeriod = BarTimePeriod.WEEK;
                JATbot.botLogger.info("1W selected");
                break;
            case "tbtnDef1Y":
                selectedTimePeriod = BarTimePeriod.MONTH;
                JATbot.botLogger.info("1Y selected");
                break;
            case "tbtnDef4H":
                selectedTimePeriod = BarTimePeriod.HOUR;
                selectedDuration = 4;
                JATbot.botLogger.info("4H selected");
                break;
            case "tbtnDef4MON":
                selectedTimePeriod = BarTimePeriod.MONTH;
                selectedDuration = 4;
                JATbot.botLogger.info("4MON selected");
                break;
            case "tbtnHourly":
                selectedTimePeriod = BarTimePeriod.HOUR;
                selectedDuration = 1;
                JATbot.botLogger.info("Hourly selected");
                break;
            default:
                break;
            // Add more cases if you have more toggle buttons
        }
    }

    @FXML
    public void onLogData(ActionEvent event) {
    }

    @FXML
    public void onBacktest(ActionEvent event) throws IOException {
        String sym = tfSymboltoGet.getText();
        bt.run(sym);
        ObservableList<OHLCData> series = bt.getData(sym);
        this.ph.showOHLCChart(this.parentNode, this.nodeChart, true, 0, series);
        this.chart = ph.getOHLCChart();

        // Get the results from passResults() and add them to lvDataDisplay
        String[] results = bt.passResults();
        lvDataDisplay.getItems().addAll(results);
    }

}
