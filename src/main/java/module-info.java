module JAT {

    requires transitive alpaca.java;
    requires transitive okhttp3;
    requires org.slf4j;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    
    //requires transitive org.jfree.jfreechart;
    requires transitive java.desktop;
    requires java.base;
    requires commons.math3;
    requires com.jat.ctfxplotsplus;
    requires java.sql;
    requires transitive org.json;
    

    opens JAT to javafx.fxml;
    exports JAT;
}
