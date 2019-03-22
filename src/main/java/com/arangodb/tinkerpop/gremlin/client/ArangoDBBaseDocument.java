//////////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the TinkerPop OLTP Provider API for ArangoDB
//
// Copyright triAGENS GmbH Cologne and The University of York
//
//////////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.tinkerpop.gremlin.client;

import com.arangodb.entity.DocumentField;
import com.arangodb.tinkerpop.gremlin.structure.ArangoDBGraph;
import com.arangodb.velocypack.annotations.Expose;

/**
 * The ArangoDB BaseBaseDocument provides the internal fields required for the driver to correctly
 * serialize and deserialize vertices and edges.
 *
 * @author Horacio Hoyos Rodriguez (https://www.york.ac.uk)
 */

public abstract class ArangoDBBaseDocument {

    /** ArangoDB internal id. */

    @DocumentField(DocumentField.Type.ID)
    protected String _id;

    /** ArangoDB internal revision. */

    @DocumentField(DocumentField.Type.REV)
    protected String _rev;

    /** ArangoDB internal name - mapped to TinkerPop's ID. */

    @DocumentField(DocumentField.Type.KEY)
    protected String _key;

    /** The label of the document */

    @Expose
    protected String label;

    /** The collection in which the element is placed. */

    @Expose(serialize = false, deserialize = false)
    protected String collection;

    /** the graph of the document. */

    @Expose(serialize = false, deserialize = false)
    protected ArangoDBGraph graph;

    /**  Flag to indicate if the element is paired to a document in the DB. */

    @Expose(serialize = false, deserialize = false)
    protected boolean paired = false;

    /**
     * Constructor used for Arango DB JavaBeans de-/serialisation..
     */
    public ArangoDBBaseDocument() {
        super();
    }

    /**
     * Instantiates a new Arango DB base document. The document's collection is assigned by requesting the graph to
     * prefix the collection.
     *
     * @param key 			        the key to assign to the docuement
     * @param label                 the document label
     * @param graph                 the graph that contains the document
     */

    public ArangoDBBaseDocument(String key, String label, ArangoDBGraph graph) {
        this._key = key;
        this.label = label;
        this.graph = graph;
        this.collection = graph.getPrefixedCollectioName(label);
    }

    /**
     * Get the Document's ArangoDB Id.
     *
     * @return the id
     */

    public String _id() {
        return _id;
    }

    /**
     * Set the Document's ArangoDB Id.
     * This method is not for public use as ids must be final. It is only provided to allow the deserialization to
     * assign the value.
     *
     * @param id                    the id
     */

    public void _id(String id) {
        this._id = id;
    }

    /**
     * Get the Document's ArangoDB Key.
     *
     * @return the name
     */

    public String _key() {
        return _key;
    }

    /**
     * Set the Document's ArangoDB Key.
     * This method is only provided to allow the deserialization to assign the value.
     *
     * @param key                   the key
     */

    protected void _key(String key) {
        this._key = key;
    }

    /**
     * Get the Document's ArangoDB Revision.
     *
     * @return the revision
     */

    public String _rev() {
        return _rev;
    }

    /**
     * Set the Document's ArangoDB Revision.
     * This method is only provided to allow the deserialization to assign the value.
     *
     * @param revision              the revision
     */

    protected void _rev(String revision) {
        this._rev = revision;
    }

    /**
     * Get the document's collection.
     *
     * @return the collection
     */

    public String collection() {
        return collection;
    }

    /**
     * Set the Documents collection.
     * This method is only provided to allow the deserialization to assign the value.
     *
     * @param collection            the collection
     */

    protected void collection(String collection) {
        this.collection = collection;
    }

    /**
     * The graph in which the document is contained.
     *
     * @return the Arango DB graph
     */

    public ArangoDBGraph graph() {
	    return graph;
	}

    /**
     * Set the document's graph
     *
     * @param graph the graph
     */

    public void graph(ArangoDBGraph graph) {
        this.graph = graph;
    }


    /**
     * Checks if the document is paired.
     *
     * @return true, if is paired
     */

    public boolean isPaired() {
        return paired;
    }

    /**
     * Sets the paired value of the document.
     *
     * @param paired                the new paired status
     */

    public void setPaired(boolean paired) {
        this.paired = paired;
    }
    
}
