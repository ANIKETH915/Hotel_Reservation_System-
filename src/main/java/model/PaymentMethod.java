package model;

public enum PaymentMethod {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentMethod fromLabel(String label) {
        for (PaymentMethod method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
