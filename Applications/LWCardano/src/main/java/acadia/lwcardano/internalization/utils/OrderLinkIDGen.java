package acadia.lwcardano.internalization.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OrderLinkIDGen {
    public static String generate() {
        // Next 6 characters is time, YYMMDD
        String sid = DateTimeFormatter.ofPattern("yyMMdd").format(OffsetDateTime.now());

        // Next 16 characters is UUID (first 8 chars and last 8 chars of UUID)
        UUID uuid = UUID.randomUUID();
        int length = 6;
        String uuidStr = uuid.toString().toUpperCase().replace("-", "");
        String uuidShortStr = uuidStr.substring(0, length);
        return sid + "-" + uuidShortStr;
    }
}
