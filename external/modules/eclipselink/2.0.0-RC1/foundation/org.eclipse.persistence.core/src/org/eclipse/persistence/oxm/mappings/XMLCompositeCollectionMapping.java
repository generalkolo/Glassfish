/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
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
package org.eclipse.persistence.oxm.mappings;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.internal.oxm.XMLConversionManager;
import org.eclipse.persistence.internal.oxm.XPathEngine;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.internal.oxm.XMLObjectBuilder;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.queries.MapContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.mappings.foundation.AbstractCompositeCollectionMapping;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.converters.XMLConverter;
import org.eclipse.persistence.oxm.mappings.nullpolicy.AbstractNullPolicy;
import org.eclipse.persistence.oxm.mappings.nullpolicy.NullPolicy;
import org.eclipse.persistence.oxm.record.DOMRecord;
import org.eclipse.persistence.oxm.record.XMLRecord;
import org.eclipse.persistence.oxm.schema.XMLSchemaReference;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.queries.ObjectBuildingQuery;

/**
 * <p>Composite collection XML mappings map an attribute that contains a homogeneous collection of objects
 * to multiple XML elements.  Use composite collection XML mappings to represent one-to-many relationships.
 * Composite collection XML mappings can reference any class that has a TopLink descriptor. The attribute in
 * the object mapped must implement either the Java Collection interface (for example, Vector or HashSet)
 * or Map interface (for example, Hashtable or TreeMap). The CompositeCollectionMapping class
 * allows a reference to the mapped class and the indexing type for that class.  This mapping is, by
 * definition, privately owned.
 *
 * <p><b>Setting the XPath</b>: TopLink XML mappings make use of XPath statements to find the relevant
 * data in an XML document.  The XPath statement is relative to the context node specified in the descriptor.
 * The XPath may contain path and positional information;  the last node in the XPath forms the local
 * root node for the composite object.  The XPath is specified on the mapping using the <code>setXPath</code>
 * method.
 *
 * <p>The following XPath statements may be used to specify the location of XML data relating to an object's
 * name attribute:
 *
 * <p><table border="1">
 * <tr>
 * <th id="c1" align="left">XPath</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1">phone-number</td>
 * <td headers="c2">The phone number information is stored in the phone-number element.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true">contact-info/phone-number</td>
 * <td headers="c2">The XPath statement may be used to specify any valid path.</td>
 * </tr>
 * <tr>
 * <td headers="c1">phone-number[2]</td>
 * <td headers="c2">The XPath statement may contain positional information.  In this case the phone
 * number information is stored in the second occurrence of the phone-number element.</td>
 * </tr>
 * </table>
 *
 * <p><b>Mapping a Composite Collection</b>:
 *
 * <!--
 * <?xml version="1.0" encoding="UTF-8"?>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
 *     <xsd:element name="customer" type="customer-type"/>
 *     <xsd:complexType name="customer-type">
 *         <xsd:sequence>
 *             <xsd:element name="first-name" type="xsd:string"/>
 *             <xsd:element name="last-name" type="xsd:string"/>
 *             <xsd:element name="phone-number">
 *                 <xsd:complexType>
 *                     <xsd:sequence>
 *                         <xsd:element name="number" type="xsd:string"/>
 *                     </xsd:sequence>
 *                     <xsd:attribute name="type" type="xsd:string"/>
 *                 </xsd:complexType>
 *             </xsd:element>
 *         </xsd:sequence>
 *     </xsd:complexType>
 * </xsd:schema>
 * -->
 *
 * <p><em>XML Schema</em><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:element name="customer" type="customer-type"/&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:complexType name="customer-type"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="first-name" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="last-name" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="phone-number"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:complexType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="number" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:attribute name="type" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:element&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &lt;/xsd:schema&gt;<br>
 * </code>
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * XMLCompositeCollectionMapping phoneNumbersMapping = new XMLCompositeCollectionMapping();<br>
 * phoneNumbersMapping.setAttributeName("phoneNumbers");<br>
 * phoneNumbersMapping.setXPath("phone-number");<br>
 * phoneNumbersMapping.setReferenceClass(PhoneNumber.class);<br>
 * </code>
 *
 * <p><b>More Information</b>: For more information about using the XML Composite Collection Mapping, see the
 * "Understanding XML Mappings" chapter of the Oracle TopLink Developer's Guide.
 *
 * @since Oracle TopLink 10<i>g</i> Release 2 (10.1.3)
 */
