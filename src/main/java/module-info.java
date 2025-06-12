module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens bomberman to javafx.fxml;
    exports bomberman;
}