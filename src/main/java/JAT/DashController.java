package JAT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.properties.DataAPIType;


/**
 * The DashController class is responsible for controlling the dashboard view of the application.
 * It handles the initialization of UI elements, loading and saving properties, connecting to the Alpaca API,
 * and managing the stream listener.
 */
public class DashController {

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
    private ListView<String> lvAccTypes;
    @FXML
    private ListView<String> lvAccValues;

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

    // Add the strings to the ListView when the scene is loaded
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
        streamListener.connectStream();
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
        streamListener.connectStream();
    }

    @FXML
    void onCheckStatus(ActionEvent event) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ac.getAccount() != null) {
            lblAPIstatus.setText("Connected");
            lblAPIstatus.setTextFill(Color.GREEN);
        } else {
            lblAPIstatus.setText("Disconnected");
            lblAPIstatus.setTextFill(Color.RED);
        }


        // if streamlistener.areStreamsConnected() == null, then both streams are disconnected

        if (streamListener.areStreamsConnected() == null) {
            lblStockStreamStatus.setText("Disconnected");
            lblStockStreamStatus.setTextFill(Color.RED);
            lblCryptoStreamStatus.setText("Disconnected");
            lblCryptoStreamStatus.setTextFill(Color.RED);
        }
        else {
            if (streamListener.areStreamsConnected()[0] == true) {
                lblStockStreamStatus.setText("Connected");
                lblStockStreamStatus.setTextFill(Color.GREEN);
            } else if (streamListener.areStreamsConnected()[0] == false) {
                lblStockStreamStatus.setText("Disconnected");
                lblStockStreamStatus.setTextFill(Color.RED);
            }

            if (streamListener.areStreamsConnected()[1] == true) {
                lblCryptoStreamStatus.setText("Connected");
                lblCryptoStreamStatus.setTextFill(Color.GREEN);
            } else if (streamListener.areStreamsConnected()[1] == false) {
                lblCryptoStreamStatus.setText("Disconnected");
                lblCryptoStreamStatus.setTextFill(Color.RED);
            }
        }
            

    }
    @FXML
    void onGetStock() {
        streamListener.listenToStock(null);
    }

    @FXML
    void onGetCrypto() {

        streamListener.listenToCoin(null);
    }

}
