/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.swing.svg;

import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;

/*
import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.EmbededExternalResourceSecurity;
import org.apache.batik.bridge.EmbededScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadScriptSecurity;
*/

/**
 * This Implements the SVGUserAgent interface to provide a very simple
 * version of client services to the JSVGComponent.
 *
 * This implementation does not require any GUI interaction to work.
 * This implementation is particularly bad about user interaction
 * most of the alert,prompt,etc methods are totally useless.
 * In a GUI environment you probably want to use SVGUserAgentGUIAdapter.
 *
 * @author <a href="mailto:deweese@apache.org>deweese</a>
 * @version $Id$
 */
public class SVGUserAgentAdapter implements SVGUserAgent {
    public SVGUserAgentAdapter() { }

    /**
     * Displays an error message.
     */
    public void displayError(String message) {
        System.err.println(message);
    }

    /**
     * Displays an error resulting from the specified Exception.
     */
    public void displayError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Displays a message in the User Agent interface.
     * The given message is typically displayed in a status bar.
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Shows an alert dialog box.
     */
    public void showAlert(String message) {
        System.err.println(message);
    }

    /**
     * Shows a prompt dialog box.
     */
    public String showPrompt(String message) {
        return "";
    }

    /**
     * Shows a prompt dialog box.
     */
    public String showPrompt(String message, String defaultValue) {
        return defaultValue;
    }

    /**
     * Shows a confirm dialog box.
     */
    public boolean showConfirm(String message) {
        return false;
    }

    /**
     * Returns the size of a px CSS unit in millimeters.
     */
    public float getPixelUnitToMillimeter() {
        return 0.26458333333333333333333333333333f; // 96dpi
    }
        
    /**
     * Returns the size of a px CSS unit in millimeters.
     * This will be removed after next release.
     * @see #getPixelUnitToMillimeter()
     */
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
            
    }

    /**
     * Returns the default font family.
     */
    public String getDefaultFontFamily() {
        return "Serif";
    }

    /** 
     * Returns the  medium font size. 
     */
    public float getMediumFontSize() {
        // 9pt (72pt == 1in)
        return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
    }

    /**
     * Returns a lighter font-weight.
     */
    public float getLighterFontWeight(float f) {
        // Round f to nearest 100...
        int weight = ((int)((f+50)/100))*100;
        switch (weight) {
        case 100: return 100;
        case 200: return 100;
        case 300: return 200;
        case 400: return 300;
        case 500: return 400;
        case 600: return 400;
        case 700: return 400;
        case 800: return 400;
        case 900: return 400;
        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }

    /**
     * Returns a bolder font-weight.
     */
    public float getBolderFontWeight(float f) {
        // Round f to nearest 100...
        int weight = ((int)((f+50)/100))*100;
        switch (weight) {
        case 100: return 600;
        case 200: return 600;
        case 300: return 600;
        case 400: return 600;
        case 500: return 600;
        case 600: return 700;
        case 700: return 800;
        case 800: return 900;
        case 900: return 900;
        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }


    /**
     * Returns the language settings.
     */
    public String getLanguages() {
        return "en";
    }

    /**
     * Returns the user stylesheet uri.
     * @return null if no user style sheet was specified.
     */
    public String getUserStyleSheetURI() {
        return null;
    }

    /**
     * Returns the class name of the XML parser.
     */
    public String getXMLParserClassName() {
        return XMLResourceDescriptor.getXMLParserClassName();
    }

    /**
     * Returns true if the XML parser must be in validation mode, false
     * otherwise.
     */
    public boolean isXMLParserValidating() {
        return false;
    }

    /**
     * Returns this user agent's CSS media.
     */
    public String getMedia() {
        return "screen";
    }

    /**
     * Returns this user agent's alternate style-sheet title.
     */
    public String getAlternateStyleSheet() {
        return null;
    }

    /**
     * Opens a link.
     * @param uri The document URI.
     * @param newc Whether the link should be activated in a new component.
     */
    public void openLink(String uri, boolean newc) {
    }

    /**
     * Tells whether the given extension is supported by this
     * user agent.
     */
    public boolean supportExtension(String s) {
        return false;
    }

    public void handleElement(Element elt, Object data){
    }

    /**
     * Returns the security settings for the given script
     * type, script url and document url
     * 
     * @param scriptType type of script, as found in the 
     *        type attribute of the &lt;script&gt; element.
     * @param scriptURL url for the script, as defined in
     *        the script's xlink:href attribute. If that
     *        attribute was empty, then this parameter should
     *        be null
     * @param docURL url for the document into which the 
     *        script was found.
     */
    public ScriptSecurity getScriptSecurity(String scriptType,
                                            ParsedURL scriptURL,
                                            ParsedURL docURL){
        return new RelaxedScriptSecurity(scriptType,
                                         scriptURL,
                                         docURL);
        /*
        return new DefaultScriptSecurity(scriptType,
                                         scriptURL,
                                         docURL);
        return new EmbededScriptSecurity(scriptType,
                                         scriptURL,
                                         docURL);
        return new NoLoadScriptSecurity(scriptType);
        */
    }

    /**
     * This method throws a SecurityException if the script
     * of given type, found at url and referenced from docURL
     * should not be loaded.
     * 
     * This is a convenience method to call checkLoadScript
     * on the ScriptSecurity strategy returned by 
     * getScriptSecurity.
     *
     * @param scriptType type of script, as found in the 
     *        type attribute of the &lt;script&gt; element.
     * @param scriptURL url for the script, as defined in
     *        the script's xlink:href attribute. If that
     *        attribute was empty, then this parameter should
     *        be null
     * @param docURL url for the document into which the 
     *        script was found.
     */
    public void checkLoadScript(String scriptType,
                                ParsedURL scriptURL,
                                ParsedURL docURL) throws SecurityException {
        ScriptSecurity s = getScriptSecurity(scriptType,
                                             scriptURL,
                                             docURL);

        if (s != null) {
            s.checkLoadScript();
        } 
    }

    /**
     * Returns the security settings for the given 
     * resource url and document url
     * 
     * @param resourceURL url for the resource, as defined in
     *        the resource's xlink:href attribute. If that
     *        attribute was empty, then this parameter should
     *        be null
     * @param docURL url for the document into which the 
     *        resource was found.
     */
    public ExternalResourceSecurity 
        getExternalResourceSecurity(ParsedURL resourceURL,
                                    ParsedURL docURL){
        return new RelaxedExternalResourceSecurity(resourceURL,
                                                   docURL);
        /*
        return new DefaultExternalResourceSecurity(resourceURL,
                                                   docURL);
        return new EmbededExternalResourceSecurity(resourceURL);
        return new NoLoadExternalResourceSecurity();
        */
    }

    /**
     * This method throws a SecurityException if the resource
     * found at url and referenced from docURL
     * should not be loaded.
     * 
     * This is a convenience method to call checkLoadExternalResource
     * on the ExternalResourceSecurity strategy returned by 
     * getExternalResourceSecurity.
     *
     * @param scriptURL url for the script, as defined in
     *        the script's xlink:href attribute. If that
     *        attribute was empty, then this parameter should
     *        be null
     * @param docURL url for the document into which the 
     *        script was found.
     */
    public void 
        checkLoadExternalResource(ParsedURL resourceURL,
                                  ParsedURL docURL) throws SecurityException {
        ExternalResourceSecurity s 
            =  getExternalResourceSecurity(resourceURL, docURL);
            
        if (s != null) {
            s.checkLoadExternalResource();
        }
    }
};
