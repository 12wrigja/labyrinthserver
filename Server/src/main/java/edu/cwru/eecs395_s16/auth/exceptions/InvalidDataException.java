package edu.cwru.eecs395_s16.auth.exceptions;

import edu.cwru.eecs395_s16.networking.responses.StatusCode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by james on 2/10/16.
 */
public class InvalidDataException extends JsonableException {
    public InvalidDataException(String variableName) {
        super(StatusCode.UNPROCESSABLE_DATA, "The data attribute " + variableName + " is not valid.");
    }

    public InvalidDataException(String... variableNames) {
        super(StatusCode.UNPROCESSABLE_DATA, "The data attributes " + Arrays.toString(variableNames).replaceAll("\\[\\]", "") + " are invalid.");
    }

    public InvalidDataException(List<String> variableNames) {
        this(new String[variableNames.size()]);
    }
}