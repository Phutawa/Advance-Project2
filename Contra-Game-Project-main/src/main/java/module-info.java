module se233.contra {
    requires javafx.controls;
    requires javafx.media;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    opens se233.contra to javafx.fxml;
    opens se233.sprites.character;  // Add this line
    opens se233.sprites.bosses;     // Add this if needed
    opens backgrounds;              // Add this if needed

    exports se233.contra;
    exports se233.contra.controller;
    exports se233.contra.model;
    exports se233.contra.model.entity;
    exports se233.contra.view;
    exports se233.contra.util;
    exports se233.contra.exception;
}