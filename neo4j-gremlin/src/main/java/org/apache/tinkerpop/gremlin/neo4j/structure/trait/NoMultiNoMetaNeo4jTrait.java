/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.apache.tinkerpop.gremlin.neo4j.structure.trait;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jHelper;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertexProperty;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.neo4j.tinkerpop.api.Neo4jDirection;
import org.neo4j.tinkerpop.api.Neo4jNode;
import org.neo4j.tinkerpop.api.Neo4jRelationship;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NoMultiNoMetaNeo4jTrait implements Neo4jTrait {

    @Override
    public Predicate<Neo4jNode> getNodePredicate() {
        return node -> true;
    }

    @Override
    public Predicate<Neo4jRelationship> getRelationshipPredicate() {
        return relationship -> true;
    }

    @Override
    public void removeVertex(final Neo4jVertex vertex) {
        try {
            final Neo4jNode node = vertex.getBaseVertex();
            for (final Neo4jRelationship relationship : node.relationships(Neo4jDirection.BOTH)) {
                relationship.delete();
            }
            node.delete();
        } catch (final IllegalStateException ignored) {
            // this one happens if the vertex is still chilling in the tx
        } catch (final RuntimeException ex) {
            if (!Neo4jHelper.isNotFound(ex)) throw ex;
            // this one happens if the vertex is committed
        }
    }

    @Override
    public <V> VertexProperty<V> getVertexProperty(final Neo4jVertex vertex, final String key) {
        if (Neo4jHelper.keyExistsInNeo4j(vertex.getBaseVertex(), key)) {
            return new Neo4jVertexProperty<>(vertex, key, (V) vertex.getBaseVertex().getProperty(key));
        } else
            return VertexProperty.<V>empty();
    }

    @Override
    public <V> Iterator<VertexProperty<V>> getVertexProperties(final Neo4jVertex vertex, final String... keys) {
        return (Iterator) IteratorUtils.stream(vertex.getBaseVertex().getKeys())
                .filter(key -> ElementHelper.keyExists(key, keys))
                .map(key -> new Neo4jVertexProperty<>(vertex, key, (V) vertex.getBaseVertex().getProperty(key))).iterator();
    }

    @Override
    public <V> VertexProperty<V> setVertexProperty(final Neo4jVertex vertex, final VertexProperty.Cardinality cardinality, final String key, final V value, final Object... keyValues) {
        if (cardinality != VertexProperty.Cardinality.single)
            throw VertexProperty.Exceptions.multiPropertiesNotSupported();
        if (keyValues.length > 0)
            throw VertexProperty.Exceptions.metaPropertiesNotSupported();
        ElementHelper.validateProperty(key, value);
        try {
            vertex.getBaseVertex().setProperty(key, value);
            return new Neo4jVertexProperty<>(vertex, key, value);
        } catch (final IllegalArgumentException iae) {
            throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
        }
    }

    @Override
    public VertexProperty.Cardinality getCardinality(final String key) {
        return VertexProperty.Cardinality.single;
    }

    @Override
    public boolean supportsMultiProperties() {
        return false;
    }

    @Override
    public boolean supportsMetaProperties() {
        return false;
    }

    @Override
    public void removeVertexProperty(final Neo4jVertexProperty vertexProperty) {
        if (Neo4jHelper.keyExistsInNeo4j(((Neo4jVertex) vertexProperty.element()).getBaseVertex(), vertexProperty.key()))
            ((Neo4jVertex) vertexProperty.element()).getBaseVertex().removeProperty(vertexProperty.key());
    }

    @Override
    public <V> Property<V> setProperty(final Neo4jVertexProperty vertexProperty, final String key, final V value) {
        throw VertexProperty.Exceptions.metaPropertiesNotSupported();
    }

    @Override
    public <V> Property<V> getProperty(final Neo4jVertexProperty vertexProperty, final String key) {
        throw VertexProperty.Exceptions.metaPropertiesNotSupported();
    }

    @Override
    public <V> Iterator<Property<V>> getProperties(final Neo4jVertexProperty vertexProperty, final String... keys) {
        throw VertexProperty.Exceptions.metaPropertiesNotSupported();
    }
}