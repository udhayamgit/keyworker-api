package uk.gov.justice.digital.hmpps.keyworker.model;

import java.util.HashMap;
import java.util.Map;

public enum AllocationReason {
    AUTO("AUTO"),
    MANUAL("MANUAL");

    private final String reasonCode;

    AllocationReason(final String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    @Override
    public String toString() {
        return reasonCode;
    }

    // Reverse lookup
    private static final Map<String, AllocationReason> lookup = new HashMap<>();

    static {
        for (final var reason : AllocationReason.values()) {
            lookup.put(reason.reasonCode, reason);
        }
    }

    public static AllocationReason get(final String reasonCode) {
        return lookup.get(reasonCode);
    }
}
