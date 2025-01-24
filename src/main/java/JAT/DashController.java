package JAT;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiClient;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import net.jacobpeterson.alpaca.openapi.trader.model.Clock;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.jat.PlotHandler;
import com.jat.OHLCChart;
import com.jat.OHLCData;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockSnapshot;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;

/**
 * The DashController class is responsible for controlling the dashboard view of
 * the application.
 * It handles the initialization of UI elements, loading and saving properties,
 * connecting to the Alpaca API,
 * and managing the stream listener.
 */
public class DashController {

    @FXML
    private Button btnBacktest;

    @FXML
    private Button btnCheckStatus;

    @FXML
    private Button btnDisconnect;

    @FXML
    private Button btnGetCrypto;

    @FXML
    private Button btnGetStock;

    @FXML
    private Button btnLogData;

    @FXML
    private Button btnReconnect;

    @FXML
    private Button btnSetText;

    @FXML
    private Button btnTryLogin;

    @FXML
    private Button btnBuy;

    @FXML
    private Button btnSell;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private AnchorPane gradientSeparator;

    @FXML
    private Label lblAPIstatus;

    @FXML
    private Label lblChecking;

    @FXML
    private Label lblCryptoStreamStatus;

    @FXML
    private Label lblMarketIndicator;

    @FXML
    private Label lblMarketIndicator1;

    @FXML
    private Label lblMarketTime;

    @FXML
    private Label lblStockStreamStatus;

    @FXML
    private Label lblTimeStatus;

    @FXML
    private Label lblTimeFrameState;
    @FXML
    private ListView<String> lvAccTypes;

    @FXML
    private ListView<String> lvAccValues;

    @FXML
    private ListView<String> lvDataDisplay;

    @FXML
    private MenuButton menuChart;

    @FXML
    private AnchorPane nodeChart;

    @FXML
    private AnchorPane nodeToggle;

    @FXML
    private ScrollPane parentNode;

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
    private TextField tfKey_ID;

    @FXML
    private TextField tfDur;

    @FXML
    private PasswordField tfSec_ID;

    @FXML
    private TextField tfSymboltoGet;
    @FXML
    private TextField tfVol;
    @FXML
    private VBox vbDash;
    @FXML
    private ImageView maxControl;

    @FXML
    private AnchorPane exitControl;
    @FXML
    private ChoiceBox<String> cbPd;

    @FXML
    private ChoiceBox<String> cbAssetClass;

    @FXML
    private TreeTableView<List<Object>> tbWatchlist;
    @FXML
    private TreeTableColumn<List<Object>, String> colAssets;
    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetBid;
    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetBidVol;
    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetAsk;
    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetAskVol;
    @FXML
    private TreeTableColumn<List<Object>, Number> colDaily;
    



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
    private static String selectedTimePeriod = "1Day"; // Default value
    //private int selectedDuration.set(1; // Default value
    private IntegerProperty selectedDuration = new SimpleIntegerProperty(1); // Default value
    private StringProperty selectedPeriod = new SimpleStringProperty("Day"); // Default value
    private StreamListener streamListener;//"15Min", , "1Week", "1Month"
    private List<String> timeframes = new ArrayList<>(Arrays.asList("4Hour", "1Day"));
    private AlpacaController ac;
    private OHLCChart chart;
    private PlotHandler ph = new PlotHandler();
    //private ApiClient adc;
    private List<Assets> assets;
    private List<String> watchlist = new ArrayList<>(Arrays.asList("AAPL","TSLA","MSFT","META","GOLD"));
    
    public JATInfoHandler jai = new JATInfoHandler();
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
            
            vbDash.setPrefSize(1044, 702);
            vbDash.setMinSize(1044, 702);
            vbDash.setMaxSize(1044, 702);
            this.mainWindow.setFullScreenExitHint("tryhard...");
            ac = new AlpacaController();
            assets = ac.getAssets();
            //adc = ac.alpaca.marketData().getInternalAPIClient();
            for(String x : watchlist){
                System.out.println(x);
            }
            addInfo();
            jai.resetRate();
            //jai.dataFromList(assets,watchlist, ac.stockH,ac.assetH, timeframes);

            
            

