/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.oxm.mappings.nullpolicy;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.oxm.NullCapableValue;
import org.eclipse.persistence.internal.oxm.XPathEngine;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.XPathNode;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.record.MarshalRecord;
import org.eclipse.persistence.oxm.record.XMLRecord;
import org.eclipse.persistence.sessions.Session;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * PUBLIC: <b>Description</b>: This node null policy allows for the handling of
 * various representations of null in XML documents.<br>
 * <p>
 * <b>Null policies have 2 concrete implementations</b>:
 * <ul>
 * <li> NullPolicy (default implementation)</li>
 * <li> IsSetNullPolicy (keyed off isSet() state of the node)</li>
 * </ul>
 * <p>
 * <p>
 * <table border="1">
 * <tr>
 * <th id="c1" align="left">Unmarshal Flag</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1"> isSetPerformedForAbsentNode </td>
 * <td headers="c2">This umarshal flag represents whether a set is done for
 * absent nodes only.</td>
 * </tr>
 * <tr>
 * <td headers="c1"> isNullRepresentedByEmptyNode </td>
 * <td headers="c2">If this unmarshal flag is false for empty nodes we set an
 * empty Object for composite mappings, otherwise we set to null.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true"> isNullRepresentedByXsiNil </td>
 * <td headers="c2">If this unmarshal flag is false for xsi:nil nodes we ignore
 * the xsi:nil attribute and treat as an empty node.<br>
 * Otherwise we set to null.</td>
 * </tr>
 * </table>
 * <p>
 * <table border="1">
 * <tr>
 * <th id="c1" align="left">Marshal Enum</th>
 * <th id="c2" align="left">XMLNullRepresentationType Description</th>
 * </tr>
 * <td headers="c1"> XSI_NIL </td>
 * <td headers="c2">Nillable: Write out an xsi:nil="true" attribute.</td>
 * </tr>
 * <tr>
 * <td headers="c1"> ABSENT_NODE(default) </td>
 * <td headers="c2">Optional: Write out no node.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true"> EMPTY_NODE </td>
 * <td headers="c2">Required: Write out an empty <node/> or node="" node.</td>
 * </tr>
 * </table> &nbsp;<b>Usage</b>:<br>
 * 
 * @see org.eclipse.persistence.internal.oxm.NullCapableValue
 * @since Oracle TopLink 11<i>g</i> Release 1 (11.1.1) 
 */
public abstract class AbstractNullPolicy {
    protected static final String TRUE = "true";
    protected static final String COLON_W_SCHEMA_NIL_ATTRIBUTE = XMLConstants.COLON + XMLConstants.SCHEMA_NIL_ATTRIBUTE;
    protected static final String XSI_NIL_ATTRIBUTE = XMLConstants.SCHEMA_INSTANCE_PREFIX + COLON_W_SCHEMA_NIL_ATTRIBUTE;

    /**
     * This state flag determines how we unmarshal absent nodes. true =
     * (default) Perform a set(null). false = Do not perform a set(null).
     */
    protected boolean isSetPerformedForAbsentNode = true;

    /**
     * This state flag determines how we unmarshal empty nodes. true = Perform a
     * set(null) or primitive type equivalent. false = (default) Perform a
     * set(new Object()).
     */
    protected boolean isNullRepresentedByEmptyNode = false;

    /**
     * This state flag determines how we unmarshal xsi:nil nodes. A set is
     * performed in both cases. true = Perform a set(null) or primitive type
     * equivalent.. false = (default) do nothing and treat as an empty node.
     */
    protected boolean isNullRepresentedByXsiNil = false;

    /**
     * This enum instance determines what to write out during a marshal
     * operation. We are defaulting to ABSENT_NODE
     */
    protected XMLNullRepresentationType marshalNullRepresentation = XMLNullRepresentationType.ABSENT_NODE;

    /**
     * Get the enum that determines what XML to write when a null value is encountered.
     * 
     * @param enumValue
     */
    public XMLNullRepresentationType getMarshalNullRepresentation() {
        return marshalNullRepresentation;

    }

    /**
     * Set the enum that determines what XML to write when a null value is encountered.
     * 
     * @param enumValue
     */
    public void setMarshalNullRepresentation(XMLNullRepresentationType anEnumInstance) {
        marshalNullRepresentation = anEnumInstance;
    }

