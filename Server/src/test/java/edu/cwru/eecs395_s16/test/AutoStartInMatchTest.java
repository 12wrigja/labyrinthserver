package edu.cwru.eecs395_s16.test;

/**
 * Created by james on 3/23/16.
 */
public abstract class AutoStartInMatchTest extends InMatchTest {

    @Override
    public void setup() throws Exception {
        super.setup();
        super.setupMatch();
    }
}
