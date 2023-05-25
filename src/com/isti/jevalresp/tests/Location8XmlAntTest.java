package com.isti.jevalresp.tests;

import com.isti.jevalresp.Run;
import org.junit.Test;

import java.io.IOException;

import static java.lang.String.format;


/**
 * So, the problem seems to be related to running in a directory and identifying
 * files there.
 */
public class Location8XmlAntTest extends TestSupport {

    /**
     * Run the test and check the results - called either via "ant test" or from the
     * command line.
     *
     * @param output The directory where output will be placed.
     * @throws IOException
     */
    private void run(String output) throws IOException {
        copyFile(output, "RESP.IU.ANMO.00.BHZ");
        copyFile(output, "RESP.IU.ANMO.10.BHZ");
        copyFile(output, "RESP.IU.ANMO..BHZ");
        copyFile(output, "RESP.US.DGMT..BHZ");
        copyFile(output, "RESP.UW.ALST.ELE");
        copyFile(output, "RESP.UW.ALST..ENE");
        copyFile(output, "RESP.UW.PRES.ELE");
        copyFile(output, "RESP.XX.RUB03.01.BHE");
        copyFile(output, "RESP.XX.RUB03.01.BHN");
        copyFile(output, "RESP.XX.RUB03.01.BHZ");
        copyFile(output, "RESP.XX.RUB03.02.EPE");
        copyFile(output, "RESP.XX.RUB03.02.EPN");
        copyFile(output, "RESP.XX.RUB03.02.EPZ");
        System.setProperty("SEEDRESP", output);  // directory used for reading files
        new Run(new String[]{"-o", output, "-l", "??", "-n", "IU", "-m",
                "ANMO", "BHZ", "2015", "*", "1", "100", "100"});
        checkFile(output, "AMP.IU.ANMO.10.BHZ.2000.363");
        checkFile(output, "PHASE.IU.ANMO.10.BHZ.2000.363");
        checkFile(output, "AMP.IU.ANMO.00.BHZ.2000.293.16");
        checkFile(output, "PHASE.IU.ANMO.00.BHZ.2000.293.16");
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
        new Location8XmlAntTest().run("/tmp");
    }

}
