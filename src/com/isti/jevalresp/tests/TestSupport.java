package com.isti.jevalresp.tests;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.net.URL;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static junit.framework.TestCase.assertEquals;


public abstract class TestSupport {

    /** This will be automatically deleted on exit. */
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
//    public TemporaryFolder tmp = new PermanentFolder();    // swap to save output

    /**
     * Find a file on the classpath.
     *
     * @param name A file to locate from the classpath.
     * @return The path to the file.
     */
    protected final String locateFile(final String name) {
        URL url = currentThread().getContextClassLoader().getResource(name);
        if (url == null) throw new RuntimeException(format("Could not find %s on classpath", name));
        String path = url.getFile();
        out.println(format("Found %s", path));
        return path;
    }

    /**
     * Check the output file (in dir) against the known good value on the classpath.
     *
     * @param dir Output directory.
     * @param name Name of output file (both in output dir and on classpath).
     * @throws IOException
     */
    protected final void checkFile(final String dir, final String name) throws IOException {
        String expected = locateFile(name);
        BufferedReader in1 = new BufferedReader(new FileReader(expected));
        BufferedReader in2 = new BufferedReader(new FileReader(new File(dir, name)));
        while (true) {
            String line1 = in1.readLine();
            String line2 = in2.readLine();
            assertEquals(format("Bad output for %s", name), line1, line2);
            if (line1 == null && line2 == null) break;
        }
        out.println(format("Checked %s", name));
        in1.close();
        in2.close();
    }

    /**
     * Copy a file to the test directory
     */
    protected final void copyFile(final String dir, final String name) throws IOException {
        String path = locateFile(name);
        BufferedReader from = new BufferedReader(new FileReader(path));
        Writer to = new FileWriter(new File(dir, name));
        while (true) {
            String line = from.readLine();
            if (line == null) break;
            to.write(line);
            to.write("\n");
        }
        to.close();
        from.close();
        out.println(format("Copied %s", name));
    }

}
