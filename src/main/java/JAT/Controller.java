package JAT;
import java.io.File;


import java.io.FileOutputStream;
import java.io.IOException;

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
import java.nio.file.Path;


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
    Path config = ac.jatConfigPath;

    private double yOffset;
    private double xOffset;
    @FXML
    void initialize() {
        // Load the state of the checkbox when the program starts
        try {
            String[] propertiesArray = ac.loadProperties();
            boolean checked = Boolean.parseBoolean(propertiesArray[4]);
            JATbot.botLogger.info("Remember me: " + checked);
            chkRemember.setSelected(checked);
            rememberMe = checked; // Update the rememberMe variable
            if (checked) {
                tfKey_ID.setText(propertiesArray[0]);
                tfSec_ID.setText(propertiesArray[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    @FXML
    void onClose(ActionEvent event) {
        // Save the state of the checkbox when the program is closed
        try {
            // Modify the specific property using the existing method
            ac.modifyProperty(config, "remMe", String.valueOf(chkRemember.isSelected()));
        } catch (Exception e) {
            JATbot.botLogger.error("Error updating rememberMe property on close: " + e.getMessage());
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
                String[] propertiesArray = ac.loadProperties();
                tfKey_ID.setText(propertiesArray[0]);
                tfSec_ID.setText(propertiesArray[1]);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JAT/dashscene.fxml"));
                Parent root = loader.load();
                DashController dashController = loader.getController();
                dashController.setMainWindow(mainWindow);
                Scene scene = new Scene(root);
                scene.setFill(null);    
                mainWindow.setScene(scene);
                mainWindow.setResizable(true);
                mainWindow.centerOnScreen();
    
            } catch (IOException e) {
                JATbot.botLogger.error("Login properties wrong...\n\nPrinting stack: \n{}", e.getMessage());
            }
        } else {
            try {
                String keyID = tfKey_ID.getText();
                String secretKey = tfSec_ID.getText();
    
                Properties props = new Properties();
                props.setProperty("key_id", keyID);
                props.setProperty("secret_key", secretKey);
    
                ac.writeProps(config, props);
    
                loadscene();
    
            } catch (IOException e) {
                JATbot.botLogger.error("Invalid credentials...\n\nPrinting stack: \n{}", e.getMessage());
            }
        }
    }
    
    @FXML
    void onRememberMe(ActionEvent event) {
        try {
            // Modify the specific property
            ac.modifyProperty(config, "remMe", String.valueOf(chkRemember.isSelected()));
        } catch (Exception e) {
            JATbot.botLogger.error("Error updating rememberMe property: " + e.getMessage());
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
    public void loadscene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/JAT/dashscene.fxml"));
        Parent root = loader.load();
        DashController dashController = loader.getController();
        dashController.setMainWindow(mainWindow);
        Scene scene = new Scene(root);
        mainWindow.setScene(scene);
        mainWindow.setResizable(true);
        mainWindow.centerOnScreen();
    }
    
    /*
    * if (alpacaAPI.stockMarketDataStreaming().isValid()) 
    * { stockvalidlabel green text valid else red text invalid }
    * if (alpacaAPI.stockMarketDataStreaming().isConnected())
    * if alpacaAPI {APIlabel text green connected else red disconnected}
    * 
    */
}


