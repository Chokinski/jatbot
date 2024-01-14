module JAT {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive alpaca.java;
    requires transitive okhttp3;
    requires transitive org.slf4j;
    requires transitive slf4j.okhttp3.logging.interceptor;


    opens JAT to javafx.fxml;
    exports JAT;
}
