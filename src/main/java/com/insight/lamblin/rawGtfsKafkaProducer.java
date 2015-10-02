package com.insight.lamblin;

import com.google.common.io.ByteStreams;
import com.google.transit.realtime.GtfsRealtime;
import com.insight.lamblin.shared.GetGtfs;
import com.insight.lamblin.shared.Props;
import com.insight.lamblin.shared.SharedStderrLog;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.docopt.Docopt;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;


/**
 * RawGtfsKafkaProducer:
 * Fetches the MTA NYCS Realtime GTFS feed, deserializes it to read the header timestamp
 * which is used to key the serialized message into the specific kafka topic for raw GTFS
 * messages from the particular stream.
 * <p>
 * See also the usage statement.
 */
public class RawGtfsKafkaProducer {
    public static final String docopt
            = "Raw Gtfs Kafka Producer.\n" +
            "\n" +
            "Usage:\n" +
            "  rawGtfsKafkaProducer [--apiKey=<key>] [--feed=<id>] [--url=<uri>]\n" +
            "  rawGtfsKafkaProducer --version\n" +
            "  rawGtfsKafkaProducer -h | --help\n" +
            "\n" +
            "Read the GTFS data from the url and output the header's timestamp as\n" +
            "a key for the Kafka topic related to the feed id. The GTFS data will\n" +
            "be produced in serialized form to the topic.\n" +
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
                new Docopt(docopt).withVersion("Raw Gtfs Kafka Producer 1.0").parse(args);
        // Cast Docopt map results, --help and --version are handled above.
        String optApiKey = (String) opts.get("--apiKey");
        String optFeed = (String) opts.get("--feed");
        String optUrl = (String) opts.get("--url");

        Properties props = new Properties();
        props.put("metadata.broker.list", "172.31.22.7:9092,172.31.22.6:9092,172.31.22.5:9092,172.31.22.4:9092");
        props.put("request.required.acks", "1");
        KafkaProducer<Long, byte[]> producer = new KafkaProducer<>(props);

        // Reading GTFS from a URL
        GtfsRealtime.FeedMessage msg;
        byte[] gtfs;
        try (InputStream in = GetGtfs.feedUrlStream(optApiKey, optFeed, optUrl)) {
            gtfs = ByteStreams.toByteArray(in);
            msg = GtfsRealtime.FeedMessage.parseFrom(gtfs);
        }
        long timestamp;
        if (msg.hasHeader() && msg.getHeader().hasTimestamp()) {
            timestamp = msg.getHeader().getTimestamp();
        } else {
            timestamp = new GregorianCalendar().getTimeInMillis();
            SharedStderrLog.log("Info: GTFS contained no timestamp. Added " +
                    timestamp + " " + formatEpoch(timestamp));
        }

        ProducerRecord<Long, byte[]> data =
                new ProducerRecord<>("raw-gtfs-" + optFeed, timestamp, gtfs);
        producer.send(data);
        producer.close();
    }

    public static String formatEpoch(long epochSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
        return sdf.format(new Date(epochSeconds * 1000));
    }

}