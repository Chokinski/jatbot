package com.jat.jatbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

@SpringBootApplication
public class JATbot extends Application {
    private static ApplicationContext springContext;
    private static Stage primaryStage;
    // init logger
    public static final Logger botLogger = LoggerFactory.getLogger(JATbot.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure that a dedicated folder for JAT exists in the user's home directory
        ensureProps();
        initUI(primaryStage);


    }

    private void initUI(Stage primaryStage) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jat/jatbot/mainscene.fxml"));
        Parent root = loader.load();
        loader.setControllerFactory(springContext::getBean);
        Controller controller = loader.getController();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        controller.setMainWindow(primaryStage);
        primaryStage.setTitle("JATbot");
        Scene scene = new Scene(root);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        setPrimaryStage(primaryStage);


    }
    private void ensureProps() throws IOException{

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


    }
    public static void setPrimaryStage(Stage theStage) {
        primaryStage = theStage;

    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        springContext = SpringApplication.run(JATbot.class, args);

        try {
            launch(args);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                ((InvocationTargetException) e).getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
        }

    }
    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> botLogger.info("JATbot is running...");
    }
}
