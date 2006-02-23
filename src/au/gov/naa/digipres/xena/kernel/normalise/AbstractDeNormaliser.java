package au.gov.naa.digipres.xena.kernel.normalise;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * An empty TransformerHandler ready to be overridden to De-normalise Xena files.
 * @author Chris Bitmead.
 */
public abstract class AbstractDeNormaliser implements TransformerHandler {
	protected Result result;

	public void setResult(Result result) throws IllegalArgumentException {
		this.result = result;
	}

	public void setSystemId(String systemID) {
	}

	public String getSystemId() {
		return null;
	}

	public Transformer getTransformer() {
		return null;
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	public void processingInstruction(String target, String data) throws SAXException {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startDTD(String name, String publicId, String systemId) throws SAXException {
	}

	public void endDTD() throws SAXException {
	}

	public void startEntity(String name) throws SAXException {
	}

	public void endEntity(String name) throws SAXException {
	}

	public void startCDATA() throws SAXException {
	}

	public void endCDATA() throws SAXException {
	}

	public void comment(char[] ch, int start, int length) throws SAXException {
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
	}
    

    /**
     * Return a human readable name for this normaliser.
     * @return String
     */
    abstract public String getName();

    public String toString() {
        return getName();
    }
    
}