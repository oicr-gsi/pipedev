package ca.on.oicr.gsi.provenance.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author mlaszloffy
 */
public class DateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").appendOptional(new DateTimeFormatterBuilder().appendLiteral('T').appendPattern("HH:mm:ss").appendOptional(new DateTimeFormatterBuilder().appendPattern(".SSS").toFormatter()).appendPattern("X").toFormatter()).toFormatter();

    @Override
    public ZonedDateTime unmarshal(String date) throws Exception {
        return date == null || date.length() == 0 ? null : ZonedDateTime.parse(date, FMT);
    }

    @Override
    public String marshal(ZonedDateTime date) throws Exception {
        return date == null ? "" : FORMATTER.format(date);
    }

}
