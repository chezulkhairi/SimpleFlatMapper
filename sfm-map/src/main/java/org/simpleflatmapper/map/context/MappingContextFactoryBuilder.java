package org.simpleflatmapper.map.context;


import org.simpleflatmapper.map.MappingContext;
import org.simpleflatmapper.map.context.impl.KeyDefinitionBuilder;
import org.simpleflatmapper.map.impl.JoinUtils;
import org.simpleflatmapper.reflect.meta.ArrayElementPropertyMeta;
import org.simpleflatmapper.reflect.meta.MapElementPropertyMeta;
import org.simpleflatmapper.reflect.meta.MapKeyValueElementPropertyMeta;
import org.simpleflatmapper.reflect.meta.PropertyMeta;
import org.simpleflatmapper.map.context.impl.BreakDetectorMappingContextFactory;
import org.simpleflatmapper.map.context.impl.NullChecker;
import org.simpleflatmapper.map.context.impl.ValuedMappingContextFactory;
import org.simpleflatmapper.reflect.setter.AppendCollectionSetter;
import org.simpleflatmapper.util.Predicate;
import org.simpleflatmapper.util.Supplier;

import java.util.ArrayList;
import java.util.List;

public class MappingContextFactoryBuilder<S, K> {

    private final Counter counter;
    private final int currentIndex;
    private final MappingContextFactoryBuilder<S, K> parent;
    private final List<K> keys;
    private final KeySourceGetter<K, S> keySourceGetter;
    private final List<MappingContextFactoryBuilder<S, K>> children = new ArrayList<MappingContextFactoryBuilder<S, K>>();
    private final List<Supplier<?>> suppliers = new ArrayList<Supplier<?>>();
    private final PropertyMeta<?, ?> owner;

    public MappingContextFactoryBuilder(KeySourceGetter<K, S> keySourceGetter) {
        this(new Counter(), new ArrayList<K>(), keySourceGetter, null, null);
    }

    protected MappingContextFactoryBuilder(Counter counter, List<K> keys, KeySourceGetter<K, S> keySourceGetter, MappingContextFactoryBuilder<S, K> parent, PropertyMeta<?, ?> owner) {
        this.counter = counter;
        this.currentIndex = counter.value;
        this.keys = keys;
        this.keySourceGetter = keySourceGetter;
        this.parent = parent;
        this.counter.value++;
        this.owner = owner;
    }


