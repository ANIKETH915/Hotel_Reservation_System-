package model;

public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    REFUNDED("Refunded"),
    PARTIAL("Partial");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentStatus fromLabel(String label) {
        for (PaymentStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
