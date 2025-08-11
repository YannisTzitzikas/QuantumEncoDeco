package com.csd.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        if (message != null && !message.isEmpty()) {
            errors.add(message);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public String toString() {
        return isValid() ? "ValidationResult{OK}" : "ValidationResult{errors=" + errors + "}";
    }
}
