package edu.cwru.eecs395_s16.ui;

import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;

/**
 * Created by james on 2/9/16.
 */
public class FunctionDescription {
    public final String name;
    public final String humanName;
    public final String description;
    public final String[] parameters;
    public final boolean mustAuthenticate;
    public final AuthenticationMiddleware invocationPoint;

    public FunctionDescription(String name, String humanName, String description, String[] parameters, boolean
            mustAuthenticate, AuthenticationMiddleware invokationPoint) {
        this.name = name;
        this.humanName = humanName;
        this.description = description;
        this.parameters = parameters;
        this.mustAuthenticate = mustAuthenticate;
        this.invocationPoint = invokationPoint;
    }
}
