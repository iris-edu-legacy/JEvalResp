package com.isti.jevalresp.tests;

import com.isti.jevalresp.Run;
import org.junit.Test;

import java.io.IOException;

import static java.lang.String.format;


public class Location3XmlAntTest extends TestSupport {

    /**
     * Run the test and check the results - called either via "ant test" or from the
     * command line.
     *
     * @param output The directory where output will be placed.
     * @throws IOException
     */
    private void run(String output) throws IOException {
        String prop = locateFile("irisxml.prop");

        new Run(new String[]{"-p", prop, "-v",  "-o", output,
                "-n", "ZH", "ILSE1", "-l", "  ", "EHE", "2013", "350", "0.0001", "100", "100"});
        checkFile(output, "AMP.ZH.ILSE1..EHE");
        checkFile(output, "PHASE.ZH.ILSE1..EHE");
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
        new Location3XmlAntTest().run("/tmp");
    }

}
