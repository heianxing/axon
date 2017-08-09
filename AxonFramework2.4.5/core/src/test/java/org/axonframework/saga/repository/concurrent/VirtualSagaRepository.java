/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.saga.repository.concurrent;

import org.axonframework.common.CollectionUtils;
import org.axonframework.saga.AssociationValue;
import org.axonframework.saga.Saga;
import org.axonframework.saga.repository.AbstractSagaRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Allard Buijze
 */
public class VirtualSagaRepository extends AbstractSagaRepository {

    private ConcurrentMap<String, byte[]> storage = new ConcurrentHashMap<String, byte[]>();
    private List<Saga> deletedSagas = new CopyOnWriteArrayList<Saga>();
    private ConcurrentMap<AssociationValue, String> associations = new ConcurrentHashMap<AssociationValue, String>();

    @Override
    public Saga load(String sagaIdentifier) {
        return deserialize(storage.get(sagaIdentifier));
    }

    @Override
    protected void updateSaga(Saga saga) {
        if (saga.isActive()) {
            storage.put(saga.getSagaIdentifier(), serialize(saga));
        } else {
            throw new IllegalArgumentException(
                    "Unexpected behavior! The update method should not be invoked for inactive sagas");
        }
    }

    @Override
    protected void deleteSaga(Saga saga) {
        storage.remove(saga.getSagaIdentifier());
        deletedSagas.add(saga);
    }

    @Override
    protected void storeSaga(Saga saga) {
        updateSaga(saga);
    }

    @Override
    protected String typeOf(Class<? extends Saga> sagaClass) {
        return sagaClass.getName();
    }

    @Override
    protected Set<String> findAssociatedSagaIdentifiers(Class<? extends Saga> type, AssociationValue associationValue) {
        final String id = associations.get(associationValue);
        if (id != null) {
            return Collections.singleton(id);
        }
        return Collections.emptySet();
    }

    @Override
    protected void storeAssociationValue(AssociationValue newAssociationValue, String sagaType, String sagaIdentifier) {
        associations.put(newAssociationValue, sagaIdentifier);
    }

    @Override
    protected void removeAssociationValue(AssociationValue associationValue, String sagaType, String sagaIdentifier) {
        associations.remove(associationValue);
    }

    public <T extends Saga> List<T> getDeletedSagas(Class<T> type) {
        return CollectionUtils.filterByType(deletedSagas, type);
    }

    private byte[] serialize(Saga saga) {
        if (saga == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream ous = new ObjectOutputStream(baos);
            //noinspection NonSerializableObjectPassedToObjectStream
            ous.writeObject(saga);
            ous.close();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to serialize object", ex);
        }
        return baos.toByteArray();
    }

    /**
     * Deserialize the byte array into an object.
     *
     * @param bytes a serialized object
     * @return the result of deserializing the bytes
     */
    private Saga deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (Saga) ois.readObject();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to deserialize object", ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to deserialize object type", ex);
        }
    }
}