public class XMLCompositeCollectionMapping extends AbstractCompositeCollectionMapping implements XMLMapping, XMLNillableMapping {
    AbstractNullPolicy nullPolicy;
    private UnmarshalKeepAsElementPolicy keepAsElementPolicy;
    private XMLInverseReferenceMapping inverseReferenceMapping;

    private boolean isWriteOnly;
    private boolean reuseContainer = false;

    public XMLCompositeCollectionMapping() {
        super();
        // The default policy is NullPolicy
        nullPolicy = new NullPolicy();
    }

    /**
     * Gets the AttributeAccessor that is used to get and set the value of the
     * container on the target object.
     * @deprecated Replaced by getInverseReferenceMapping().getAttributeAccessor()
     */
    @Deprecated
    public AttributeAccessor getContainerAccessor() {
        if (this.inverseReferenceMapping == null) {
            return null;
        }
        return this.inverseReferenceMapping.getAttributeAccessor();
    }

    /**
     * Sets the AttributeAccessor that is used to get and set the value of the
     * container on the target object.
     *
     * @param anAttributeAccessor - the accessor to be used.
     * @deprecated Replaced by getInverseReferenceMapping().setAttributeAccessor()
     */
    @Deprecated
    public void setContainerAccessor(AttributeAccessor anAttributeAccessor) {
        if (this.inverseReferenceMapping == null) {
            return;
        }
        this.inverseReferenceMapping.setAttributeAccessor(anAttributeAccessor);
    }

    /**
     * Sets the name of the backpointer attribute on the target object. Used to
     * populate the backpointer. If the specified attribute doesn't exist on the
     * reference class of this mapping, a DescriptorException will be thrown
     * during initialize.
     *
     * @param attributeName - the name of the backpointer attribute to be populated
     * @deprecated Replaced by getInverseReferenceMapping().setAttributeName()
     */
    @Deprecated
    public void setContainerAttributeName(String attributeName) {
        if (this.inverseReferenceMapping == null) {
            return;
        }    	
        this.inverseReferenceMapping.setAttributeName(attributeName);
    }

    /**
     * Gets the name of the backpointer attribute on the target object.
     * @deprecated Replaced by getInverseReferenceMapping().getAttributeName()
     */
    @Deprecated
    public String getContainerAttributeName() {
        if (this.inverseReferenceMapping == null) {
            return null;
        }
        return this.inverseReferenceMapping.getAttributeName();
    }

    /**
     * Sets the method name to be used when accessing the value of the back pointer
     * on the target object of this mapping. If the specified method isn't declared
     * on the reference class of this mapping, a DescriptorException will be thrown
     * during initialize.
     *
     * @param methodName - the name of the getter method to be used.
     * @deprecated Replaced by getInverseReferenceMapping().setGetMethodName()
     */
    @Deprecated
    public void setContainerGetMethodName(String methodName) {
        if (this.inverseReferenceMapping == null) {
            return;
        }
        this.inverseReferenceMapping.setGetMethodName(methodName);
    }

    /**
     * Gets the name of the method to be used when accessing the value of the
     * back pointer on the target object of this mapping.
     * @deprecated Replaced by getInverseReferenceMapping().getGetMethodName()
     */
    @Deprecated
    public String getContainerGetMethodName() {
        if (this.inverseReferenceMapping == null) {
            return null;
        }
        return this.inverseReferenceMapping.getGetMethodName();
    }

    /**
     * Gets the name of the method to be used when setting the value of the
     * back pointer on the target object of this mapping.
     * @deprecated Replaced by getInverseReferenceMapping().getSetMethodName()
     */
    @Deprecated
    public String getContainerSetMethodName() {
        if (this.inverseReferenceMapping == null) {
            return null;
        }
        return this.inverseReferenceMapping.getSetMethodName();
    }

