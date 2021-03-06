package org.simpleflatmapper.converter.joda.impl.time;

import org.joda.time.LocalTime;
import org.simpleflatmapper.converter.Converter;


public class JodaLocalTimeTojuLocalTimeConverter implements Converter<LocalTime, java.time.LocalTime> {


    @Override
    public java.time.LocalTime convert(LocalTime in) throws Exception {
        if (in == null) return null;
        return java.time.LocalTime.of(in.getHourOfDay(), in.getMinuteOfHour(), in.getSecondOfMinute(), in.getMillisOfSecond() * 1000);
    }
}
