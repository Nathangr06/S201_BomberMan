module bomberman {
    // Dépendances JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Export du package racine
    exports bomberman;
    opens bomberman to javafx.fxml;

    // MODEL - Packages exportés et ouverts
    exports bomberman.model.entities;
    opens bomberman.model.entities to javafx.fxml;

    exports bomberman.model.game;
    opens bomberman.model.game to javafx.fxml;

    exports bomberman.model.ai;
    opens bomberman.model.ai to javafx.fxml;

    exports bomberman.model.profile;
    opens bomberman.model.profile to javafx.fxml;

    // CONTROLLER - Packages exportés et ouverts
    exports bomberman.controller.menu;
    opens bomberman.controller.menu to javafx.fxml;

    exports bomberman.controller.game;
    opens bomberman.controller.game to javafx.fxml;

    exports bomberman.utils;
    opens bomberman.utils to javafx.fxml;

}