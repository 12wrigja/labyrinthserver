package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;

/**
 * Created by james on 2/10/16.
 */
public class NoInputRequest implements RequestData {
    @Override
    public void validate() throws InvalidDataException {}
}