    public void addKey(K key) {
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    public void addSupplier(int index, Supplier<?> supplier) {
        while(suppliers.size() <= index) {
            suppliers.add(null);
        }
        if (suppliers.get(index) != null) {
            throw new IllegalStateException("Conflicting suppliers");
        }
        suppliers.set(index, supplier);
    }

    public Predicate<S> nullChecker() {
        return new NullChecker<S, K>(keys, keySourceGetter);
    }

    public MappingContextFactoryBuilder<S, K> newBuilder(List<K> subKeys, PropertyMeta<?, ?> owner) {
        MappingContextFactoryBuilder<S, K> subBuilder = new MappingContextFactoryBuilder<S, K>(counter, subKeys, keySourceGetter, this, owner);
        children.add(subBuilder);
        return subBuilder;
    }

    @SuppressWarnings("unchecked")
    public MappingContextFactory<S> newFactory() {
        if (parent != null)  {
            throw new IllegalStateException();
        }


        MappingContextFactory<S> context;

        ArrayList<MappingContextFactoryBuilder<S, K>> builders = new ArrayList<MappingContextFactoryBuilder<S, K>>();
        addAllBuilders(builders);
        
        copySuppliers(builders);

        if (suppliers.isEmpty()) {
            context = MappingContext.EMPTY_FACTORY;
        } else {
            context = new ValuedMappingContextFactory<S>(suppliers);
        }

        if (hasKeys(builders)) {
            KeyDefinitionBuilder<S, K>[] keyDefinitionsBuilder = new KeyDefinitionBuilder[builders.get(builders.size() - 1).currentIndex + 1];

            for (int i = 0; i < builders.size(); i++) {
                MappingContextFactoryBuilder<S, K> builder = builders.get(i);

                populateKey(keyDefinitionsBuilder, builders, builder);
            }

            KeyDefinition<S, K>[] keyDefinitions = KeyDefinitionBuilder.<S, K>toKeyDefinitions(keyDefinitionsBuilder);
            KeyDefinition<S, K> rootKeyDefinition = keyDefinitions[0];

            context = new BreakDetectorMappingContextFactory<S>(rootKeyDefinition, keyDefinitions, context);
        }

        return context;
    }

    private void copySuppliers(ArrayList<MappingContextFactoryBuilder<S, K>> builders) {
        for (int i = 1; i < builders.size(); i++) {
            MappingContextFactoryBuilder<S, K> builder = builders.get(i);
            for(int j = 0; j < builder.suppliers.size(); j++) {
                Supplier<?> supplier = builder.suppliers.get(j);
                if (supplier != null) {
                    addSupplier(j, supplier);
                }
            }
        }
    }

    private KeyDefinitionBuilder<S, K> populateKey(KeyDefinitionBuilder<S, K>[] keyDefinitions, ArrayList<MappingContextFactoryBuilder<S, K>> builders, MappingContextFactoryBuilder<S, K> builder) {

        if (keyDefinitions[builder.currentIndex] != null) {
            return keyDefinitions[builder.currentIndex];
        }

        int parentIndex = builder.getNonEmptyParentIndex();

        KeyDefinitionBuilder<S, K> parent = null;
        if (parentIndex != -1) {
            parent = keyDefinitions[parentIndex];
            if (parent == null) {
                // not yet define look for parent and create key
                for(int i = 0; i < builders.size(); i++) {
                    MappingContextFactoryBuilder<S, K> potentialParent = builders.get(i);
                    if (potentialParent.currentIndex == parentIndex) {
                        parent = populateKey(keyDefinitions, builders, potentialParent);
                        break;
                    }
                }
                if (parent == null) {
                    throw new IllegalArgumentException("Could not find parent for builder " + builder);
                }
            }
        }

        KeyDefinitionBuilder<S, K> keyDefinition;

        // empty key use parent key except for child of appendsetter
        if (parent != null && builder.inheritKeys(parentIndex)) {
             keyDefinition = parent.asChild(builder.currentIndex);
        } else {
            List<K> keys = new ArrayList<K>(builder.effectiveKeys());

            // ignore root parent
            if (parent != null && parentIndex >0) {
                appendParentKeys(parent, keys);
            }

            keyDefinition = new KeyDefinitionBuilder<S, K>(keys, builder.keySourceGetter, builder.currentIndex);
        }

        keyDefinitions[builder.currentIndex] = keyDefinition;
        return keyDefinition;
    }

    private boolean inheritKeys(int parentIndex) {
        return (effectiveKeys().isEmpty() && ! newObjectOnEachRow(parentIndex));
    }

    private void appendParentKeys(KeyDefinitionBuilder<S, K> parent, List<K> keys) {
        // if keys is empty we generate a new row every time so leave empty
        if (!keys.isEmpty()) {
            for(K k : parent.getKeys()) {
                if (!keys.contains(k)) {
                    keys.add(k);
                }
            }
        }
    }

    private List<K> effectiveKeys() {

        if (!keys.isEmpty()) {
            return keys;
        }

        List<K> keys = new ArrayList<K>();
        for(MappingContextFactoryBuilder<S, K> child : children) {
            if (child.isEligibleAsSubstituteKey()) {
                keys.addAll(child.effectiveKeys());
            }

        }
        return keys;
    }


    private boolean newObjectOnEachRow(int parentIndex) {
        if (owner instanceof ArrayElementPropertyMeta) {
            ArrayElementPropertyMeta elementPropertyMeta = (ArrayElementPropertyMeta) owner;
            if (elementPropertyMeta.getSetter() instanceof AppendCollectionSetter) {
                return true;
            }
        } else if (owner instanceof MapKeyValueElementPropertyMeta) {
            return true;
        }
        
        if (parent != null && parent.currentIndex != parentIndex ) {
            return parent.newObjectOnEachRow(parentIndex);
        }
        
        return false;
    }

    private static <S, K> boolean hasKeys(ArrayList<MappingContextFactoryBuilder<S, K>> builders) {
        for(int i = 0; i < builders.size(); i++) {
            if (!builders.get(i).hasNoKeys()) return true;
        }
        return false;
    }

    private boolean isEligibleAsSubstituteKey() {
        return !JoinUtils.isArrayElement(owner);
    }

    // ignore empty parent useful to skip root keys
    private int getNonEmptyParentIndex() {
        return parent == null
                ? -1
                : parent.effectiveKeys().isEmpty() ? parent.getNonEmptyParentIndex() : parent.currentIndex;
    }


    private void addAllBuilders(ArrayList<MappingContextFactoryBuilder<S, K>> builders) {
        builders.add(this);
        for(MappingContextFactoryBuilder<S, K> child : children) {
            child.addAllBuilders(builders);
        }
    }

    public boolean hasNoKeys() {
        return effectiveKeys().isEmpty();
    }

    public boolean hasNoDependentKeys() {
        if (!hasNoKeys()) {
            return false;
        }

        for(MappingContextFactoryBuilder<S, K> builder : children) {
            if (!builder.hasNoDependentKeys()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public int currentIndex() {
        return currentIndex;
    }

    public boolean hasChildren() {
        return children.isEmpty();
    }

    private static class Counter {
        int value;
    }

    @Override
    public String toString() {
        return "MappingContextFactoryBuilder{" +
                "currentIndex=" + currentIndex +
                ", keys=" + keys +
                ", children=" + children +
                '}';
    }
}
