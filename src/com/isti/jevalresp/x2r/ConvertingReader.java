package com.isti.jevalresp.x2r;

import edu.iris.dmc.fdsn.station.model.Channel;
import edu.iris.dmc.fdsn.station.model.Network;
import edu.iris.dmc.fdsn.station.model.Station;
import edu.iris.dmc.service.ServiceUtil;
import edu.iris.dmc.service.StationService;
import edu.iris.dmc.ws.util.RespUtil;

import java.io.*;
import java.util.List;

import static java.lang.System.out;


/**
 * A converter from station.xml input stream to response reader.
 */
public class ConvertingReader extends Reader {

    /** The lookahead space for a non-blank character */
    private static int NON_BLANK_SPACE = 1000;

    /** The reader we delegate to (contains translated output). */
    private Reader delegate;

    /** Delayed error, saved and rethrown so that it can be handled by existing logic. */
    private IOException error;

    /**
     * @param input The source of station.xml formatted data.
     */
    public ConvertingReader(final InputStream input) {
        try {
            PipedReader reader = new PipedReader();
            final PipedWriter writer = new PipedWriter(reader);
            final List<Network> networks = getNetworks(input);
            new Thread() {
                @Override
                public void run() {
                    try {
                        RespUtil.write(new PrintWriter(writer), networks);
                    } catch (Exception e) {
                        synchronized (ConvertingReader.this) {
                            error = new IOException(e.getMessage(), e);
                        }
                    } finally {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            synchronized (ConvertingReader.this) {
                                if (null == error) error = e;
                            }
                        }
                    }
                }
            }.start();
            delegate = reader;
        } catch (IOException e) {
            synchronized (this) {
                // give preference to error from sub-process
                if (null == error) error = e;
            }
        }
    }

    private static List<Network> getNetworks(final InputStream input) throws IOException {
        StationService service = ServiceUtil.getInstance().getStationService();
        List<Network> networks = service.load(input);
        if (null == networks || networks.isEmpty()) {
            throw new IOException("IRIS-WS found no networks in the data");
        }
        return networks;
    }

    /**
     * @throws IOException If an exception was generated during initialisation.
     */
    private void assertNoError() throws IOException {
        if (error != null) {
            throw error;
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        assertNoError();
        return delegate.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        assertNoError();
        delegate.close();
    }

    /**
     * A utility method that includes the necessary flag logic.
     *
     * @param input The source of input data.
     * @return A reader that returns response format data.
     */
    public static Reader toReader(InputStream input) {
        if (input == null) {
            return null;
        } else {
            BufferedInputStream buffer = new BufferedInputStream(input);
            buffer.mark(NON_BLANK_SPACE);
            boolean xml = false;
            try {
                for (int i = 0; i < NON_BLANK_SPACE; ++i) {
                    int c = buffer.read();
                    if (c == -1) break;
                    if (c == (int)' ') continue;
                    if (c == (int)'\n') continue;
                    if (c == (int)'\r') continue;
                    if (c == (int)'<') {xml = true; break;}
                    break;  // other character
                }
                buffer.reset();
            } catch (IOException e) {
                // this will come out later
            }
            if (xml) {
                return new ConvertingReader(buffer);
            } else {
                return new InputStreamReader(buffer);
            }
        }
    }

    /**
     * Debug method to check the above.
     *
     * @param path A file with XML data.
     */
    public static void dump(String path) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        try {
            writer.printf("Calling IRIS-WS to read %s\n", path);
            final List<Network> networks = getNetworks(new FileInputStream(path));
            writer.printf("Found %d networks\n", networks.size());
            writer.println("Calling IRIS-WS to convert to SEED");
            RespUtil.write(writer, networks);
            writer.println("Output complete");
        } finally {
            writer.close();
        }
    }

}
