package com.insight.lamblin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.docopt.Docopt;

/**
 * import protobuf utils...
 */
public class GtfsToJson {

    private static final String docopt
            = "Gtfs to Json.\n" +
            "\n" +
            "Usage:\n" +
            "  gtfsToJson read [<file>...]\n" +
            "  gtfsToJson get [--apiKey=<key>] [--feed=<id>] [--url=<uri>]\n" +
            "  gtfsToJson --version\n" +
            "  gtfsToJson -h | --help\n" +
            "\n" +
            "Read either reads Gtfs data from files or stdin and outputs the json to stdout.\n" +
            "\n" +
            "Get will request the url assuming the response is a GTFS formatted binary stream,\n" +
            "and similarly output the json to stdout.\n" +
            "Note that the feed id is substituted into the url's last \"=1&\" in\n" +
            "place of \"1\" and the MTA Feed Api Key is appended to the url.\n" +
            "If the key is omitted a local.properties file with apiKey=<key>\n" +
            "is expected.\n" +
            "\n" +
            "Options:\n" +
            "  -f --feed=<id>    Select the subway feed id 1,2,11 [default: 1]\n" +
            "  -h --help         Show this screen.\n" +
            "  -k --apiKey=<key> Your developer MTA Feed Api Key\n" +
            "  -u --url=<uri>    MTA GTFS Feed URL " +
            "[default: http://datamine.mta.info/mta_esi.php?feed_id=1&key=].\n" +
            "  -v --version      Show version.\n"
            + "\n";
    private InputStream in;

    public GtfsToJson(InputStream in) {
        this.in = in;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> opts =
                new Docopt(docopt).withVersion("Gtfs to Json 1.0").parse(args);
        // Cast Docopt map results, --help and --version are handled above.
        String optApiKey = (String) opts.get("--apiKey");
        String optFeed = (String) opts.get("--feed");
        String optUrl = (String) opts.get("--url");
        List<String> optFile = (List<String>) opts.get("<file>");
        Boolean optGet = (Boolean) opts.get("get");
        Boolean optRead = (Boolean) opts.get("read");
        GtfsToJson gtfsToJson = null;

        // Reading GTFS from standard input or files
        if (optRead) {
            if (optFile.isEmpty()) {
                gtfsToJson = new GtfsToJson(System.in);
                gtfsToJson.out();
            } else {
                for (String filename : optFile) {
                    Path path = Paths.get(filename);
                    try (InputStream in = Files.newInputStream(path)) {
                        gtfsToJson = new GtfsToJson(in);
                        gtfsToJson.out();
                    }
                }
            }
        }

        // Reading GTFS from a URL
        if (optGet) {
            if (optApiKey == null) {
                // Defaults to key in local.properties if available
            }
            if (!"1".equals(optFeed) && !"2".equals(optFeed) && !"11".equals(optFeed)) {
                die(optFeed, "Error: Value of '--feed' or '-f' flag was invalid: %s\n" +
                        "\tPlease use:\n\t\t'1' for the 1,2,3,4,5,6,S trains,\n" +
                        "\t\t'2' for the L train, and\n\t\tand '11' for the SIR");
            }
//            System.getProperty("user.dir"));
//            URL url = new URL(
// "file:///C:\\Users\\dlamblin\\Documents\\src\\github.com\\dlamblin\\mta-delay-monitoring\\gtfs");
            int paramFeed = optUrl.lastIndexOf("=1&");
            if (paramFeed > -1) {
                optUrl = optUrl.substring(0, paramFeed + 1) +optFeed +
                        optUrl.substring(paramFeed + 2);
            } else {
                die(optUrl, "Error: Uri given to '--url' or '-u' did not contain '=1&' " +
                        "for feed substitution. Uri was: %s");
            }
            URL url = new URL(optUrl);
            gtfsToJson = new GtfsToJson(url.openStream());
            gtfsToJson.out();
        }
    }

    private static void die(String option, String format) {
        System.err.printf(format, option);
        System.exit(1);
    }

    private void out() throws IOException {
        String readLine;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        while (((readLine = br.readLine()) != null)) {
            System.out.println(readLine);
        }
        in.reset();
        FeedMessage feed = FeedMessage.parseFrom(in);
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate());
            }
        }
    }
}
