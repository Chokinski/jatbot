package JAT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.stage.Stage;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.properties.DataAPIType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The DashController class is responsible for controlling the dashboard view of the application.
 * It handles the initialization of UI elements, loading and saving properties, connecting to the Alpaca API,
 * and managing the stream listener.
 */
public class DashController {

    @FXML
    private VBox vbDash;
    
    @FXML
    private AnchorPane gradientSeparator;

    @FXML
    private MenuButton chart;

    @FXML
    private Label lblAPIstatus;
    @FXML
    private Label lblCryptoStreamStatus;
    @FXML
    private Label lblStockStreamStatus;
    @FXML
    private Label lblChecking;

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



    private AlpacaController ac;

    @FXML
    public void initialize() {
        // initialize Controller
        this.ac = new AlpacaController();

    
        this.streamListener = new StreamListener();
        startGradientAnimation();
    // Add the strings to the ListView when the scene is loaded
    lblChecking.setText("");
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
            "")
            
            ;
    }
    private StreamListener streamListener;

    private double xOffset;
    private double yOffset;

    @FXML
    public void onClose() {

        streamListener.disconnectAlpacaAPI();
        
    }
    
    @FXML
    void onBtnRetryLogin(ActionEvent event){

    String keyID = tfKey_ID.getText();
    String secretKey = tfSec_ID.getText();
    //connect to Alpaca
    ac.connect();
    this.onReconnect(event);

    }

    @FXML
    void onReconnect(ActionEvent event) {

        streamListener.disconnectStream();
        streamListener.connectCryptoStream();
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
        final String[] dots = new String[] {"", ".", "..", "..."};
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
                lblAPIstatus.setText("Connected");
                lblAPIstatus.setTextFill(Color.GREEN);
            } else {
                lblAPIstatus.setText("Disconnected");
                lblAPIstatus.setTextFill(Color.RED);
            }
    
            if (streamListener.areStreamsConnected() == null) {
                lblStockStreamStatus.setText("Disconnected");
                lblStockStreamStatus.setTextFill(Color.RED);
                lblCryptoStreamStatus.setText("Disconnected");
                lblCryptoStreamStatus.setTextFill(Color.RED);
            }
            else {
                if (streamListener.areStreamsConnected()[0]) {
                    lblStockStreamStatus.setText("Connected");
                    lblStockStreamStatus.setTextFill(Color.GREEN);
                } else {
                    lblStockStreamStatus.setText("Disconnected");
                    lblStockStreamStatus.setTextFill(Color.RED);
                }
    
                if (streamListener.areStreamsConnected()[1]) {
                    lblCryptoStreamStatus.setText("Connected");
                    lblCryptoStreamStatus.setTextFill(Color.GREEN);
                } else {
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
    void setText(ActionEvent event) {
        lvDataDisplay.getItems().addAll("Hello World!");
    }


        private Stage mainWindow;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void startGradientAnimation() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(gradientSeparator.backgroundProperty(), new Background(new BackgroundFill(LinearGradient.valueOf("to bottom, #2E3436 0%, #F6F6F4 50%, #2E3436 100%"), CornerRadii.EMPTY, Insets.EMPTY)))),
            new KeyFrame(Duration.millis(500), new KeyValue(gradientSeparator.backgroundProperty(), new Background(new BackgroundFill(LinearGradient.valueOf("to bottom, #F6F6F4 0%, #2E3436 50%, #F6F6F4 100%"), CornerRadii.EMPTY, Insets.EMPTY))))
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }
    
}
