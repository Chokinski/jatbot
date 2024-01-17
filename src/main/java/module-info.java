module JAT {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive alpaca.java;
    requires transitive okhttp3;
    requires transitive org.slf4j;
    requires transitive slf4j.okhttp3.logging.interceptor;
    requires transitive org.jfree.jfreechart;
    requires transitive org.jfree.fxgraphics2d;
    requires transitive java.desktop;

    opens JAT to javafx.fxml;
    exports JAT;
}