            // nodeChart.setMouseTransparent(true);
            // redrawChart();

            // Redraw the chart when the canvas size changes
            // chartCanvas.widthProperty().addListener(obs -> redrawChart());
            // chartCanvas.heightProperty().addListener(obs -> redrawChart());
            startGradientAnimation();
            startMarketTimeUpdate();
            provideListeners();
            tfSymboltoGet.setText("AAPL");
            onBacktest(new ActionEvent());
            

        });

    }

    protected void addInfo() {

        lblChecking.setText("");
        lvDataDisplay.getItems().addAll("");
        lvAccTypes.getItems().addAll(
                "Account ID", "Portfolio Value", "Account Created", "Account Status",
                "Account Cash", "Buying Power", "Day Trade Count", "Day Trade Limit",
                "Equity", "Initial Margin", "Last Equity", "Last Maintenance Margin");
        lvAccValues.getItems().addAll(ac.getAccID(), ac.getPortValue(),
                ac.getCreateDate(), ac.getAccStatus(),
                ac.getAccCash(), ac.getBuyingPower(),
                ac.getDayTradeCount(), ac.getDayTradeLimit(),
                ac.getEquity(), ac.getInitialMargin(),
                ac.getLastEquity(), ac.getLastMaintenanceMargin(),
                "");

        ObservableList<String> periods = FXCollections.observableArrayList("Min", "Hour", "Daily", "Weekly", "Month",
                "Year");
        cbPd.setItems(periods);
        ObservableList<String> classes = FXCollections.observableArrayList("Stocks", "Crypto","Options","Futures","Forex");
        cbAssetClass.setItems(classes);


        Platform.runLater(() -> {populateWatchlist(watchlist);});
        

    }
