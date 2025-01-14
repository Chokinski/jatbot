module JAT {

    requires transitive alpaca.java;
    requires transitive okhttp3;
    requires transitive org.slf4j;
    requires transitive javafx.fxml;
    //requires transitive org.jfree.jfreechart;
    requires transitive java.desktop;
    requires java.base;
    requires transitive commons.math3;
    requires com.jat.ctfxplotsplus;
    requires java.sql;
    requires transitive org.json;
    

    opens JAT to javafx.fxml;
    exports JAT;
}
