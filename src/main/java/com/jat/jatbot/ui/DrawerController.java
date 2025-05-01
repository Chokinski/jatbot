package com.jat.jatbot.ui;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.nd4j.linalg.cpu.nativecpu.bindings.Nd4jCpu.pad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jat.ctfxplotsplus.OHLCData;
import com.jat.jatbot.ai.StockData;
import com.jat.jatbot.ai.robot;
import com.jat.jatbot.alpaca.AlpacaAssetHandler;
import com.jat.jatbot.alpaca.AlpacaController;
import com.jat.jatbot.alpaca.AlpacaCryptoHandler;
import com.jat.jatbot.alpaca.AlpacaStockHandler;
import com.jat.jatbot.alpaca.StreamListener;
import com.jat.jatbot.datahandlers.JATInfoHandler;
import com.jat.jatbot.datahandlers.OHLCParser;
import com.jat.jatbot.dbhandlers.CsvUploadController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockSnapshot;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import net.jacobpeterson.alpaca.openapi.trader.model.AssetClass;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import net.jacobpeterson.alpaca.openapi.trader.model.Order;
import net.jacobpeterson.alpaca.openapi.trader.model.Position;

@Component
public class DrawerController {

    @FXML
    private GridPane gpCheckBoxes;
    @FXML
    private ChoiceBox<String> cbAssetClass;
    
    @FXML
    private TreeTableView<List<Object>> tbWatchlist;

    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetAsk;
    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetAskVol;

    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetBid;

    @FXML
    private TreeTableColumn<List<Object>, Number> colAssetBidVol;
    
    @FXML
    private TreeTableColumn<List<Object>, String> colAssets;
    
    @FXML
    private TreeTableColumn<List<Object>, Number> colDaily;
    @FXML
    private TreeTableView<List<Object>> tbOrders;
    
    @FXML
    private TreeTableColumn<List<Object>, String> colOrderAsset;
    @FXML
    private TreeTableColumn<List<Object>, String> colOrderTime;
    @FXML
    private TreeTableColumn<List<Object>, String> colOrderStatus;

    @FXML
    private TreeTableColumn<List<Object>, String> colOrderSide;
    @FXML
    private TreeTableColumn<List<Object>, String> colOrderValue;
    @FXML
    private TreeTableColumn<List<Object>, String> colOrderStopPrice;

    @FXML
    private TreeTableColumn<List<Object>, String> colOrderLimitPrice;
    
    @FXML
    private TreeTableView<List<Object>> tbPositions;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosAsset;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosPricePerShare;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosMVal;

    @FXML
    private TreeTableColumn<List<Object>, String> colPosQty;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosSide;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosDailyPL;

    @FXML
    private TreeTableColumn<List<Object>, String> colPosPL;
    @FXML
    private TreeTableColumn<List<Object>, String> colPosPLPerc;

    @FXML
    private JFXTextField tfTimeFrameToGrab;

    @FXML
    private JFXTextField tfSymbolToGrab;
    @FXML
    private Label lblModelStatus;
    @FXML
    private JFXButton btnLoadModel;
    @FXML
    private JFXButton btnLaunchAlgo;
    
    @FXML
    private ListView<String> lvAccTypes;
    @FXML
    private ListView<String> lvAccValues;
    @FXML
    private ListView<String> lvPredictionsValues;
    @FXML
    private JFXCheckBox chkDataStatus;
    @FXML
    private JFXCheckBox chkWriteStatus;
    @FXML
    private JFXCheckBox chkUploadStatus;
    @FXML
    private JFXCheckBox chkPredStatus;
    @FXML
    private JFXCheckBox chkCompareStatus;
    @FXML
    private JFXCheckBox chkHandledOrder;
    

    @FXML
    private VBox vbDash;
    private List<String> watchlist = new ArrayList<>(Arrays.asList("AAPL","TSLA","MSFT","META","GOLD"));
    @Autowired
    public robot robot;
        @Autowired
    public AlpacaController ac;
    @Autowired
    public AlpacaAPI api;
    @Autowired
    public JATInfoHandler jai;
    @Autowired
    public AlpacaStockHandler stockH;
    @Autowired
    public AlpacaAssetHandler assetH;
    @Autowired
    public AlpacaCryptoHandler cryptoH;
    @Autowired
    public CsvUploadController csvUploadController;
    private List<Assets> assets;
    @Autowired
    private StreamListener streamListener;
    @FXML
    public void initialize() {



        Platform.runLater(()->{
        streamListener.giveRobot(robot,ac);
        addInfo();
        lblModelStatus.setTextFill(Color.RED);
        for(String x : watchlist){
            System.out.println(x);
        }
        });
        assets = ac.getAssets();
        startUpdates();
    }

