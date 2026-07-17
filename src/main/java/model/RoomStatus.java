package model;

public enum RoomStatus {
    AVAILABLE("Available"),
    BOOKED("Booked"),
    RESERVED("Reserved"),
    MAINTENANCE("Maintenance"),
    CLEANING("Cleaning");

    private final String label;

    RoomStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RoomStatus fromLabel(String label) {
        for (RoomStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown room status: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
