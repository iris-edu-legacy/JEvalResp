package com.isti.jevalresp.tests;

import org.junit.rules.TemporaryFolder;

public class PermanentFolder extends TemporaryFolder {

    @Override
    protected void after() {}

}
