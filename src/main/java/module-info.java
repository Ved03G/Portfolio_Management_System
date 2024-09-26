module org.example.portfolio_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires json.simple;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.google.gson;
    requires de.jensd.fx.glyphs.fontawesome;

    opens org.example.portfolio_management_system to javafx.fxml;
    exports org.example.portfolio_management_system;
}