package org.sfm.reflect.meta;

import org.sfm.reflect.InstantiatorDefinition;
import org.sfm.reflect.Parameter;
import org.sfm.reflect.TypeHelper;

import java.util.Arrays;
import java.util.List;

public class OptionalPropertyFinder<T> implements PropertyFinder<T> {


    private final OptionalClassMeta<T> tupleClassMeta;
    private final PropertyFinder<?> propertyFinder;

    public OptionalPropertyFinder(OptionalClassMeta<T> tupleClassMeta) {
        this.tupleClassMeta = tupleClassMeta;
        this.propertyFinder = tupleClassMeta.getReflectionService().getClassMeta(tupleClassMeta.getTargetType(), false).newPropertyFinder();
	}


    @Override
    public <E> PropertyMeta<T, E> findProperty(PropertyNameMatcher propertyNameMatcher) {
        if (propertyNameMatcher.matches(tupleClassMeta.getProperty().getName())) {
            return (PropertyMeta<T, E>) tupleClassMeta.getProperty();
        } else {
            final PropertyMeta<?, Object> property = propertyFinder.findProperty(propertyNameMatcher);

            if (property != null) {
                return getSubPropertyMeta((PropertyMeta<E, ?>) property);
            }
        }


        return null;
    }

    private <E> PropertyMeta<T, E> getSubPropertyMeta(PropertyMeta<E, ?> property) {
        return new SubPropertyMeta<T, E>(tupleClassMeta.getReflectionService(), (PropertyMeta<T, E>) tupleClassMeta.getProperty(), property);
    }

    @Override
    public List<InstantiatorDefinition> getEligibleInstantiatorDefinitions() {
        return Arrays.asList(tupleClassMeta.getInstantiatorDefinition());
    }

    @Override
    public <E> ConstructorPropertyMeta<T, E> findConstructor(InstantiatorDefinition instantiatorDefinition) {
        return null;
    }
}
