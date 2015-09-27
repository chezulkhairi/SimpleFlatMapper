package org.sfm.jdbc;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.sfm.map.FieldMapper;
import org.sfm.map.column.ColumnProperty;
import org.sfm.map.column.FieldMapperColumnDefinition;
import org.sfm.map.column.joda.JodaDateTimeZoneProperty;
//IFJAVA8_START
import org.sfm.map.column.time.JavaZoneIdProperty;
//IFJAVA8_END
import org.sfm.map.impl.ConstantTargetFieldMapperFactorImpl;
import org.sfm.map.mapper.PropertyMapping;
import org.sfm.reflect.Getter;
import org.sfm.reflect.impl.*;
import org.sfm.reflect.meta.PropertyMeta;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;

//IFJAVA8_START
import java.time.ZoneId;
//IFJAVA8_END

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.mockito.Mockito.*;

public class PreparedStatementFieldMapperFactoryTest {

    private ConstantTargetFieldMapperFactorImpl factory;

    private PreparedStatement ps;

    private int index;
    @Before
    public void setUp() {
        factory = ConstantTargetFieldMapperFactorImpl.instance();
        ps = mock(PreparedStatement.class);
        index = 1;
    }

    @Test
    public void testMapBoolean() throws Exception {
        newFieldMapperAndMapToPS(new ConstantBooleanGetter<Object>(true), boolean.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Boolean>(false), Boolean.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Boolean>(), Boolean.class);

        verify(ps).setBoolean(1, true);
        verify(ps).setBoolean(2, false);
        verify(ps).setNull(3, Types.BOOLEAN);
    }

    @Test
    public void testMapByte() throws Exception {
        newFieldMapperAndMapToPS(new ConstantByteGetter<Object>((byte)2), byte.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Byte>((byte) 3), Byte.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Byte>(), Byte.class);

        verify(ps).setByte(1, (byte) 2);
        verify(ps).setByte(2, (byte) 3);
        verify(ps).setNull(3, Types.TINYINT);
    }

    @Test
    public void testMapChar() throws Exception {
        newFieldMapperAndMapToPS(new ConstantCharacterGetter<Object>((char)2), char.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Character>((char) 3), Character.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Character>(), Character.class);

        verify(ps).setInt(1, 2);
        verify(ps).setInt(2, 3);
        verify(ps).setNull(3, Types.INTEGER);
    }

    @Test
    public void testMapShort() throws Exception {
        newFieldMapperAndMapToPS(new ConstantShortGetter<Object>((short)2), short.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Short>((short) 3), Short.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Short>(), Short.class);

        verify(ps).setShort(1, (short) 2);
        verify(ps).setShort(2, (short) 3);
        verify(ps).setNull(3, Types.SMALLINT);
    }

    @Test
    public void testMapInt() throws Exception {
        newFieldMapperAndMapToPS(new ConstantIntGetter<Object>(2), int.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Integer>(3), Integer.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Integer>(), Integer.class);

        verify(ps).setInt(1, 2);
        verify(ps).setInt(2, 3);
        verify(ps).setNull(3, Types.INTEGER);
    }

    @Test
    public void testMapLong() throws Exception {
        newFieldMapperAndMapToPS(new ConstantLongGetter<Object>((long)2), long.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Long>((long) 3), Long.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Long>(), Long.class);

        verify(ps).setLong(1, (long) 2);
        verify(ps).setLong(2, (long) 3);
        verify(ps).setNull(3, Types.BIGINT);
    }

    @Test
    public void testMapFloat() throws Exception {
        newFieldMapperAndMapToPS(new ConstantFloatGetter<Object>((float)2), float.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Float>((float) 3), Float.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Float>(), Float.class);

        verify(ps).setFloat(1, (float) 2);
        verify(ps).setFloat(2, (float) 3);
        verify(ps).setNull(3, Types.FLOAT);
    }

    @Test
    public void testMapDouble() throws Exception {
        newFieldMapperAndMapToPS(new ConstantDoubleGetter<Object>((double)2), double.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Double>((double) 3), Double.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Double>(), Double.class);

        verify(ps).setDouble(1, (double) 2);
        verify(ps).setDouble(2, (double) 3);
        verify(ps).setNull(3, Types.DOUBLE);
    }

    @Test
    public void testMapDateNoSqlType() throws Exception {
        final Date date = new Date();
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Date>(date), Date.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Date>(), Date.class);

