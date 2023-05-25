package com.isti.jevalresp.tests;

import com.isti.jevalresp.Run;
import org.junit.Test;

import java.io.IOException;

import static java.lang.String.format;


/**
 * More location tests - these use local files.
 */
public class Location6XmlAntTest extends TestSupport {

    /**
     * Run the test and check the results - called either via "ant test" or from the
     * command line.
     *
     * @param output The directory where output will be placed.
     * @throws IOException
     */
    private void run(String output) throws IOException {
        String seed = locateFile("RESP.IU.ANMO..BHZ");
        new Run(new String[]{"-o", output, "-l", "??", "-n", "IU", "-m", "-f", seed,
                "ANMO", "BHZ", "1990", "*", "1", "100", "100"});
        checkFile(output, "AMP.IU.ANMO..BHZ.1989.241");
        checkFile(output, "PHASE.IU.ANMO..BHZ.1989.241");
    }

    /**
     * Run the test automatically (via @test / "ant test").
     *
     * @throws IOException
     */
    @Test
    public void testFile() throws IOException {
        String output = tmp.getRoot().getAbsolutePath();
        System.out.println(format("Output to %s", output));
        run(output);
    }

    /**
     * This test can be run from the command line, but you must include the irisxml.prop
     * and known good files on the classpath, so that they can be found automatically.
     * Output will be placed in /tmp.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new Location6XmlAntTest().run("/tmp");
    }

}