    /**
     * Sets the name of the method to be used when setting the value of the back pointer
     * on the target object of this mapping. If the specified method isn't declared on
     * the reference class of this mapping, a DescriptorException will be
     * raised during initialize.
     *
     * @param methodName - the name of the setter method to be used.
     * @deprecated Replaced by getInverseReferenceMapping().setSetMethodName()
     */
    @Deprecated
    public void setContainerSetMethodName(String methodName) {
        if (this.inverseReferenceMapping == null) {
            return;
        }
        this.inverseReferenceMapping.setSetMethodName(methodName);
    }

    /**
     * INTERNAL:
     */
    public boolean isXMLMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * The mapping is initialized with the given session. This mapping is fully initialized
     * after this.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        //modified so that reference class on composite mappings is no longer mandatory
        if ((getReferenceClass() == null) && (getReferenceClassName() != null)) {
            setReferenceClass(session.getDatasourcePlatform().getConversionManager().convertClassNameToClass(getReferenceClassName()));
        }
        if (getReferenceClass() != null) {
            super.initialize(session);
        } else {
            //below should be the same as AbstractCompositeCollectionMapping.initialize
            if (getField() == null) {
                throw DescriptorException.fieldNameNotSetInMapping(this);
            }
            setField(getDescriptor().buildField(getField()));
            setFields(collectFields());
            if (hasConverter()) {
                getConverter().initialize(this, session);
            }
        }