        verify(ps).setTimestamp(1, new Timestamp(date.getTime()));
        verify(ps).setNull(2, Types.TIMESTAMP);
    }

    @Test
    public void testMapSqlDate() throws Exception {
        final java.sql.Date date = new java.sql.Date(new Date().getTime());
        newFieldMapperAndMapToPS(new ConstantGetter<Object, java.sql.Date>(date), java.sql.Date.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, java.sql.Date>(), java.sql.Date.class);

        verify(ps).setDate(1, date);
        verify(ps).setNull(2, Types.DATE);
    }

    @Test
    public void testMapTimestamp() throws Exception {
        final Timestamp date = new Timestamp(new Date().getTime());
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Timestamp>(date), Timestamp.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Timestamp>(), Timestamp.class);

        verify(ps).setTimestamp(1, date);
        verify(ps).setNull(2, Types.TIMESTAMP);
    }

    @Test
    public void testMapTime() throws Exception {
        final Time date = new Time(new Date().getTime());
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Time>(date), Time.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Time>(), Time.class);

        verify(ps).setTime(1, date);
        verify(ps).setNull(2, Types.TIME);
    }

    @Test
    public void testMapCalendar() throws Exception {
        final Date date = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        newFieldMapperAndMapToPS(new ConstantGetter<Object, Calendar>(cal), Calendar.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Calendar>(), Calendar.class);

        verify(ps).setTimestamp(1, new Timestamp(date.getTime()));
        verify(ps).setNull(2, Types.TIMESTAMP);
    }

    @Test
    public void testMapString() throws Exception {
        newFieldMapperAndMapToPS(new ConstantGetter<Object, String>("xyz"), String.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, String>(), String.class);

        verify(ps).setString(1, "xyz");
        verify(ps).setNull(2, Types.VARCHAR);
    }


    @Test
    public void testMapURL() throws Exception {
        URL url = new URL("https://github.com/arnaudroger/SimpleFlatMapper/");
        newFieldMapperAndMapToPS(new ConstantGetter<Object, URL>(url), URL.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, URL>(), URL.class);

        verify(ps).setURL(1, url);
        verify(ps).setNull(2, Types.DATALINK);
    }

    @Test
    public void testBigDecimal() throws Exception {
        BigDecimal value = new BigDecimal("234.45");
        newFieldMapperAndMapToPS(new ConstantGetter<Object, BigDecimal>(value), BigDecimal.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, BigDecimal>(), BigDecimal.class);

        verify(ps).setBigDecimal(1, value);
        verify(ps).setNull(2, Types.NUMERIC);
    }

    @Test
    public void testInputStream() throws Exception {
        InputStream value = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 });
        newFieldMapperAndMapToPS(new ConstantGetter<Object, InputStream>(value), InputStream.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, InputStream>(), InputStream.class);

        verify(ps).setBinaryStream(1, value);
        verify(ps).setNull(2, Types.BINARY);
    }

    @Test
    public void testBlob() throws Exception {
        Blob value = mock(Blob.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Blob>(value), Blob.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Blob>(), Blob.class);

        verify(ps).setBlob(1, value);
        verify(ps).setNull(2, Types.BINARY);
    }

    @Test
    public void testBytes() throws Exception {
        byte[] value = new byte[] { 1, 2, 3, 4 };
        newFieldMapperAndMapToPS(new ConstantGetter<Object, byte[]>(value), byte[].class);
        newFieldMapperAndMapToPS(new NullGetter<Object, byte[]>(), byte[].class);

        verify(ps).setBytes(1, value);
        verify(ps).setNull(2, Types.BINARY);
    }

    @Test
    public void testRef() throws Exception {
        Ref value = mock(Ref.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Ref>(value), Ref.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Ref>(), Ref.class);

        verify(ps).setRef(1, value);
        verify(ps).setNull(2, Types.REF);
    }

    @Test
    public void testReader() throws Exception {
        Reader value = mock(Reader.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Reader>(value), Reader.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Reader>(), Reader.class);

        verify(ps).setCharacterStream(1, value);
        verify(ps).setNull(2, Types.VARCHAR);
    }

    @Test
    public void testClob() throws Exception {
        Clob value = mock(Clob.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Clob>(value), Clob.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Clob>(), Clob.class);

        verify(ps).setClob(1, value);
        verify(ps).setNull(2, Types.CLOB);
    }

    @Test
    public void testNClob() throws Exception {
        NClob value = mock(NClob.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, NClob>(value), NClob.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, NClob>(), NClob.class);

        verify(ps).setNClob(1, value);
        verify(ps).setNull(2, Types.NCLOB);
    }

    @Test
    public void testRowId() throws Exception {
        RowId value = mock(RowId.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, RowId>(value), RowId.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, RowId>(), RowId.class);

        verify(ps).setRowId(1, value);
        verify(ps).setNull(2, Types.ROWID);
    }

    @Test
    public void testSQLXML() throws Exception {
        SQLXML value = mock(SQLXML.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, SQLXML>(value), SQLXML.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, SQLXML>(), SQLXML.class);

        verify(ps).setSQLXML(1, value);
        verify(ps).setNull(2, Types.SQLXML);
    }

    @Test
    public void testArray() throws Exception {
        Array value = mock(Array.class);
        newFieldMapperAndMapToPS(new ConstantGetter<Object, Array>(value), Array.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, Array>(), Array.class);

        verify(ps).setArray(1, value);
        verify(ps).setNull(2, Types.ARRAY);
    }

    @Test
    public void testJodaDateTime() throws Exception {
        org.joda.time.DateTime value = new org.joda.time.DateTime();

        newFieldMapperAndMapToPS(new ConstantGetter<Object, org.joda.time.DateTime>(value), org.joda.time.DateTime.class);
        newFieldMapperAndMapToPS(new NullGetter<Object, org.joda.time.DateTime>(), org.joda.time.DateTime.class);

        verify(ps).setTimestamp(1, new Timestamp(value.getMillis()));
        verify(ps).setNull(2, Types.TIMESTAMP);
    }

    @Test
    public void testJodaLocalDateTime() throws Exception {
        org.joda.time.LocalDateTime value = new org.joda.time.LocalDateTime();
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        newFieldMapperAndMapToPS(new ConstantGetter<Object, org.joda.time.LocalDateTime>(value), org.joda.time.LocalDateTime.class, new JodaDateTimeZoneProperty(dateTimeZone));
        newFieldMapperAndMapToPS(new NullGetter<Object, org.joda.time.LocalDateTime>(), org.joda.time.LocalDateTime.class);

        verify(ps).setTimestamp(1, new Timestamp(value.toDateTime(dateTimeZone).getMillis()));
        verify(ps).setNull(2, Types.TIMESTAMP);
    }

    @Test
    public void testJodaLocalTime() throws Exception {
        org.joda.time.LocalTime value = new org.joda.time.LocalTime();
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        newFieldMapperAndMapToPS(new ConstantGetter<Object, org.joda.time.LocalTime>(value), org.joda.time.LocalTime.class, new JodaDateTimeZoneProperty(dateTimeZone));
        newFieldMapperAndMapToPS(new NullGetter<Object, org.joda.time.LocalTime>(), org.joda.time.LocalTime.class);

        verify(ps).setTime(1, new Time(value.toDateTimeToday(dateTimeZone).getMillis()));
        verify(ps).setNull(2, Types.TIME);
    }

    @Test
    public void testJodaLocaDate() throws Exception {
        org.joda.time.LocalDate value = new org.joda.time.LocalDate();
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        newFieldMapperAndMapToPS(new ConstantGetter<Object, org.joda.time.LocalDate>(value), org.joda.time.LocalDate.class, new JodaDateTimeZoneProperty(dateTimeZone));
        newFieldMapperAndMapToPS(new NullGetter<Object, org.joda.time.LocalDate>(), org.joda.time.LocalDate.class);

        verify(ps).setDate(1, new java.sql.Date(value.toDate().getTime()));
        verify(ps).setNull(2, Types.DATE);
    }

    //IFJAVA8_START
    @Test
    public void testJavaLocalDateTime() throws Exception {
        java.time.LocalDateTime value = java.time.LocalDateTime.now();
        java.time.ZoneId zoneId = ZoneId.of("America/Los_Angeles");

        newFieldMapperAndMapToPS(new ConstantGetter<Object, java.time.LocalDateTime>(value),  java.time.LocalDateTime.class, new JavaZoneIdProperty(zoneId));
        newFieldMapperAndMapToPS(new NullGetter<Object, java.time.LocalDateTime>(), java.time.LocalDateTime.class);

        verify(ps).setTimestamp(1, new Timestamp(value.atZone(zoneId).toInstant().toEpochMilli()));
        verify(ps).setNull(2, Types.TIMESTAMP);
    }
    //IFJAVA8_END

    protected <T, P> void newFieldMapperAndMapToPS(Getter<T, P> getter, Class<P> clazz, ColumnProperty... properties) throws Exception {
        FieldMapper<T, PreparedStatement> fieldMapper = factory.newFieldMapper(newPropertyMapping(getter, clazz, properties), null, null);
        fieldMapper.mapTo(null, ps, null);
    }

    @SuppressWarnings("unchecked")
    private <T, P> PropertyMapping<T, P, JdbcColumnKey, FieldMapperColumnDefinition<JdbcColumnKey>> newPropertyMapping(Getter<T, P> getter, Class<P> clazz, ColumnProperty... properties) {
        PropertyMeta<T, P> propertyMeta = mock(PropertyMeta.class);
        when(propertyMeta.getGetter()).thenReturn(getter);
        when(propertyMeta.getPropertyType()).thenReturn(clazz);
        return
                new PropertyMapping<T, P, JdbcColumnKey, FieldMapperColumnDefinition<JdbcColumnKey>>(
                        propertyMeta,
                        new JdbcColumnKey("col", index++),
                        FieldMapperColumnDefinition.<JdbcColumnKey>of(properties));
    }






}