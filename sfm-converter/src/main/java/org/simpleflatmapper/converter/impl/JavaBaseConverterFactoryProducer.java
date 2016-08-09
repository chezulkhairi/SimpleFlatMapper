package org.simpleflatmapper.converter.impl;


import org.simpleflatmapper.converter.AbstractConverterFactory;
import org.simpleflatmapper.converter.AbstractConverterFactoryProducer;
import org.simpleflatmapper.converter.Converter;
import org.simpleflatmapper.converter.ConverterFactory;
import org.simpleflatmapper.converter.ConvertingTypes;

import org.simpleflatmapper.converter.ToStringConverter;
import org.simpleflatmapper.util.Consumer;
import org.simpleflatmapper.util.TypeHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;

public class JavaBaseConverterFactoryProducer extends AbstractConverterFactoryProducer {
	@Override
	public void produce(Consumer<ConverterFactory> consumer) {
		constantConverter(consumer, Number.class, Byte.class      , new NumberByteConverter());
		constantConverter(consumer, Number.class, Short.class     , new NumberShortConverter());
		constantConverter(consumer, Number.class, Integer.class   , new NumberIntegerConverter());
		constantConverter(consumer, Number.class, Long.class      , new NumberLongConverter());
		constantConverter(consumer, Number.class, Float.class     , new NumberFloatConverter());
		constantConverter(consumer, Number.class, Double.class    , new NumberDoubleConverter());
		constantConverter(consumer, Number.class, BigDecimal.class, new NumberBigDecimalConverter());
		constantConverter(consumer, Number.class, BigInteger.class, new NumberBigIntegerConverter());


		constantConverter(consumer, CharSequence.class, Byte.class,      new CharSequenceByteConverter());
		constantConverter(consumer, CharSequence.class, Character.class, new CharSequenceCharacterConverter());
		constantConverter(consumer, CharSequence.class, Short.class,     new CharSequenceShortConverter());
		constantConverter(consumer, CharSequence.class, Integer.class,   new CharSequenceIntegerConverter());
		constantConverter(consumer, CharSequence.class, Long.class,      new CharSequenceLongConverter());
		constantConverter(consumer, CharSequence.class, Float.class,     new CharSequenceFloatConverter());
		constantConverter(consumer, CharSequence.class, Double.class,    new CharSequenceDoubleConverter());
		constantConverter(consumer, CharSequence.class, UUID.class,      new CharSequenceUUIDConverter());
		factoryConverter (consumer, new AbstractConverterFactory<CharSequence, Enum>(CharSequence.class, Enum.class) {
			@SuppressWarnings("unchecked")
			public Converter<? super CharSequence, ? extends Enum> newConverter(ConvertingTypes targetedTypes, Object... params) {
				return new CharSequenceToEnumConverter(TypeHelper.toClass(targetedTypes.getTo()));
			}

			@Override
			public int score(ConvertingTypes targetedTypes) {
				if (TypeHelper.isAssignable(Enum.class, targetedTypes.getTo())) {
					return ConvertingTypes.getSourceScore(convertingTypes.getFrom(), targetedTypes.getFrom());
				}
				return -1;
			}
		});

		constantConverter(consumer, Object.class, String.class, ToStringConverter.INSTANCE);
		constantConverter(consumer, Object.class, URL.class, new ToStringToURLConverter());
	}
}