    @FXML
    public void loadModel(ActionEvent event) {
        // Load the model and update the status text field
        lblModelStatus.setText("Loading...");
        lblModelStatus.setTextFill(Color.ORANGE);
        robot.loadModel();
        lblModelStatus.setText("Ready");
        lblModelStatus.setTextFill(Color.GREEN);
        tfTimeFrameToGrab.setVisible(true);
        tfSymbolToGrab.setVisible(true);
        btnLoadModel.setVisible(false);
        btnLaunchAlgo.setVisible(true);
        
        
    }
    public void resetStatesAfterPrediction() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        chkDataStatus.setSelected(false);
        chkWriteStatus.setSelected(false);
        chkUploadStatus.setSelected(false);
        chkPredStatus.setSelected(false);
        chkCompareStatus.setSelected(false);
        chkHandledOrder.setSelected(false);
        gpCheckBoxes.setVisible(false);
        lblModelStatus.setText("Ready");
        lblModelStatus.setTextFill(Color.GREEN);
        tfTimeFrameToGrab.setVisible(true);
        tfSymbolToGrab.setVisible(true);
        btnLaunchAlgo.setVisible(true);
        btnLaunchAlgo.setDisable(false);

    }

    private ScheduledExecutorService executorService;
    private ScheduledExecutorService secondexecutorService;
    private ScheduledExecutorService thirdexecutorService;
    public void startUpdates() {
        // System.out.println("\nStarting market time update.");
        // Create a scheduled executor to periodically update the market time label
        executorService = Executors.newSingleThreadScheduledExecutor();
        secondexecutorService = Executors.newSingleThreadScheduledExecutor();
        thirdexecutorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> Platform.runLater(this::populateWatchlist), 0, 20, TimeUnit.SECONDS);
    secondexecutorService.scheduleAtFixedRate(() -> Platform.runLater(this::populateOrders), 0, 20, TimeUnit.SECONDS);
    thirdexecutorService.scheduleAtFixedRate(() -> Platform.runLater(this::populatePositions), 0, 20, TimeUnit.SECONDS);
    }

    public void stopUpdates() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @FXML
    public void launchAlgo(ActionEvent event) {
        // Launch the algorithm and update the status text field
        Platform.runLater(()->{
        gpCheckBoxes.setVisible(true);
        lblModelStatus.setText("Starting algo...");
        lblModelStatus.setTextFill(Color.YELLOW);
        String timeframe = tfTimeFrameToGrab.getText();
        String symbol = tfSymbolToGrab.getText();
        tfTimeFrameToGrab.setVisible(false);
        tfSymbolToGrab.setVisible(false);
        btnLaunchAlgo.setDisable(true);
        btnLaunchAlgo.setVisible(false);
        try {
            lblModelStatus.setText("Getting data...");
            ObservableList<OHLCData> data = getUserData(event, symbol, timeframe);
            chkDataStatus.setSelected(true);
            
            lblModelStatus.setText("Writing data...");
            String sanitizedSymbol = symbol.replace("/", "");
            OHLCParser.writeResultsUsingStandardOutput(data,sanitizedSymbol);
            chkWriteStatus.setSelected(true);
            lblModelStatus.setText("Uploading data...");
            csvUploadController.uploadDatasetWithSym(sanitizedSymbol);
            chkUploadStatus.setSelected(true);
            lblModelStatus.setText("Getting prediction...");
            Thread.sleep(500);
            StockData prediction = robot.getSinglePrediction(sanitizedSymbol);
            lvPredictionsValues.getItems().addAll("Asset: "+sanitizedSymbol, "Prediction: "+String.valueOf(prediction.getClose()),"Time:  "+prediction.getTimestamp());
            chkPredStatus.setSelected(true);
            lblModelStatus.setText("Comparing data...");
            LocalDateTime predictionTime = jai.parseStringToDateTime(prediction.getTimestamp());
            robot.setCurrentPredPrice(prediction.getClose());
            robot.setCurrentPredDate(predictionTime);
            chkCompareStatus.setSelected(true);
            lblModelStatus.setText("Handling order...");
            if (getAssetClass().toString().equals("crypto")) {
                streamListener.connectCryptoRobotStream();
                Set<String> symbols = new HashSet<>();
                symbols.add(symbol);
                Thread.sleep(5000);
                streamListener.listenToCoinTrades(symbols);
                chkHandledOrder.setSelected(true);
            } else {
                streamListener.connectStockRobotStream();
                Set<String> symbols = new HashSet<>();
                symbols.add(symbol);
                Thread.sleep(5000);
                streamListener.listenToStockTrades(symbols);
                chkHandledOrder.setSelected(true);
            }
            Thread.sleep(500);
            resetStatesAfterPrediction();
        } catch (ApiException | IOException | ExecutionException | InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }});}

    protected void addInfo() {
        lvAccTypes.getItems().addAll(
                "Account ID", "Portfolio Value", "Account Created", "Account Status",
                "Account Cash", "Buying Power", "Day Trade Count", "Day Trade Limit",
                "Equity", "Initial Margin", "Last Equity", "Last Maintenance Margin");
        lvAccValues.getItems().addAll(ac.getAccID(), ac.getPortValue(),
                ac.getCreateDate(), ac.getAccStatus(),
                ac.getAccCash(), ac.getBuyingPower(),
                ac.getDayTradeCount(), ac.getDayTradeLimit(),
                ac.getEquity(), ac.getInitialMargin(),
                ac.getLastEquity(), ac.getLastMaintenanceMargin());
                ObservableList<String> classes = FXCollections.observableArrayList("Stocks", "Crypto","Options","Futures","Forex");
                cbAssetClass.setItems(classes);




        populateWatchlist();
        populateOrders();
        populatePositions();

    }
    public void populateOrders() {
        List<Order> orders = ac.getAllOrders();
        // Initialize root node with an empty list of objects (no initial data)
        final TreeItem<List<Object>> root = new TreeItem<>(new ArrayList<>(Arrays.asList("Assets", "", "", "")));
        root.setExpanded(true); // Expand the root node by default
        tbOrders.setShowRoot(false); // Hide the root node in the TreeTableView
        tbOrders.setRoot(root);
    
        // Set up the cell value factories for each column
        colOrderAsset.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(0).toString())); // Asset symbol (String)
    
        colOrderTime.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(1).toString()));
        colOrderStatus.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(2).toString()));
    
    
        colOrderSide.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(3).toString()));
    
        colOrderValue.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(4).toString()));
    
        colOrderStopPrice.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(5).toString()));
        colOrderLimitPrice.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(6).toString()));
    
        // Iterate over each symbol in snapResp
        for (Order o:orders) {
            String symbol = o.getSymbol();
            String time = o.getCreatedAt()== null ? "N/A" : o.getCreatedAt().toString();
            String status = o.getStatus() == null ? "N/A" : o.getStatus().toString();
            String side = o.getSide().toString();
            String value = o.getNotional() == null ? "Qty "+ o.getQty() : o.getNotional().toString(); 
            String stop = o.getStopPrice() == null ? "N/A" : o.getStopPrice();
            String limit = o.getLimitPrice() == null ? "N/A" : o.getLimitPrice();
            
    
                // Create the row with asset data: symbol, bid, ask, daily%, bidVol, askVol
                List<Object> row = Arrays.asList(symbol, time,status,side,value,stop,limit);
    
                // Create a new TreeItem for the row
                TreeItem<List<Object>> treeItem = new TreeItem<>(row);
    
                // Add the new TreeItem to the TreeView in a thread-safe way
                Platform.runLater(() -> {
                    root.getChildren().add(treeItem); // Add directly to the root
                    colOrderTime.setSortType(TreeTableColumn.SortType.DESCENDING); // Sort in ascending order
    tbOrders.getSortOrder().clear(); // Clear any existing sort order
    tbOrders.getSortOrder().add(colOrderTime); // Add the time column to the sort order
    tbOrders.sort(); // Trigger the sort
                });
            }
        }

        public void populatePositions() {
            List<Position> positions = ac.getAllPositions();
            // Initialize root node with an empty list of objects (no initial data)
            final TreeItem<List<Object>> root = new TreeItem<>(new ArrayList<>(Arrays.asList("Assets", "", "", "")));
            root.setExpanded(true); // Expand the root node by default
            tbPositions.setShowRoot(false); // Hide the root node in the TreeTableView
            tbPositions.setRoot(root);
        
            // Set up the cell value factories for each column
            colPosAsset.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(0).toString())); // Asset symbol (String)
        
            colPosPricePerShare.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(1).toString()));
            colPosMVal.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(2).toString()));
            colPosQty.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(3).toString()));
            colPosSide.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(4).toString()));
            colPosDailyPL.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(5).toString()));
            colPosPL.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(6).toString()));
            colPosPLPerc.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().get(7).toString()));
            
        
        
            // Iterate over each symbol in snapResp
            for (Position p:positions) {
                String symbol = p.getSymbol();
                String pricePerShare = p.getCurrentPrice()== null ? "N/A" : p.getCurrentPrice();
                String marketVal = p.getMarketValue() == null ? "N/A" : p.getMarketValue();
                String qty = p.getQty() == null ? "N/A" : p.getQty();
                String side = p.getSide() == null ? "N/A" : p.getSide(); 
                String dailyPl = p.getUnrealizedIntradayPl() == null ? "N/A" : p.getUnrealizedIntradayPl();
                String ulPL = p.getUnrealizedPl() == null ? "N/A" : p.getUnrealizedPl();
                String ulPLPerc = p.getUnrealizedPlpc() == null ? "N/A" : p.getUnrealizedPlpc();
                
        
                    // Create the row with asset data: symbol, bid, ask, daily%, bidVol, askVol
                    List<Object> row = Arrays.asList(symbol,pricePerShare,marketVal,qty,side,dailyPl,ulPL,ulPLPerc);
        
                    // Create a new TreeItem for the row
                    TreeItem<List<Object>> treeItem = new TreeItem<>(row);
        
                    // Add the new TreeItem to the TreeView in a thread-safe way
                    Platform.runLater(() -> {
                        root.getChildren().add(treeItem); // Add directly to the root
                    });
                }
            }


