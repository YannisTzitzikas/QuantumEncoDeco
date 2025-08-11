package com.csd.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates validation results, including accumulated error and warning
 * messages.
 */
public final class ValidationResult {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /** Adds an error message */
    public ValidationResult addError(String message) {
        if (message != null && !message.isEmpty()) {
            errors.add(message);
        }
        return this;
    }

    /** Adds a warning message */
    public ValidationResult addWarning(String message) {
        if (message != null && !message.isEmpty()) {
            warnings.add(message);
        }
        return this;
    }

    /** Returns true if no errors were added */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /** Returns an unmodifiable list of errors */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /** Returns an unmodifiable list of warnings */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /** Merges another ValidationResult’s errors and warnings into this one */
    public ValidationResult merge(ValidationResult other) {
        if (other != null) {
            errors.addAll(other.errors);
            warnings.addAll(other.warnings);
        }
        return this;
    }

    public String warnToString() {
        StringBuilder sb = new StringBuilder("");

        if (!warnings.isEmpty()) {
            sb.append("Warnings:\n");
            for (String warning : warnings) {
                sb.append("\t- ").append(warning).append('\n');
            }
        }

        return sb.toString();
    }

    public String errorsToString() {
        StringBuilder sb = new StringBuilder("");
        
        if (isValid()) sb.append("\tOK\n");
        else
        {
            sb.append("Errors:\n");
            for (String error : errors) {
                sb.append("\t- ").append(error).append('\n');
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationResult{\n");

        sb.append("\t").append(errorsToString());
        sb.append("\t").append(warnToString());
        sb.append('}');

        return sb.toString();
    }

}
