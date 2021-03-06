module org.simpleflatmapper.datastax {
        requires transitive org.simpleflatmapper.map;
        requires transitive org.simpleflatmapper.tuple;
        requires cassandra.driver.core;
        requires guava;
        exports org.simpleflatmapper.datastax;

        provides org.simpleflatmapper.reflect.meta.AliasProviderProducer
        with org.simpleflatmapper.datastax.impl.mapping.DatastaxAliasProviderFactory;
        
        provides org.simpleflatmapper.converter.ConverterFactoryProducer
                with org.simpleflatmapper.datastax.impl.converter.DatastaxConverterFactoryProducer;
}