    /**
     * INTERNAL: 
     * When using the SAX or DOM Platform, this method is responsible for
     * marshalling null values for the XML Direct Mapping.
     * 
     * @param xPathFragment
     * @param marshalRecord
     * @param object
     * @param session
     * @param namespaceResolver
     * @return true if this method caused any nodes to be marshaled, else false.
     */
    public boolean directMarshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, //
            Object object, Session session, NamespaceResolver namespaceResolver) {
        // Handle attributes - XSI_NIL, ABSENT_NODE have the same behavior
        if (xPathFragment.isAttribute()) {
            // Write out an empty attribute
            if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.EMPTY_NODE)) {
                XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
                // We mutate the null into an empty string 
                marshalRecord.attribute(xPathFragment, namespaceResolver, XMLConstants.EMPTY_STRING);
                marshalRecord.closeStartGroupingElements(groupingFragment);
                return true;
            } else {
                // XSI_NIL attributes are invalid - and are ignored
                // ABSENT_NODE - Write out nothing
                return false;
            }
        } else {
            // Nillable: write out xsi:nil="true" attribute in empty element    	
            if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.XSI_NIL)) {
                XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
                String xsiPrefix = processNamespaceResolverForXSIPrefix(namespaceResolver, marshalRecord);
                StringBuilder qName = new StringBuilder(XMLConstants.ATTRIBUTE); // Unsynchronized
                qName.append(xsiPrefix).append(COLON_W_SCHEMA_NIL_ATTRIBUTE);
                XPathFragment nilFragment = new XPathFragment(qName.toString());
                nilFragment.setNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
                marshalRecord.attribute(nilFragment, namespaceResolver, TRUE);
                marshalRecord.closeStartGroupingElements(groupingFragment);
                return true;
            } else {
                // EMPTY_NODE - Write out empty element
                if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.EMPTY_NODE)) {
                    XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
                    marshalRecord.closeStartGroupingElements(groupingFragment);
                    return true;
                } else {
                    // ABSENT_NODE - Write out nothing
                    return false;
                }
            }
        }
    }

    /**
     * INTERNAL: When using the SAX Platform, this method is responsible for
     * marshalling null values for the XML Composite Object Mapping.
     * 
     * @param xPathFragment
     * @param marshalRecord
     * @param object
     * @param session
     * @param namespaceResolver
     * @return true if this method caused any nodes to be marshaled, else false.
     */
    public boolean compositeObjectMarshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, //
            Object object, Session session, NamespaceResolver namespaceResolver) {
        // Nillable
        if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.XSI_NIL)) {
            XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
            marshalRecord.closeStartGroupingElements(groupingFragment);
            marshalRecord.openStartElement(xPathFragment, namespaceResolver);
            String xsiPrefix = processNamespaceResolverForXSIPrefix(namespaceResolver, marshalRecord);
            XPathFragment nilFragment = new XPathFragment(XMLConstants.ATTRIBUTE + xsiPrefix + COLON_W_SCHEMA_NIL_ATTRIBUTE);
            nilFragment.setNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
            marshalRecord.attribute(nilFragment, namespaceResolver, TRUE);
            marshalRecord.closeStartElement();
            marshalRecord.endElement(xPathFragment, namespaceResolver);
            return true;
        } else {
            // Optional and Required
            // This call is really only valid when using DOM - TBD false
            // Write out empty element - we need to differentiate between
            // object=null and object=new Object() with null fields and 0-numeric primitive values
            // EMPTY_NODE - Write out empty element - Required
            if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.EMPTY_NODE)) {
                XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
                marshalRecord.closeStartGroupingElements(groupingFragment);
                marshalRecord.openStartElement(xPathFragment, namespaceResolver);
                marshalRecord.closeStartElement();
                marshalRecord.endElement(xPathFragment, namespaceResolver);
                return true;
            } else {
                // ABSENT_NODE - Write out nothing - Optional
                return false;
            }
        }
    }

    /**
     * INTERNAL: When using the DOM Platform, this method is responsible for
     * marshalling null values for the XML Composite Object Mapping.
     * 
     * @param record
     * @param object
     * @param field
     * @return true if this method caused any objects to be marshaled, else false.
     */
    public boolean compositeObjectMarshal(XMLRecord record, Object object, XMLField field, AbstractSession session) {
        if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.XSI_NIL)) {
            Node root = record.getDOM();
            Element nested = (Element) XPathEngine.getInstance().create(field, root, session);
            nested.setAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, XSI_NIL_ATTRIBUTE, TRUE);
            return true;
        } else {
            // EMPTY_NODE - Write out empty element - Required
            if (getMarshalNullRepresentation().equals(XMLNullRepresentationType.EMPTY_NODE)) {
                Node root = record.getDOM();
                XPathEngine.getInstance().create(field, root, session);
                return true;
            } else {
                // ABSENT_NODE - Write out nothing - Optional
                return false;
            }
        }
    }

    /**
     * INTERNAL: When using the SAX or DOM Platform during unmarshal operations. 
     * Use the attributes to determine if the element represents a null value.
     * 
     * @param attributes
     * @return true if based on the attributes the corresponding element
     *         represents a null value, else false.
     */
    public boolean valueIsNull(Attributes attributes) {
        // Nillable 
        if (isNullRepresentedByXsiNil()) {
            // Ignore any other attributes that are in addition to xsi:nil
            if(null == attributes) {
                return false;
            }
            int index = attributes.getIndex(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_NIL_ATTRIBUTE);
            if (index >= 0) {
                return true;
            }
        } else {
            // EMPTY_NODE - Required
            if (isNullRepresentedByEmptyNode() && (null == attributes || attributes.getLength() == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL: When using the DOM Platform during unmarshal operations. 
     * Use the element to determine if the element represents a null value.
     * 
     * @ param element
     * @return true if based on the element it represents a null value, else false.
     */
    public boolean valueIsNull(Element element) {
        // Check Nillable: Ignore any other attributes that are in addition to xsi:nil
        if (null == element) {
            return true;
        } else {
            if (isNullRepresentedByXsiNil() && element.hasAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_NIL_ATTRIBUTE)) {
                return true;
            } else {
                // EMPTY_NODE - Required
                // Verify no attributes and no child nodes on the DOM element
                if (isNullRepresentedByEmptyNode() && !element.hasAttributes() && (element.getChildNodes().getLength() == 0)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * INTERNAL: When using the SAX Platform this allows a NodeValue to be
     * registered to receive events from the TreeObjectBuilder.
     * @param xPathNode
     * @param nullCapableValue
     */
    public abstract void xPathNode(XPathNode xPathNode, NullCapableValue nullCapableValue);

    /**
     * @return the isSetPerformedForAbsentNode flag
     */
    public boolean getIsSetPerformedForAbsentNode() {
        return isSetPerformedForAbsentNode;
    }

    /**
     * 
     * @return
     */
    public boolean isNullRepresentedByEmptyNode() {
        return isNullRepresentedByEmptyNode;
    }

    /**
     * 
     * @param bIsNullRepresentedByEmptyNode
     */
    public void setNullRepresentedByEmptyNode(boolean bisNullRepresentedByEmptyNode) {
        isNullRepresentedByEmptyNode = bisNullRepresentedByEmptyNode;
    }

    /**
     * 
     * @return
     */
    public boolean isNullRepresentedByXsiNil() {
        return isNullRepresentedByXsiNil;
    }

    /**
     * 
     * @param isNullRepresentedByXsiNil
     */
    public void setNullRepresentedByXsiNil(boolean bIsNullRepresentedByXsiNil) {
        isNullRepresentedByXsiNil = bIsNullRepresentedByXsiNil;
    }

    /**
     * INTERNAL:
     * Private function to process or create an entry in the NamespaceResolver for the xsi prefix.
     * @param namespaceResolver
     * @return xsi prefix
     */
    protected String processNamespaceResolverForXSIPrefix(NamespaceResolver namespaceResolver, MarshalRecord marshalRecord) {
        String xsiPrefix;
        if (null == namespaceResolver) {
            // add new xsi entry into the properties map
            xsiPrefix = XMLConstants.SCHEMA_INSTANCE_PREFIX;
            namespaceResolver = new NamespaceResolver();
            namespaceResolver.put(xsiPrefix, XMLConstants.SCHEMA_INSTANCE_URL);
            marshalRecord.attribute(XMLConstants.XMLNS_URL, xsiPrefix, XMLConstants.XMLNS + XMLConstants.COLON + xsiPrefix, XMLConstants.SCHEMA_INSTANCE_URL);
        } else {
            // find an existing xsi entry in the map
            xsiPrefix = namespaceResolver.resolveNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
            if (null == xsiPrefix) {
                xsiPrefix = namespaceResolver.generatePrefix(XMLConstants.SCHEMA_INSTANCE_PREFIX);
                marshalRecord.attribute(XMLConstants.XMLNS_URL, xsiPrefix, XMLConstants.XMLNS + XMLConstants.COLON + xsiPrefix, XMLConstants.SCHEMA_INSTANCE_URL);
            }
        }
        return xsiPrefix;
    }
    
    public void directMarshal(DatabaseField field, XMLRecord record, Object object) {
        Object fieldValue = null;
        if(getMarshalNullRepresentation() == XMLNullRepresentationType.EMPTY_NODE) {
            fieldValue = XMLConstants.EMPTY_STRING;
        } else {
            if(!(((XMLField)field).getLastXPathFragment().isAttribute())) {
                if(getMarshalNullRepresentation() == XMLNullRepresentationType.XSI_NIL) {
                    fieldValue = XMLRecord.NIL;
                }
            }
        }
        record.put(field, fieldValue);
    }
}