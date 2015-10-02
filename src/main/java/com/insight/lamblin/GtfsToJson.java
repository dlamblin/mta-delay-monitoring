package com.insight.lamblin;

import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.insight.lamblin.shared.GetGtfs;
import com.insight.lamblin.shared.Props;
import org.docopt.Docopt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Reads the GTFS data either from a file, stdin or url source. Outputs mostly json-like to stdout.
 * TODO(lamblin): retrofit the json to be correct.
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
            "If the key is omitted a " + Props.LOCAL_PROPERTIES +
            " file with MtaApiKey=<key>\n" +
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

    @SuppressWarnings("unchecked")
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

        // Reading GTFS from standard input or files
        if (optRead) {
            if (optFile.isEmpty()) {
                GtfsToJson.out(System.in);
            } else {
                for (String filename : optFile) {
                    Path path = Paths.get(filename);
                    try (InputStream in = Files.newInputStream(path)) {
                        GtfsToJson.out(in);
                    }
                }
            }
        }

        // Reading GTFS from a URL
        if (optGet) {
            try (InputStream in = GetGtfs.feedUrlStream(optApiKey, optFeed, optUrl)) {
                GtfsToJson.out(in);
            }
        }
    }

    public static void out(InputStream in) throws IOException {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(com.google.transit.realtime.NyctSubway.nyctFeedHeader);
        registry.add(com.google.transit.realtime.NyctSubway.nyctTripDescriptor);
        registry.add(com.google.transit.realtime.NyctSubway.nyctStopTimeUpdate);

        FeedMessage feed = FeedMessage.parseFrom(in, registry);
        if (feed.hasHeader()) {
            System.out.println(feed.getHeader());
        }
        for (FeedEntity entity : feed.getEntityList()) {
            System.out.println(entity);
        }
    }
}
