module JAT {

    requires transitive alpaca.java;
    requires transitive okhttp3;
    requires transitive org.slf4j;

    //requires transitive org.jfree.jfreechart;
    requires transitive java.desktop;
    requires java.base;
    requires transitive commons.math3;
    requires transitive ctfxplotsplus;
    requires java.sql;
    

    opens JAT to javafx.fxml;
    exports JAT;
}
