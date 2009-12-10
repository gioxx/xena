/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
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
/*
 * $Id$
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.Expression;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.xml.sax.SAXException;

/**
 * Implement xsl:value-of.
 * <pre>
 * <!ELEMENT xsl:value-of EMPTY>
 * <!ATTLIST xsl:value-of
 *   select %expr; #REQUIRED
 *   disable-output-escaping (yes|no) "no"
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#value-of">value-of in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemValueOf extends ElemTemplateElement
{
    static final long serialVersionUID = 3490728458007586786L;

  /**
   * The select expression to be executed.
   * @serial
   */
  private XPath m_selectExpression = null;

  /**
   * True if the pattern is a simple ".".
   * @serial
   */
  private boolean m_isDot = false;

  /**
   * Set the "select" attribute.
   * The required select attribute is an expression; this expression
   * is evaluated and the resulting object is converted to a
   * string as if by a call to the string function.
   *
   * @param v The value to set for the "select" attribute.
   */
  public void setSelect(XPath v)
  {

    if (null != v)
    {
      String s = v.getPatternString();

      m_isDot = (null != s) && s.equals(".");
    }

    m_selectExpression = v;
  }

  /**
   * Get the "select" attribute.
   * The required select attribute is an expression; this expression
   * is evaluated and the resulting object is converted to a
   * string as if by a call to the string function.
   *
   * @return The value of the "select" attribute.
   */
  public XPath getSelect()
  {
    return m_selectExpression;
  }

  /**
   * Tells if this element should disable escaping.
   * @serial
   */
  private boolean m_disableOutputEscaping = false;

  /**
   * Set the "disable-output-escaping" attribute.
   * Normally, the xml output method escapes & and < (and
   * possibly other characters) when outputting text nodes.
   * This ensures that the output is well-formed XML. However,
   * it is sometimes convenient to be able to produce output
   * that is almost, but not quite well-formed XML; for
   * example, the output may include ill-formed sections
   * which are intended to be transformed into well-formed
   * XML by a subsequent non-XML aware process. For this reason,
   * XSLT provides a mechanism for disabling output escaping.
   * An xsl:value-of or xsl:text element may have a
   * disable-output-escaping attribute; the allowed values
   * are yes or no; the default is no; if the value is yes,
   * then a text node generated by instantiating the xsl:value-of
   * or xsl:text element should be output without any escaping.
   * @see <a href="http://www.w3.org/TR/xslt#disable-output-escaping">disable-output-escaping in XSLT Specification</a>
   *
   * @param v The value to set for the "disable-output-escaping" attribute.
   */
  public void setDisableOutputEscaping(boolean v)
  {
    m_disableOutputEscaping = v;
  }

  /**
   * Get the "disable-output-escaping" attribute.
   * Normally, the xml output method escapes & and < (and
   * possibly other characters) when outputting text nodes.
   * This ensures that the output is well-formed XML. However,
   * it is sometimes convenient to be able to produce output
   * that is almost, but not quite well-formed XML; for
   * example, the output may include ill-formed sections
   * which are intended to be transformed into well-formed
   * XML by a subsequent non-XML aware process. For this reason,
   * XSLT provides a mechanism for disabling output escaping.
   * An xsl:value-of or xsl:text element may have a
   * disable-output-escaping attribute; the allowed values
   * are yes or no; the default is no; if the value is yes,
   * then a text node generated by instantiating the xsl:value-of
   * or xsl:text element should be output without any escaping.
   * @see <a href="http://www.w3.org/TR/xslt#disable-output-escaping">disable-output-escaping in XSLT Specification</a>
   *
   * @return The value of the "disable-output-escaping" attribute.
   */
  public boolean getDisableOutputEscaping()
  {
    return m_disableOutputEscaping;
  }

  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_VALUEOF;
  }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   *
   * NEEDSDOC @param sroot
   *
   * @throws TransformerException
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {

    super.compose(sroot);

    java.util.Vector vnames = sroot.getComposeState().getVariableNames();

    if (null != m_selectExpression)
      m_selectExpression.fixupVariables(
        vnames, sroot.getComposeState().getGlobalsSize());
  }

  /**
   * Return the node name.
   *
   * @return The node name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_VALUEOF_STRING;
  }

  /**
   * Execute the string expression and copy the text to the
   * result tree.
   * The required select attribute is an expression; this expression
   * is evaluated and the resulting object is converted to a string
   * as if by a call to the string function. The string specifies
   * the string-value of the created text node. If the string is
   * empty, no text node will be created. The created text node will
   * be merged with any adjacent text nodes.
   * @see <a href="http://www.w3.org/TR/xslt#value-of">value-of in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    XPathContext xctxt = transformer.getXPathContext();
    SerializationHandler rth = transformer.getResultTreeHandler();

    if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEvent(this);

    try
    {
      // Optimize for "."
      if (false && m_isDot && !transformer.getDebug())
      {
        int child = xctxt.getCurrentNode();
        DTM dtm = xctxt.getDTM(child);

        xctxt.pushCurrentNode(child);

        if (m_disableOutputEscaping)
          rth.processingInstruction(
            javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING, "");

        try
        {
          dtm.dispatchCharactersEvents(child, rth, false);
        }
        finally
        {
          if (m_disableOutputEscaping)
            rth.processingInstruction(
              javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING, "");

          xctxt.popCurrentNode();
        }
      }
      else
      {
        xctxt.pushNamespaceContext(this);

        int current = xctxt.getCurrentNode();

        xctxt.pushCurrentNodeAndExpression(current, current);

        if (m_disableOutputEscaping)
          rth.processingInstruction(
            javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING, "");

        try
        {
          Expression expr = m_selectExpression.getExpression();

          if (transformer.getDebug())
          {
            XObject obj = expr.execute(xctxt);

            transformer.getTraceManager().fireSelectedEvent(current, this,
                    "select", m_selectExpression, obj);
            obj.dispatchCharactersEvents(rth);
          }
          else
          {
            expr.executeCharsToContentHandler(xctxt, rth);
          }
        }
        finally
        {
          if (m_disableOutputEscaping)
            rth.processingInstruction(
              javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING, "");

          xctxt.popNamespaceContext();
          xctxt.popCurrentNodeAndExpression();
        }
      }
    }
    catch (SAXException se)
    {
      throw new TransformerException(se);
    }
    catch (RuntimeException re) {
    	TransformerException te = new TransformerException(re);
    	te.setLocator(this);
    	throw te;
    }
    finally
    {
      if (transformer.getDebug())
	    transformer.getTraceManager().fireTraceEndEvent(this); 
    }
  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to children list
   *
   * @return Child just added to children list
   *
   * @throws DOMException
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    return null;
  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_selectExpression.getExpression().callVisitors(m_selectExpression, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }

}
