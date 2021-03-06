/*
 * NGCCHandler.java
 *
 * Created on 2001/08/05, 11:54
 */

package relaxngcc.runtime;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Base class for classes generated by RelaxNGCC for typed-sax and plain-sax modes.  
 */
public abstract class NGCCPlainHandler implements ContentHandler
{
    protected XMLReader _ngcc_reader; //XML data source
    protected NGCCPlainHandler _ngcc_parent; //handler to be recovered at the end of current automaton 
    protected Locator _locator;
    
    protected boolean _tokenizeText;
    protected Stack _attrStack; //a stack of attribute set
    
    private String _accumulated_text = new String();
    
    protected NGCCPlainHandler(XMLReader reader)
    {
        this(reader,null); //with no parent handler
    }
    protected NGCCPlainHandler(XMLReader reader,NGCCPlainHandler parent)
    {
        _ngcc_reader = reader;
        _ngcc_parent = parent;
        _attrStack = new Stack();
        initState();
    }        
        
    
    protected AttributesImpl currentAttrs() { return (AttributesImpl)_attrStack.peek(); }
    
    //main handler. the classes generated by RelaxNGCC overrides these methods.
    public abstract void enterElement(String uri, String localName, String qname) throws SAXException;
    public abstract void leaveElement(String uri, String localName, String qname) throws SAXException;
    public abstract void text(String value) throws SAXException;
    public abstract void processAttribute() throws SAXException;
    public abstract boolean accepted();
    protected abstract void initState();
    
    public void startDocument() {}
    public void endDocument() throws SAXException
    {
        if(!accepted()) throw new SAXException("Unexpected end of document");
    }
    public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException
    {
        if(_accumulated_text.length()>0) consumeText(_accumulated_text);
        _attrStack.push(new AttributesImpl(atts));
        enterElement(uri, localname, qname);
    }
    public void endElement(String uri, String localname, String qname) throws SAXException
    {
        if(_accumulated_text.length()>0) consumeText(_accumulated_text);
        if(!_attrStack.empty()) _attrStack.pop();
        leaveElement(uri, localname, qname);
    }
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        _accumulated_text += new String(ch, start, length);
    }
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {}
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {}
    public void endPrefixMapping(String prefix) throws SAXException
    {}
    public void setDocumentLocator(Locator loc)
    { _locator=loc; }
    public void processingInstruction(String target, String data) throws SAXException
    {}
    public void skippedEntity(String name) throws SAXException
    {}
    
    private void consumeText(String txt) throws SAXException
    {
        if(_tokenizeText)
        {
            StringTokenizer t = new StringTokenizer(txt, " \t\r\n");
            while(t.hasMoreTokens()) text(t.nextToken());
        }
        else
            text(txt);
        
        _accumulated_text = "";
    }
    
    protected Locator getLocator() { return _locator; }
    
    protected int getAttributeIndex(String uri, String localname)
    {
        return currentAttrs().getIndex(uri, localname);
    }
    protected void consumeAttribute(int index) throws SAXException
    {
        String value = currentAttrs().getValue(index);
        currentAttrs().removeAttribute(index);
        consumeText(value);
    }

    //initialize a new child handler with startElement event.
    protected void setupNewHandler(NGCCPlainHandler h, String uri, String localname, String qname) throws SAXException
    {
        _ngcc_reader.setContentHandler(h);
        h.setDocumentLocator(_locator);
        h.startElement(uri,localname,qname,currentAttrs());
        _attrStack.pop();
        h.processAttribute();
    }
    //initialize a new child handler with attribute event.
    protected void setupNewHandler(NGCCPlainHandler h) throws SAXException
    {
        _ngcc_reader.setContentHandler(h);
        h._tokenizeText = _tokenizeText;
        h._accumulated_text = _accumulated_text;
        h.setDocumentLocator(_locator);
        h._attrStack.push(currentAttrs());
        h.processAttribute();
    }
    
    protected void resetHandlerByAttr() throws SAXException
    {
        _ngcc_reader.setContentHandler(_ngcc_parent);
        _ngcc_parent.processAttribute();
    }
    protected void resetHandlerByStart(String uri, String localname, String qname) throws SAXException
    {
        _ngcc_reader.setContentHandler(_ngcc_parent);
        _ngcc_parent.startElement(uri, localname, qname, currentAttrs());
    }
    protected void resetHandlerByEnd(String uri, String localname, String qname) throws SAXException
    {
        _ngcc_reader.setContentHandler(_ngcc_parent);
        _ngcc_parent.processAttribute();
        _ngcc_parent.endElement(uri, localname, qname);
    }
    
    protected void throwUnexpectedElementException(String qname) throws SAXException
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Unexpected element [");
        buf.append(qname);
        buf.append("] appears.(Line");
        buf.append(getLocator().getLineNumber());
        buf.append(",Column");
        buf.append(getLocator().getColumnNumber());
        buf.append(")");
        
        throw new SAXException(buf.toString());
    } 
    
}