        ContainerPolicy cp = getContainerPolicy();
        if (cp != null) {
            if (cp.getContainerClass() == null) {
                Class cls = session.getDatasourcePlatform().getConversionManager().convertClassNameToClass(cp.getContainerClassName());
                cp.setContainerClass(cls);
            }
            if (cp instanceof MapContainerPolicy) {
                ((MapContainerPolicy) cp).setElementClass(this.getReferenceClass());
            }
        }
        if(null != getContainerAccessor()) {
            getContainerAccessor().initializeAttributes(this.referenceClass);
        }

    }

    /**
     * Get the XPath String
     * @return String the XPath String associated with this Mapping     *
     */
    public String getXPath() {
        return getField().getName();
    }

    /**
     * Set the Mapping field name attribute to the given XPath String
     *
     * @param xpathString String
     *
     */
    public void setXPath(String xpathString) {
        this.setField(new XMLField(xpathString));
    }

    protected Object buildCompositeObject(ClassDescriptor descriptor, AbstractRecord nestedRow, ObjectBuildingQuery query, JoinedAttributeManager joinManger) {
        return descriptor.getObjectBuilder().buildObject(query, nestedRow, joinManger);
    }

    protected AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord parentRow) {
        ClassDescriptor classDesc = null;
        try{
            classDesc = getReferenceDescriptor(attributeValue, session);
        }catch(Exception e){
            //do nothing
        }
        XMLField xmlFld = (XMLField) getField();
        if (xmlFld.hasLastXPathFragment() && xmlFld.getLastXPathFragment().hasLeafElementType()) {
            XMLRecord xmlRec = (XMLRecord) parentRow;
            xmlRec.setLeafElementType(xmlFld.getLastXPathFragment().getLeafElementType());
        }
        XMLRecord parent = (XMLRecord) parentRow;

        if (classDesc != null) {
            XMLObjectBuilder objectBuilder = (XMLObjectBuilder) classDesc.getObjectBuilder();

            boolean addXsiType = shouldAddXsiType((XMLRecord) parentRow, classDesc);
            XMLRecord child = (XMLRecord) objectBuilder.createRecordFor(attributeValue, (XMLField) getField(), parent, this);
            child.setNamespaceResolver(parent.getNamespaceResolver());
            objectBuilder.buildIntoNestedRow(child, attributeValue, session, addXsiType);
            return child;
        } else {
            if (attributeValue instanceof Element && getKeepAsElementPolicy() == UnmarshalKeepAsElementPolicy.KEEP_UNKNOWN_AS_ELEMENT) {
                return new DOMRecord((Element) attributeValue);
            }else{
                Node newNode = XPathEngine.getInstance().create((XMLField)getField(), parent.getDOM(), attributeValue, session);
                DOMRecord newRow = new DOMRecord(newNode);
                return newRow;
            }
        }
    }

    /**
     * INTERNAL:
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }

        Object attributeValue = this.getAttributeValueFromObject(object);
        if (attributeValue == null) {
            row.put(this.getField(), null);
            return;
        }

        ContainerPolicy cp = this.getContainerPolicy();

        Vector nestedRows = new Vector(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object element = cp.next(iter, session);
            // convert the value - if necessary
            if (hasConverter()) {
                if (getConverter() instanceof XMLConverter) {
                    element = ((XMLConverter) getConverter()).convertObjectValueToDataValue(element, session, ((XMLRecord) row).getMarshaller());
                } else {
                    element = getConverter().convertObjectValueToDataValue(element, session);
                }
            }
            nestedRows.addElement(buildCompositeRow(element, session, row));
        }

        Object fieldValue = null;
        if (!nestedRows.isEmpty()) {
            fieldValue = this.getDescriptor().buildFieldValueFromNestedRows(nestedRows, getStructureName(), session);
        }
        row.put(this.getField(), fieldValue);
    }

    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ContainerPolicy cp = this.getContainerPolicy();

        Object fieldValue = row.getValues(this.getField());

        // BUG#2667762 there could be whitespace in the row instead of null
        if ((fieldValue == null) || (fieldValue instanceof String)) {
            if (reuseContainer) {
                Object currentObject = ((XMLRecord) row).getCurrentObject();
                Object container = getAttributeAccessor().getAttributeValueFromObject(currentObject);
                return container != null ? container : cp.containerInstance();
            } else {
                return cp.containerInstance();
            }
        }

        Vector nestedRows = this.getDescriptor().buildNestedRowsFromFieldValue(fieldValue, executionSession);
        if (nestedRows == null) {
            if (reuseContainer) {
                Object currentObject = ((XMLRecord) row).getCurrentObject();
                Object container = getAttributeAccessor().getAttributeValueFromObject(currentObject);
                return container != null ? container : cp.containerInstance();
            } else {
                return cp.containerInstance();
            }
        }

        Object result = null;
        if (reuseContainer) {
            Object currentObject = ((XMLRecord) row).getCurrentObject();
            Object container = getAttributeAccessor().getAttributeValueFromObject(currentObject);
            result = container != null ? container : cp.containerInstance();
        } else {
            result = cp.containerInstance(nestedRows.size());
        }

        for (Enumeration stream = nestedRows.elements(); stream.hasMoreElements();) {
            AbstractRecord nestedRow = (AbstractRecord) stream.nextElement();
            Object objectToAdd = buildObjectFromNestedRow(nestedRow, joinManager, sourceQuery, executionSession);
            cp.addInto(objectToAdd, result, sourceQuery.getSession());
            if(null != getContainerAccessor()) {
                Object currentObject = ((XMLRecord)row).getCurrentObject();
                if(this.inverseReferenceMapping.getContainerPolicy() == null) {
                    getContainerAccessor().setAttributeValueInObject(objectToAdd, currentObject);
                } else {
                    Object backpointerContainer = getContainerAccessor().getAttributeValueFromObject(objectToAdd);
                    if(backpointerContainer == null) {
                        backpointerContainer = this.inverseReferenceMapping.getContainerPolicy().containerInstance();
                        getContainerAccessor().setAttributeValueInObject(objectToAdd, backpointerContainer);
                    }
                    this.inverseReferenceMapping.getContainerPolicy().addInto(currentObject, backpointerContainer, executionSession);
                }
            }
        }
        return result;
    }

    public Object buildObjectFromNestedRow(AbstractRecord nestedRow, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) {
        Object objectToAdd = null;
        ClassDescriptor aDescriptor = getReferenceDescriptor((DOMRecord) nestedRow);
        
        if(aDescriptor == null){    
            if ((getKeepAsElementPolicy() == UnmarshalKeepAsElementPolicy.KEEP_UNKNOWN_AS_ELEMENT) || (getKeepAsElementPolicy() == UnmarshalKeepAsElementPolicy.KEEP_ALL_AS_ELEMENT)) {
                XMLPlatformFactory.getInstance().getXMLPlatform().namespaceQualifyFragment((Element) ((DOMRecord)nestedRow).getDOM());
                objectToAdd = ((DOMRecord)nestedRow).getDOM();
                if (getConverter() != null) {     
                    if (getConverter() instanceof XMLConverter) {
                        objectToAdd = ((XMLConverter) getConverter()).convertDataValueToObjectValue(objectToAdd, executionSession, ((XMLRecord) nestedRow).getUnmarshaller());
                    } else {
                        objectToAdd = getConverter().convertDataValueToObjectValue(objectToAdd, executionSession);
                    }
                }
                //simple case
                objectToAdd = convertToSimpleTypeIfPresent(objectToAdd, nestedRow,executionSession);
            } else {
                NodeList children =((Element) ((DOMRecord)nestedRow).getDOM()).getChildNodes();
                for(int i=0; i< children.getLength(); i++){
                    Node nextNode = children.item(i);
                    if(nextNode.getNodeType() == nextNode.ELEMENT_NODE){
                        //complex child
                        throw XMLMarshalException.noDescriptorFound(this);                              
                    }
                }
                 //simple case
                 objectToAdd = convertToSimpleTypeIfPresent(objectToAdd, nestedRow,executionSession);       
            }
        }
        else{
            if (aDescriptor.hasInheritance()) {
                Class newElementClass = aDescriptor.getInheritancePolicy().classFromRow(nestedRow, executionSession);
                if (newElementClass == null) {
                    // no xsi:type attribute - look for type indicator on the field
                    QName leafElementType = ((XMLField) getField()).getLeafElementType();
                    if (leafElementType != null) {
                        Object indicator = aDescriptor.getInheritancePolicy().getClassIndicatorMapping().get(leafElementType);
                        // if the inheritance policy does not contain the user-set type, throw an exception
                        if (indicator == null) {
                            throw DescriptorException.missingClassForIndicatorFieldValue(leafElementType, aDescriptor.getInheritancePolicy().getDescriptor());
                        }
                        newElementClass = (Class) indicator;
                    }
                }
                if (newElementClass != null) {
                    aDescriptor = this.getReferenceDescriptor(newElementClass, executionSession);
                } else {
                    // since there is no xsi:type attribute or leaf element type set, 
                    // use the reference descriptor -  make sure it is non-abstract
                    if (Modifier.isAbstract(aDescriptor.getJavaClass().getModifiers())) {
                        // throw an exception
                        throw DescriptorException.missingClassIndicatorField(nestedRow, aDescriptor.getInheritancePolicy().getDescriptor());
                    }
                }
            }

            //Object element 
            objectToAdd = buildCompositeObject(aDescriptor, nestedRow, sourceQuery, joinManager);
            if (hasConverter()) {
                if (getConverter() instanceof XMLConverter) {
                    objectToAdd = ((XMLConverter) getConverter()).convertDataValueToObjectValue(objectToAdd, executionSession, ((XMLRecord) nestedRow).getUnmarshaller());
                } else {
                    objectToAdd = getConverter().convertDataValueToObjectValue(objectToAdd, executionSession);
                }
            }
        }
        return objectToAdd;
    }
    private Object convertToSimpleTypeIfPresent(Object objectToAdd, AbstractRecord nestedRow, AbstractSession executionSession){
        String stringValue = null;

        Element theElement = ((Element) ((DOMRecord)nestedRow).getDOM());
        Node textchild = theElement.getFirstChild();

        if ((textchild != null) && (textchild.getNodeType() == Node.TEXT_NODE)) {
            stringValue = ((Text) textchild).getNodeValue();
            if(stringValue != null && getKeepAsElementPolicy() != UnmarshalKeepAsElementPolicy.KEEP_UNKNOWN_AS_ELEMENT && getKeepAsElementPolicy()!=UnmarshalKeepAsElementPolicy.KEEP_ALL_AS_ELEMENT){
                objectToAdd = stringValue;
            }
        }
        if ((stringValue == null) || stringValue.length() == 0 ) {
            return objectToAdd;
        }

        String type = theElement.getAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE);
        if ((null != type) && type.length() > 0) {
            XPathFragment typeFragment = new XPathFragment(type);
            String namespaceURI = ((DOMRecord)nestedRow).resolveNamespacePrefix(typeFragment.getPrefix());
            typeFragment.setNamespaceURI(namespaceURI);
            QName schemaTypeQName = new QName(namespaceURI, typeFragment.getLocalName());
            Class theClass = (Class) XMLConversionManager.getDefaultXMLTypes().get(schemaTypeQName);
            if (theClass != null) {
                objectToAdd = ((XMLConversionManager) executionSession.getDatasourcePlatform().getConversionManager()).convertObject(stringValue, theClass, schemaTypeQName);
            }
        }
        return objectToAdd;
    }

    public ClassDescriptor getReferenceDescriptor(DOMRecord xmlRecord) {
        ClassDescriptor returnDescriptor = referenceDescriptor;

        if (returnDescriptor == null) {
            // Try to find a descriptor based on the schema type
            String type = ((Element) xmlRecord.getDOM()).getAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE);

            if ((null != type) && type.length() >0 ) {
                XPathFragment typeFragment = new XPathFragment(type);
                String namespaceURI = xmlRecord.resolveNamespacePrefix(typeFragment.getPrefix());
                typeFragment.setNamespaceURI(namespaceURI);

                returnDescriptor = xmlRecord.getUnmarshaller().getXMLContext().getDescriptorByGlobalType(typeFragment);

            } else {
                //try leaf element type
                QName leafType = ((XMLField) getField()).getLastXPathFragment().getLeafElementType();
                if (leafType != null) {
                    XPathFragment frag = new XPathFragment();
                    String xpath = leafType.getLocalPart();
                    String uri = leafType.getNamespaceURI();
                    if ((uri != null) && uri.length() > 0) {
                        frag.setNamespaceURI(uri);
                        String prefix = ((XMLDescriptor) getDescriptor()).getNonNullNamespaceResolver().resolveNamespaceURI(uri);
                        if ((prefix != null) && prefix.length() > 0) {
                            xpath = prefix + XMLConstants.COLON + xpath;
                        }
                    }
                    frag.setXPath(xpath);

                    returnDescriptor = xmlRecord.getUnmarshaller().getXMLContext().getDescriptorByGlobalType(frag);
                }
            }
        }

        return returnDescriptor;

    }

    protected ClassDescriptor getReferenceDescriptor(Class theClass, AbstractSession session) {
        if ((getReferenceDescriptor() != null) && getReferenceDescriptor().getJavaClass().equals(theClass)) {
            return getReferenceDescriptor();
        }

        ClassDescriptor subDescriptor = session.getDescriptor(theClass);
        if (subDescriptor == null && getKeepAsElementPolicy() != UnmarshalKeepAsElementPolicy.KEEP_UNKNOWN_AS_ELEMENT) {
            throw DescriptorException.noSubClassMatch(theClass, this);
        } else {
            return subDescriptor;
        }
    }

    /**
     * INTERNAL:
     */
    public boolean shouldAddXsiType(XMLRecord record, ClassDescriptor descriptor) {
        XMLDescriptor xmlDescriptor = (XMLDescriptor) descriptor;
        if ((getReferenceDescriptor() == null) && (xmlDescriptor.getSchemaReference() != null)) {
            if (descriptor.hasInheritance()) {
                XMLField indicatorField = (XMLField) descriptor.getInheritancePolicy().getClassIndicatorField();
                if ((indicatorField.getLastXPathFragment().getNamespaceURI() != null) && indicatorField.getLastXPathFragment().getNamespaceURI().equals(XMLConstants.SCHEMA_INSTANCE_URL)
                        && indicatorField.getLastXPathFragment().getLocalName().equals(XMLConstants.SCHEMA_TYPE_ATTRIBUTE)) {
                    return false;
                }
            }

            XMLSchemaReference xmlRef = xmlDescriptor.getSchemaReference();
            if ((xmlRef.getType() == XMLSchemaReference.COMPLEX_TYPE) && xmlRef.isGlobalDefinition()) {
                QName ctxQName = xmlRef.getSchemaContextAsQName(xmlDescriptor.getNamespaceResolver());
                QName leafType = ((XMLField) getField()).getLeafElementType();

                if ((leafType == null) || (!ctxQName.equals(record.getLeafElementType()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void writeSingleValue(Object value, Object parent, XMLRecord record, AbstractSession session) {
        Object element = value;
        if (hasConverter()) {
            if (getConverter() instanceof XMLConverter) {
                element = ((XMLConverter) getConverter()).convertObjectValueToDataValue(element, session, record.getMarshaller());
            } else {
                element = getConverter().convertObjectValueToDataValue(element, session);
            }
        }
        XMLRecord nestedRow = (XMLRecord) buildCompositeRow(element, session, record);
        record.add(getField(), nestedRow);
    }

    /**
     * Set the AbstractNullPolicy on the mapping<br>
     * The default policy is NullPolicy.<br>
     *
     * @param aNullPolicy
     */
    public void setNullPolicy(AbstractNullPolicy aNullPolicy) {
        nullPolicy = aNullPolicy;
    }

    /**
     * INTERNAL:
     * Get the AbstractNullPolicy from the Mapping.<br>
     * The default policy is NullPolicy.<br>
     * @return
     */
    public AbstractNullPolicy getNullPolicy() {
        return nullPolicy;
    }

    public UnmarshalKeepAsElementPolicy getKeepAsElementPolicy() {
        return keepAsElementPolicy;
    }

    public void setKeepAsElementPolicy(UnmarshalKeepAsElementPolicy keepAsElementPolicy) {
        this.keepAsElementPolicy = keepAsElementPolicy;
    }

    protected XMLDescriptor getDescriptor(XMLRecord xmlRecord, AbstractSession session, QName rootQName) throws XMLMarshalException {
        if (rootQName == null) {
            rootQName = new QName(xmlRecord.getNamespaceURI(), xmlRecord.getLocalName());
        }
        XMLContext xmlContext = xmlRecord.getUnmarshaller().getXMLContext();
        XMLDescriptor xmlDescriptor = xmlContext.getDescriptor(rootQName);
        if (null == xmlDescriptor) {
            if (!((getKeepAsElementPolicy() == UnmarshalKeepAsElementPolicy.KEEP_UNKNOWN_AS_ELEMENT) || (getKeepAsElementPolicy() == UnmarshalKeepAsElementPolicy.KEEP_ALL_AS_ELEMENT))) {
                throw XMLMarshalException.noDescriptorWithMatchingRootElement(xmlRecord.getLocalName());
            }
        }
        return xmlDescriptor;
    }

    public boolean isWriteOnly() {
        return this.isWriteOnly;
    }

    public void setIsWriteOnly(boolean b) {
        this.isWriteOnly = b;
    }

    public void preInitialize(AbstractSession session) throws DescriptorException {
        getAttributeAccessor().setIsWriteOnly(this.isWriteOnly());
        getAttributeAccessor().setIsReadOnly(this.isReadOnly());
        super.preInitialize(session);
    }

    public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        if(isWriteOnly()) {
            return;
        }
        super.setAttributeValueInObject(object, value);
    }

    /**
     * Return true if the original container on the object should be used if
     * present.  If it is not present then the container policy will be used to
     * create the container.
     */
    public boolean getReuseContainer() {
        return reuseContainer;
    }

    /**
     * Specify whether the original container on the object should be used if
     * present.  If it is not present then the container policy will be used to
     * create the container.
     */
    public void setReuseContainer(boolean reuseContainer) {
        this.reuseContainer = reuseContainer;
    }

    public XMLInverseReferenceMapping getInverseReferenceMapping() {
        return inverseReferenceMapping;
    }

    void setInverseReferenceMapping(XMLInverseReferenceMapping inverseReferenceMapping) {
        this.inverseReferenceMapping = inverseReferenceMapping;
    }

}