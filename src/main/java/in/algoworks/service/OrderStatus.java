package in.algoworks.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

// Supported order tracking statuses and the transition rules between them.
public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Delivered and Cancelled orders can no longer change.
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    // Orders only move forward; they can be cancelled until they are shipped.
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null || next == this || isTerminal()) {
            return false;
        }

        if (next == CANCELLED) {
            return ordinal() <= PROCESSING.ordinal();
        }

        return next.ordinal() > ordinal();
    }

    // Case-insensitive lookup that also accepts OUT_FOR_DELIVERY style values.
    public static Optional<OrderStatus> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().replaceAll("[_\\-\\s]+", " ")
                .toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(status -> status.label.toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public static String allowedLabels() {
        List<String> labels = Arrays.stream(values())
                .map(OrderStatus::getLabel)
                .collect(Collectors.toList());
        return String.join(", ", labels);
    }
}
