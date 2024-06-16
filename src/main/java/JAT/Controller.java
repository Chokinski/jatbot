package JAT;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


// This controller belongs to the login scene

public class Controller {    

    @FXML
    private BorderPane bp;
    
    @FXML
    private Button btnLightMode;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnTryLogin;

    @FXML
    private TextField tfKey_ID;

    @FXML
    private PasswordField tfSec_ID;

    @FXML
    private Label streamStatus;

    @FXML
    private Label APIstatus;

   @FXML
    private CheckBox chkRemember;

    AlpacaController ac = new AlpacaController();
    public boolean rememberMe;
    Properties properties = new Properties();
    InputStream jatConfig = getClass().getResourceAsStream("/JAT/JATconfig.properties");
    private double yOffset;
    private double xOffset;
    @FXML
    void initialize() {
        // Load the state of the checkbox when the program starts
        try {
            properties.load(jatConfig);
            boolean checked = Boolean.parseBoolean(properties.getProperty("rememberMe"));
            chkRemember.setSelected(checked);
            rememberMe = checked; // Update the rememberMe variable
            if (checked) {
                // Load the keys from the alpaca.properties file
                Properties alpacaProperties = new Properties();
                try (InputStream alpacaIn = getClass().getResourceAsStream("/JAT/alpaca.properties")) {
                    alpacaProperties.load(alpacaIn);
                }
    
                tfKey_ID.setText(alpacaProperties.getProperty("key_id"));
                tfSec_ID.setText(alpacaProperties.getProperty("secret_key"));
            }
        } catch (IOException e) {e.printStackTrace();}


    }
    
    @FXML
    void onClose(ActionEvent event) {
        // Save the state of the checkbox when the program is closed
        try (FileOutputStream out = new FileOutputStream(new File("src/main/resources/JAT/JATconfig.properties"))) {
            properties.setProperty("rememberMe", String.valueOf(chkRemember.isSelected()));
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    
    void btnLightSwitch(ActionEvent event) {
        
        double opacity = mainWindow.getOpacity() == 1.0 ? 0.2 : 1.0;
        mainWindow.setOpacity(opacity);
        String btnText = btnLightMode.getText().equals("Light") ? "Dark" : "Light";
        btnLightMode.setText(btnText);

    }
    @FXML
    void onBtnLogin(ActionEvent event) {
        if (rememberMe) {
            try {
                // Load the properties file
                tfKey_ID.setText(ac.loadProperties()[0]);
                tfSec_ID.setText(ac.loadProperties()[1]);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashscene.fxml"));
                Parent root = loader.load();
                DashController dashController = loader.getController();
                dashController.setMainWindow(mainWindow);
                Scene scene = new Scene(root);
                scene.setFill(null);    
                mainWindow.setScene(scene);
            } catch (IOException e) {
                JATbot.botLogger.error("Login Failed.. Printing stack: \n{}",e.getMessage());
            }
        } else {
            try {
                String keyID = tfKey_ID.getText();
                String secretKey = tfSec_ID.getText();

                // Load the properties file
                InputStream in = getClass().getResourceAsStream("/JAT/alpaca.properties");
                properties.load(in);
                in.close();

                // Set the new values
                properties.setProperty("key_id", keyID);
                properties.setProperty("secret_key", secretKey);

                // Save the properties file
                try (FileOutputStream out = new FileOutputStream(new File("src/main/resources/JAT/alpaca.properties"))) {
                    properties.store(out, null);
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashscene.fxml"));
                Parent root = loader.load();
                DashController dashController = loader.getController();
                dashController.setMainWindow(mainWindow);
                Scene scene = new Scene(root);
                mainWindow.setScene(scene);
            } catch (IOException e) {
                JATbot.botLogger.error("Login Failed.. Printing stack: \n{}",e.getMessage());
            }
        }
    }

    @FXML
    void onRememberMe(ActionEvent event) {
        try {
            // Load the properties file
            InputStream in = getClass().getResourceAsStream("/JAT/JATconfig.properties");
            properties.load(in);

            // Set the new value
            properties.setProperty("rememberMe", String.valueOf(chkRemember.isSelected()));

            // Save the properties file
            try (FileOutputStream out = new FileOutputStream(new File("src/main/resources/JAT/JATconfig.properties"))) {
                properties.store(out, null);
            }
        } catch (IOException e) {
            JATbot.botLogger.error("Login Failed.. Printing stack: \n{}",e.getMessage());
        }
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
    
    private Stage mainWindow;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    /*
     * if (alpacaAPI.stockMarketDataStreaming().isValid()) 
     * { stockvalidlabel green text valid else red text invalid }
     * if (alpacaAPI.stockMarketDataStreaming().isConnected())
     * if alpacaAPI {APIlabel text green connected else red disconnected}
     * 
     */
}