public void populateWatchlist(List<String> watchlist) {
    // Initialize root node with an empty list of objects (no initial data)
    final TreeItem<List<Object>> root = new TreeItem<>(new ArrayList<>(Arrays.asList("Assets", "", "", "")));
    root.setExpanded(true); // Expand the root node by default
    tbWatchlist.setShowRoot(false); // Hide the root node in the TreeTableView
    tbWatchlist.setRoot(root);

    // Set up the cell value factories for each column
    colAssets.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(0).toString())); // Asset symbol (String)

    colAssetAsk.setCellValueFactory(param -> {
        Object value = param.getValue().getValue().get(1);
        return value instanceof Number ? new ReadOnlyObjectWrapper<Number>((Number) value) : null;
    });

    colAssetBid.setCellValueFactory(param -> {
        Object value = param.getValue().getValue().get(2);
        return value instanceof Number ? new ReadOnlyObjectWrapper<Number>((Number) value) : null;
    });

    colDaily.setCellValueFactory(param -> {
        Object value = param.getValue().getValue().get(3);
        return value instanceof Number ? new ReadOnlyObjectWrapper<Number>((Number) value) : null;
    });

    colAssetBidVol.setCellValueFactory(param -> {
        Object value = param.getValue().getValue().get(4);
        return value instanceof Number ? new ReadOnlyObjectWrapper<Number>((Number) value) : null;
    });

    colAssetAskVol.setCellValueFactory(param -> {
        Object value = param.getValue().getValue().get(5);
        return value instanceof Number ? new ReadOnlyObjectWrapper<Number>((Number) value) : null;
    });

    // Fetch the stock snapshots asynchronously
    Map<String, StockSnapshot> snapResp = ac.stockH.getStockSnapshots(String.join(",", watchlist)).join();

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // Iterate over each symbol in snapResp
    for (Map.Entry<String, StockSnapshot> entry : snapResp.entrySet()) {
        String symbol = entry.getKey();
        StockSnapshot snapshot = entry.getValue();

        // Get the StockQuote for bid/ask/bidVol/askVol
        jai.addToRate();
        StockQuote quote = snapshot.getLatestQuote();
        if (quote != null) {
            double bid = quote.getBp();
            double ask = quote.getAp();
            int bidVol = quote.getBs();
            int askVol = quote.getAs();

            // Calculate the daily percentage change
            double dailyPercentage = 0;
            StockBar dailyBar = snapshot.getDailyBar();
            StockTrade latestT = snapshot.getLatestTrade();
            if (latestT != null && dailyBar != null && dailyBar.getO() != 0) {
                dailyPercentage = (dailyBar.getC() - latestT.getP()) / dailyBar.getO() * 100;
            }

            // Create the row with asset data: symbol, bid, ask, daily%, bidVol, askVol
            List<Object> row = Arrays.asList(symbol, ask, bid, dailyPercentage, bidVol, askVol);

            // Create a new TreeItem for the row
            TreeItem<List<Object>> treeItem = new TreeItem<>(row);

            // Add the new TreeItem to the TreeView in a thread-safe way
            Platform.runLater(() -> {
                root.getChildren().add(treeItem); // Add directly to the root
            });
        } else {
            System.err.println("No quote data available for symbol: " + symbol);
        }
    }

    // Wait for all futures to complete (if necessary)
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                System.out.println("All asset data fetched and added to the watchlist.");
            })
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
}
    

    protected void provideListeners() {
        cbPd.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> obs, String oldp, String newp) {
                // TODO Auto-generated method stub
                switch (newp) {
                    case "Min":
                        selectedPeriod.set("Min");
                        break;
                    case "Hour":
                        selectedPeriod.set("Hour");
                        break;
                    case "Daily":
                        selectedPeriod.set("Day");
                        break;
                    case "Weekly":
                        selectedPeriod.set("Week");
                        break;
                    case "Month":
                        selectedPeriod.set("M");
                        break;
                    case "Year":
                        selectedPeriod.set("Y");
                        break;
                }

            }
        });
        tfDur.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> obs, String old, String newv) {
                // TODO Auto-generated method stub
                System.out.println(newv);
                selectedDuration.set(Integer.parseInt(newv));

            }

        });
        // Add listener to selectedDuration
        selectedDuration.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                // Update the label with the new value
                lblTimeFrameState.setText("Current - " + selectedDuration.getValue() + selectedPeriod.getValue());
            }
        });

        selectedPeriod.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
                // Update the label with the new value
                lblTimeFrameState.setText("Current - " + selectedDuration.getValue() + selectedPeriod.getValue());
            }
        });
        lblTimeFrameState.setText("Current - " + selectedDuration.getValue() + selectedPeriod.getValue());
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
    void onMaximize(MouseEvent event) {
        Platform.runLater(() -> {
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            if (vbDash.getPrefWidth() == 1044 && vbDash.getPrefHeight() == 702) {
                // Set vbDash to fullscreen
                vbDash.setPrefWidth(bounds.getWidth());
                vbDash.setPrefHeight(bounds.getHeight());
                vbDash.setMinSize(bounds.getWidth(), bounds.getHeight());
                vbDash.setMaxSize(bounds.getWidth(), bounds.getHeight());

                // Set anchors to make vbDash fill the entire window
                AnchorPane.setTopAnchor(vbDash, 0.0);
                AnchorPane.setBottomAnchor(vbDash, 0.0);
                AnchorPane.setLeftAnchor(vbDash, 0.0);
                AnchorPane.setRightAnchor(vbDash, 0.0);

                this.mainWindow.setFullScreen(true);

                vbDash.getScene().setOnDragDetected(event1 -> event1.consume());
            } else if (vbDash.getPrefWidth() == bounds.getWidth() && vbDash.getPrefHeight() == bounds.getHeight()) {
                this.mainWindow.setFullScreen(false);
                // Reset vbDash to default size
                vbDash.setPrefWidth(1044);
                vbDash.setPrefHeight(702);
                vbDash.setMinSize(1044, 702);
                vbDash.setMaxSize(1044, 702);
                vbDash.getScene().setOnDragDetected(null);

                this.mainWindow.centerOnScreen();
                // Remove anchors to revert vbDash to its preferred size
                AnchorPane.setTopAnchor(vbDash, null);
                AnchorPane.setBottomAnchor(vbDash, null);
                AnchorPane.setLeftAnchor(vbDash, null);
                AnchorPane.setRightAnchor(vbDash, null);

            }

            // Request layout update
            vbDash.getScene().getWindow().sizeToScene(); // Optional: Force the window to resize
            vbDash.requestLayout(); // Force a layout update
        });
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
        streamListener.listenToStock(Set.of(tfSymboltoGet.getText()));
    }

    public void updateTokenDisplay(String text) {
        Platform.runLater(() -> {
            lvDataDisplay.getItems().addAll(text);
        });
    }

    @FXML
    void onGetCrypto() {
        streamListener.listenToCoin(Set.of(tfSymboltoGet.getText()));
    }

    private ObservableList<OHLCData> getUserData(ActionEvent event)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {

        /*
         * Debugging statements
         * JATbot.botLogger.info("Start Date: " + startParse+"\nEnd Date: " + endParse+
         * "\nStart Year: " + startYear+"\nStart Month: " + startMonth+"\nStart Day: " +
         * startDay +
         * "\nEnd Year: " + endYear+"\nEnd Month: " + endMonth+"\nEnd Day: " + endDay);
         */
        // Retrieve new data series

        return ac.stockH.getBarsDataAsync(tfSymboltoGet.getText(), selectedTimePeriod,jai.getCount()).join();
    }

    // Sets the text of the button
    @FXML
    void setText(ActionEvent event) {
        String sym = tfSymboltoGet.getText();

        try {

            // Clear existing chart from nodeChart if it exists
            if (chart != null) {
                nodeChart.getChildren().remove(chart);
            }

            ObservableList<OHLCData> series = getUserData(event);

            // Create and show new OHLC chart
            this.ph.showOHLCChart(this.parentNode, this.nodeChart, true, 0, series);
            this.chart = ph.getOHLCChart();
            this.chart.setTitle(sym);

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
        // System.out.println("\nInside updateMarrkeTimeLabel.");
        Clock clock = ac.getMarketTime();
        String formattedTime = formatMarketTime(clock); // Format the market time as needed
        Platform.runLater(() -> {
            lblMarketTime.setText(formattedTime);
            if (clock.getIsOpen() == true) {
                lblMarketIndicator1.setText("OPEN");
                lblMarketIndicator1.setTextFill(Color.GREEN);
                lblTimeStatus.setText("Closes at: " + formattoHHMM(clock.getNextClose()));
            } else if (clock.getIsOpen() == false) {
                lblMarketIndicator1.setText("CLOSED");
                lblMarketIndicator1.setTextFill(Color.RED);
                lblTimeStatus.setText("Opens at: " + formattoHHMM(clock.getNextOpen()));
            }
        }); // Update label on JavaFX Application Thread

    }

    // Method to format the market time as needed
    private String formatMarketTime(Clock clock) {
        // System.out.println("\n\nInside formatter....");
        OffsetDateTime cur = clock.getTimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = formatter.format(cur);
        // System.out.println("\n\n\nReceived and formatted to: " + formattedTime);
        return formattedTime;
    }

    private String formattoHHMM(OffsetDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = formatter.format(time);
        return formattedTime;
    }

    // Other code...

    // Method to start updating the market time label periodically
    public void startMarketTimeUpdate() {
        // System.out.println("\nStarting market time update.");
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

    @FXML

    public void onSell(ActionEvent event) {

    }

    @FXML

    public void onBuy(ActionEvent event) {

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

    public void animateBackgroundColor(boolean isCryptoSelected) {
        // Find the node with the id nodeToggle
        AnchorPane nodeToggle = (AnchorPane) vbDash.lookup("#nodeToggle");

        // Define initial and final colors and radii based on stream choice
        Color initialColor;
        Color finalColor;
        CornerRadii initialRadii;
        CornerRadii finalRadii;

        if (isCryptoSelected) {
            // Transition to Crypto state
            initialColor = Color.web("#f6f6f492");
            finalColor = Color.web("#331872");
            initialRadii = new CornerRadii(10, 15, 6, 10, false);
            finalRadii = new CornerRadii(15, 10, 10, 6, false);
        } else {
            // Transition to Stocks state
            initialColor = Color.web("#331872");
            finalColor = Color.web("#f6f6f492");
            initialRadii = new CornerRadii(15, 10, 10, 6, false);
            finalRadii = new CornerRadii(10, 15, 6, 10, false);
        }

        // Create initial and final BackgroundFills
        BackgroundFill startFill = new BackgroundFill(initialColor, initialRadii, Insets.EMPTY);
        BackgroundFill endFill = new BackgroundFill(finalColor, finalRadii, Insets.EMPTY);

        // Create a timeline for color and radius animation
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(nodeToggle.backgroundProperty(), new Background(startFill))),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(nodeToggle.backgroundProperty(), new Background(endFill))));

        timeline.setAutoReverse(false); // Set to false for a one-way transition
        timeline.setCycleCount(1); // Play only once

        timeline.play();
    }

    @FXML
    public void onToggleStream(ActionEvent event) {
        ToggleButton selectedButton = (ToggleButton) event.getSource();
        String buttonId = selectedButton.getId();

        switch (buttonId) {
            case "tbtnToggleStream":
                if (selectedButton.isSelected()) {
                    animateBackgroundColor(true); // Transition to Crypto state
                    selectedButton.setText("Crypto");
                    this.streamChoice = "Crypto";
                } else {
                    animateBackgroundColor(false); // Transition to Stocks state
                    selectedButton.setText("Stocks");
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
                selectedPeriod .set("Day");
                selectedDuration.set(1);

                break;
            case "tbtnDef1MIN":
                selectedPeriod .set("Min");
                selectedDuration.set(1);
                break;
            case "tbtnDef1MON":
                selectedPeriod .set("M");
                selectedDuration.set(1);
                break;
            case "tbtnDef1W":
                selectedPeriod .set("Week");
                selectedDuration.set(1);
                break;
            case "tbtnDef1Y":
                selectedPeriod .set("M");
                selectedDuration.set(12);
                break;
            case "tbtnDef4H":
                selectedPeriod .set("Hour");
                selectedDuration.set(4);
                break;
            case "tbtnDef4MON":
                selectedPeriod .set("M");
                selectedDuration.set(4);
                break;
            case "tbtnHourly":
                selectedPeriod .set("Hour");
                selectedDuration.set(1);
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
public void onBacktest(ActionEvent event) {
    String sym = tfSymboltoGet.getText();

    CompletableFuture.supplyAsync(() -> {
        try {
            return getUserData(event);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    })
    .thenCompose(series -> CompletableFuture.supplyAsync(() -> {
        // Perform backtesting logic here
        if (series != null) {

        }
        return series;
    }))
    .thenCompose(series -> {
        // Run the backtest after data processing
        return CompletableFuture.runAsync(() -> {
            try {

                if (series != null) {
                    for (OHLCData data : series) {
                        System.out.println(data);
                    }
                    Backtesting bt = new Backtesting(ac, 500);
                    bt.run(sym, series);

                    String[] results = bt.passResults();
                    Platform.runLater(() -> {

                        try {
                            this.ph.showOHLCChart(this.parentNode, this.nodeChart, true, 1000, series);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        this.chart = ph.getOHLCChart();
                        this.chart.setTitle(sym);
    
                        ObservableList<String> current = FXCollections.observableArrayList();
                        current.addAll(results);
                        lvDataDisplay.getItems().clear();
                        lvDataDisplay.getItems().addAll(current);
                    });

                } else {
                    System.out.println("No data available for backtesting.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    })
    .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
    });
}
    @FXML
    public void onExit(MouseEvent event) {
        Platform.exit();
        System.exit(0);

    }
}