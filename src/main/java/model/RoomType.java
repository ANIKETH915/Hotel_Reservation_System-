package model;

public enum RoomType {
    STANDARD("Standard"),
    DELUXE("Deluxe"),
    SUITE("Suite"),
    LUXURY_SUITE("Luxury Suite"),
    PRESIDENTIAL_SUITE("Presidential Suite");

    private final String label;

    RoomType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RoomType fromLabel(String label) {
        for (RoomType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown room type: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
