package com.example.bomberman;

public enum CellType {
    EMPTY(0),
    WALL(1),
    DESTRUCTIBLE_WALL(2),
    PLAYER_SPAWN(3),
    PLAYER2_SPAWN(4);

    private final int value;

    CellType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CellType fromValue(int value) {
        for (CellType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return EMPTY;
    }
}