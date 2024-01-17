package JAT;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class JATbot extends Application {
    private static Stage primaryStage;
    // init logger
    public static final Logger botLogger = LoggerFactory.getLogger(JATbot.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    // Ensure that a dedicated folder for JAT exists in the user's home directory
    Path jatDir = Paths.get(System.getProperty("user.home"), "JAT");
    if (!Files.exists(jatDir)) {
        Files.createDirectory(jatDir);
    }

    Properties properties = new Properties();
    File configFile = new File(jatDir.toFile(), "JATconfig.properties");
    if (configFile.exists()) {
        try (FileInputStream in = new FileInputStream(configFile)) {
            properties.load(in);
        }
    }

    String jarPath = properties.getProperty("jarPath");
    if (jarPath == null || jarPath.isEmpty()) {
        // Get the path of the running jar
        String currentJarPath;
        try {
            currentJarPath = new File(JATbot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not get the path of the running jar", e);
        }

        // Copy the jar to the JAT directory
        Path targetPath = jatDir.resolve(new File(currentJarPath).getName());
        Files.copy(Paths.get(currentJarPath), targetPath);

        // Set the new jar path
        jarPath = targetPath.toString();
        properties.setProperty("jarPath", jarPath);

        try (FileOutputStream out = new FileOutputStream(configFile)) {
            properties.store(out, null);
        }

        // Relaunch the jar
        new ProcessBuilder("java", "-jar", jarPath).start();
        Platform.exit();
    }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainscene.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        controller.setMainWindow(primaryStage);
        primaryStage.setTitle("JATbot");
        Scene scene = new Scene(root);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    public static void setPrimaryStage(Stage theStage) {
        primaryStage = theStage; 
    
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    public static void main(String[] args) {

        try {
            launch(args);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                ((InvocationTargetException) e).getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
        }

          /*AlpacaController.logCreateDate();
          AlpacaController.logPortValue();
          AlpacaController.logStatus();
          AlpacaController.placeTrade();
         /* botLogger.info("This is an informational message.");
         * botLogger.warn("This is a warning message.");
         * botLogger.error("This is an error message.");
         */

    }

}