public void populateWatchlist() {
    watchlist = new ArrayList<>(Arrays.asList("AAPL","TSLA","MSFT","META","GOLD"));
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
    Map<String, StockSnapshot> snapResp = stockH.getStockSnapshots(String.join(",", watchlist)).join();

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // Iterate over each symbol in snapResp
    for (Map.Entry<String, StockSnapshot> entry : snapResp.entrySet()) {
        String symbol = entry.getKey();
        StockSnapshot snapshot = entry.getValue();

        // Get the StockQuote for bid/ask/bidVol/askVol
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
                colDaily.setSortType(TreeTableColumn.SortType.DESCENDING); // Sort in descending order
    tbWatchlist.getSortOrder().clear(); // Clear any existing sort order
    tbWatchlist.getSortOrder().add(colDaily); // Add the daily column to the sort order
    tbWatchlist.sort(); // Trigger the sort
            });
        } else {
            System.err.println("No quote data available for symbol: " + symbol);
        }
    }

}
    private ObservableList<OHLCData> getUserData(ActionEvent event,String symbol, String timeframe)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
                ObservableList<OHLCData> datatoreturn = FXCollections.observableArrayList();
        /*
         * Debugging statements
         * JATbot.botLogger.info("Start Date: " + startParse+"\nEnd Date: " + endParse+
         * "\nStart Year: " + startYear+"\nStart Month: " + startMonth+"\nStart Day: " +
         * startDay +
         * "\nEnd Year: " + endYear+"\nEnd Month: " + endMonth+"\nEnd Day: " + endDay);
         */
        // Retrieve new data series
        AssetClass assetc = getAssetClass();
        //System.out.println(assetc.toString());
        switch(assetc.toString()) 
            {
                case "crypto":
                datatoreturn = cryptoH.getBarsDataAsync(symbol, timeframe,jai.getCount()).join();
                    break;

                case "us_equity":
                datatoreturn = stockH.getBarsDataAsync(symbol, timeframe,jai.getCount()).join();
            }

            return datatoreturn;
    }
        public AssetClass getAssetClass() {

        CompletableFuture<AssetClass> assetClass = 
        CompletableFuture.supplyAsync(()->{
            boolean stop = false;
            for(int i = 0; i<assets.size() && !stop;i++){
                
                Assets a = assets.get(i);
                if(a.getSymbol().toLowerCase().equals(tfSymbolToGrab.getText().toLowerCase())){
                    System.out.println("Found one!");    
                    stop=true;
                    return a.getPropertyClass();
                    
                }
            }
            System.out.println("But returning null...");
            return null;

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
        return assetClass.join();


    }
}
