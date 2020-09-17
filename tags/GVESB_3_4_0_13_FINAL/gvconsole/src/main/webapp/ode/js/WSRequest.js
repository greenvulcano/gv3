/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// This file introduces two classes: WSRequest for invoking a Web Service, and WebServiceError to encapsulate failure information.

var WSRequest = function() {
    // properties and usage mirror XMLHTTPRequest
    this.readyState = 0;
    this.responseText = null;
    this.responseXML = null;
    this.error = null;
    this.onreadystatechange = null;
    // Some internal properties
    this._xmlhttp = WSRequest.util._createXMLHttpRequestObject();
    this._soapVer = null;
    this._async = true;
    this._optionSet = null;
    this._uri = null;
    this._username = null;
    this._password = null;
};

var WebServiceError = function(reason, detail, code) {
    this.reason = reason;
    this.detail = detail;
    this.code = code;
    this.toString = function() { return this.reason; };
};

/**
 * @description Prepare a Web Service Request .
 * @method open
 * @public
 * @static
 * @param {object} options
 * @param {string} URL
 * @param {boolean} asyncFlag
 * @param {string} username
 * @param {string} password
 */
WSRequest.prototype.open = function(options, URL, asnycFlag, username, password) {
    if (arguments.length < 2 || arguments.length > 6)
    {
        throw new WebServiceError("Invalid input argument", "WSRequest.open method requires 2 to 6 arguments, but " + arguments.length + (arguments.length == 1 ? " was" : " were") + " specified.");
    }

    if (typeof(options) == "string") {
        this._optionSet = new Array();
        this._optionSet["HTTPMethod"] = options;
    } else {
        this._optionSet = options;
    }

    this._uri = URL;
    this._async = asnycFlag;
    if (username != null && password == null)
        throw new WebServiceError("User name should be accompanied by a password", "WSRequest.open invocation specified username: '" + username + "' without a corresponding password.");
    else
    {
        this._username = username;
        this._password = password;
    }

    this.readyState = 1;
    if (this.onreadystatechange != null)
        this.onreadystatechange();
    this.responseText = null;
    this.responseXML = null;
    this.error = null;
};

/**
 * @description Send the payload to the Web Service.
 * @method send
 * @public
 * @static
 * @param {dom} response xml payload
 */
WSRequest.prototype.send = function(payload) {
    if (arguments.length > 1) {
        throw new WebServiceError("Invalid input argument.", "WSRequest.send() only accepts a single argument, " + arguments.length + " were specified.");
    }

    // request body formatted as a string
    var req = null;

    var method;
    if (this._optionSet["HTTPMethod"] != null)
        method = this._optionSet["HTTPMethod"];
    else
        method = "POST";

    this._soapVer = WSRequest.util._bindingVersion(this._optionSet);

    if (payload != null)
    {
        // seralize the dom to string
        var content = WSRequest.util._serializeToString(payload);
        if (typeof(content) == "boolean" && content == false) {
            throw new WebServiceError("Invalid input argument.", "WSRequest.send() unable to serialize XML payload.");
        }

    }

    // formulate the message envelope
    if (this._soapVer == 0) {
        var processed = WSRequest.util._buildHTTPpayload(this._optionSet, this._uri, content);
        req = processed["body"];
        this._uri = processed["url"];
    } else {
        req = WSRequest.util._buildSOAPEnvelope(this._soapVer, this._optionSet, this._uri, content, this._username, this._password);
    }

    // Note that we infer soapAction from the "action" parameter - also used for wsa:Action.
    //  WS-A recommends keeping these two items in sync.
    var soapAction = this._optionSet["action"];

    try {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
    } catch(e) {
    }
    var accessibleDomain = true;  // assume so for now
    try {
        this._xmlhttp.open(method, this._uri, this._async, this._username, this._password);
        // Process protocol-specific details
        switch (this._soapVer) {
            case 1.1:
                soapAction = (soapAction == undefined ? '""' : '"' + soapAction + '"');
                this._xmlhttp.setRequestHeader("SOAPAction", soapAction);
                this._xmlhttp.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
                break;
            case 1.2:
                this._xmlhttp.setRequestHeader("Content-Type", "application/soap+xml;charset=UTF-8" + (soapAction == undefined ? "" : ";action=" + soapAction));
                break;
            case 0:
                var contentType;
                if (this._optionSet["HTTPInputSerialization"] != null) {
                    contentType = this._optionSet["HTTPInputSerialization"]
                } else {
                    if (method == "GET" | method == "DELETE") {
                        contentType = "application/x-www-form-urlencoded";
                    } else {
                        contentType = "application/xml";
                    }
                }
                this._xmlhttp.setRequestHeader("Content-Type", contentType);
                break;
        }
    } catch (e) {
        // If we received an error, see if it's an XSS error, if so don't fail - there still might be hope!
        if (e.description == "Access is denied.\r\n" || e.toString() == "Permission denied to call method XMLHttpRequest.open") {
            try {
                // Are we in the context of a Google Gadget?
                accessibleDomain = _IG_FetchXmlContent == undefined;
            } catch (d) {
                throw e;
            }
        } else throw e;
    }
    if (accessibleDomain) {
        if (this._async) {
            // async call
            this._xmlhttp.onreadystatechange = WSRequest.util._bind(this._handleReadyState, this);
            this._xmlhttp.send(req);
        } else {
            // sync call
            this.readyState = 2;
            if (this.onreadystatechange != null)
                this.onreadystatechange();
            this._xmlhttp.send(req);
	    this._processResult();
            if (this.error != null)
                throw (this.error);
            this.readyState = 4;
            if (this.onreadystatechange != null)
                this.onreadystatechange();
        }
    } else {
        // Fallback to a Google Gadget, if we're able too.
        if (!this._async)
            throw ("Can only access Web service from within a Google Gadget when a callback is defined.");
        if (this._soapVer != 0 || method.toUpperCase() != "GET")
            throw ("Can only access Web service from within a Google Gadget through the HTTP binding, using the GET method.");

        this.readyState = 2;
        if (this.onreadystatechange != null)
            this.onreadystatechange();

        _IG_FetchXmlContent(this._uri,  WSRequest.util._bind(this._FetchXMLContentCallback, this));
    }
}

/**
 * @description Google Gadget request callback - simulate an XMLHttp callback and return to normal processing.
 * @method _FetchXMLContentCallback
 * @private
 * @static
 * @param {dom} response xml payload
 */
WSRequest.prototype._FetchXMLContentCallback = function (response) {
    if (response != null && typeof(response) == "object") {
        this._xmlhttp = {
            "responseXML" : response,
            "responseText" : WSRequest.util._serializeToString(response),
            "status" : "200",
            "readyState" : 4
         };
    } else {
         this._xmlhttp = {
            "responseXML" : null,
            "responseText" : response,
            "status" : "",
            "statusText" : "_IG_FetchXMLContent failed to return valid XML.",
            "readyState" : 4
         };
    }
    this._handleReadyState();
}

/**
 * @description Set responseText, responseXML, and error of WSRequest.
 * @method _processResult
 * @private
 * @static
 */
WSRequest.prototype._processResult = function () {
    var httpstatus;
    if (this._soapVer == 0) {
        this.responseText = this._xmlhttp.responseText;
        this.responseXML = this._xmlhttp.responseXML;

        httpstatus = this._xmlhttp.status;
        if (httpstatus == '200' || httpstatus == '202') {
            this.error = null;
        } else {
            this.error = new WebServiceError(this._xmlhttp.statusText, this.responseText, "HTTP " + this._xmlhttp.status);
        }
    } else {
        var browser = WSRequest.util._getBrowser();

        if (this._xmlhttp.responseText != "") {
            var response;
            var responseXMLdoc;
            if (browser == "ie" || browser == "ie7") {
                if (this._xmlhttp.responseXML.documentElement == null) {
                    // unrecognized media type (probably application/soap+xml)
                    responseXMLdoc = new ActiveXObject("Microsoft.XMLDOM");
                    responseXMLdoc.loadXML(this._xmlhttp.responseText);
                    response = responseXMLdoc.documentElement;
                } else {
                    response = this._xmlhttp.responseXML.documentElement;
                }
            } else {
                var parser = new DOMParser();
                responseXMLdoc = parser.parseFromString(this._xmlhttp.responseText,"text/xml");
                response = responseXMLdoc.documentElement;
                response.normalize();  //fixes data getting truncated at 4096 characters
            }
            var soapNamespace;
            if (this._soapVer == 1.1)
                soapNamespace = "http://schemas.xmlsoap.org/soap/envelope/";
            else
                soapNamespace = "http://www.w3.org/2003/05/soap-envelope";

            var soapBody = WSRequest.util._firstElement(response, soapNamespace, "Body");
            if (soapBody != null && soapBody.hasChildNodes()) {
                var newDoc;
                if (browser == "ie" || browser == "ie7") {
                    newDoc = new ActiveXObject("Microsoft.XMLDOM");
                    newDoc.appendChild(soapBody.firstChild);
                } else {
                    newDoc = document.implementation.createDocument("", "", null);
                    newDoc.appendChild(soapBody.firstChild.cloneNode(true));
                }

                this.responseXML = newDoc;
                this.responseText = WSRequest.util._serializeToString(newDoc);

                fault = WSRequest.util._firstElement(newDoc, soapNamespace, "Fault");
                if (fault != undefined) {
                    this.error = new WebServiceError();
                    if (this._soapVer == 1.2) {
                        this.error.code = WSRequest.util._stringValue(WSRequest.util._firstElement(fault, soapNamespace, "Value"));
                        this.error.reason = WSRequest.util._stringValue(WSRequest.util._firstElement(fault, soapNamespace, "Text"));
                        this.error.detail = WSRequest.util._firstElement(fault, soapNamespace, "Detail");
                    } else {
                        this.error.code = WSRequest.util._stringValue(fault.getElementsByTagName("faultcode")[0]);
                        this.error.reason = WSRequest.util._stringValue(fault.getElementsByTagName("faultstring")[0]);
                        this.error.detail = fault.getElementsByTagName("detail")[0];
                    }
                }
            } else {
                // empty SOAP body - not necessarily an error
                this.responseXML = null;
                this.responseText = "";
                this.error = null;
            }
        } else {
            // If this block being executed; it's due to server connection has falied.
            this.responseXML = null;
            this.responseText = "";
            try {
                httpstatus = this._xmlhttp.status;
                if (httpstatus == '200' || httpstatus == '202') {
                    this.error = null;
                } else {
                    this.error = new WebServiceError();
                    this.error.code = "HTTP " + this._xmlhttp.status;
                    this.error.reason = "Server connection has failed.";
                    this.error.detail = this._xmlhttp.statusText;
                }
            } catch (e) {
                this.error = new WebServiceError();
                this.error.code = null;
                this.error.reason = "Server connection has failed.";
                this.error.detail = e.toString();
            }
        }
    }
}

/**
 * @description XMLHttp callback handler.
 * @method _handleReadyState
 * @private
 * @static
 */
WSRequest.prototype._handleReadyState = function() {
    if (this._xmlhttp.readyState == 2) {
        this.readyState = 2;
        if (this.onreadystatechange != null)
            this.onreadystatechange();
    }

    if (this._xmlhttp.readyState == 3) {
        this.readyState = 3;
        if (this.onreadystatechange != null)
            this.onreadystatechange();
    }

    if (this._xmlhttp.readyState == 4) {
        this._processResult();

        this.readyState = 4;
        if (this.onreadystatechange != null)
            this.onreadystatechange();
    }
};


// Utility functions

WSRequest.util = {

    _msxml : [
            'MSXML2.XMLHTTP.3.0',
            'MSXML2.XMLHTTP',
            'Microsoft.XMLHTTP'
            ],

    /**
     * @description Instantiates a XMLHttpRequest object and returns it.
     * @method _createXMLHttpRequestObject
     * @private
     * @static
     * @return object
     */
    _createXMLHttpRequestObject : function() {
        var xhrObject;

        try {
            xhrObject = new XMLHttpRequest();
        } catch(e) {
            for (var i = 0; i < this._msxml.length; ++i) {
                try
                {
                    // Instantiates XMLHttpRequest for IE and assign to http.
                    xhrObject = new ActiveXObject(this._msxml[i]);
                    break;
                }
                catch(e) {
                    // do nothing
                }
            }
        } finally {
            return xhrObject;
        }
    },

    /**
     * @description Serialize payload to string.
     * @method _serializeToString
     * @private
     * @static
     * @param {dom} payload   xml payload
     * @return string
     */
    _serializeToString : function(payload) {
        if (payload == null) return null;
        if (typeof(payload) == "string") {
            return payload;
        } else if (typeof(payload) == "object") {
            var browser = WSRequest.util._getBrowser();
            switch (browser) {
                case "gecko":
                case "safari":
                    var serializer = new XMLSerializer();
                    return serializer.serializeToString(payload);
                    break;
                case "ie":
                case "ie7":
                    return payload.xml;
                    break;
                case "opera":
                    var xmlSerializer = document.implementation.createLSSerializer();
                    return xmlSerializer.writeToString(payload);
                    break;
                case "undefined":
                    throw new WebServiceError("Unknown browser", "WSRequest.util._serializeToString doesn't recognize the browser, to invoke browser-specific serialization code.");
            }
        } else {
            return false;
        }
    },


    /**
     * @description get the character element children in a browser-independent way.
     * @method _stringValue
     * @private
     * @static
     * @param {dom element} node
     * @return string
     */
    _stringValue : function(node) {
        var browser = WSRequest.util._getBrowser();
        switch (browser) {
            case "ie":
            case "ie7":
                return node.text;
                break;
            case "gecko":
            case "opera":
            case "safari":
            case "undefined":
                var value = "";
                if (node.nodeType == 3) {
                    value = node.nodeValue;
                } else {
                    for (var i = 0; i < node.childNodes.length; i++) {
                        value += WSRequest.util._stringValue(node.childNodes[i]);
                    }
                }
                return value;
                break;
        }
    },


    /**
     * @description Determines which binding to use (SOAP 1.1, SOAP 1.2, or HTTP) from the various options.
     * @method _bindingVersion
     * @private
     * @static
     * @param {Array} options   Options given by user
     * @return string
     */
    _bindingVersion : function(options) {
        var soapVer;
        switch (options["useBindng"]) {
            case "SOAP 1.2":
                soapVer = 1.2;
                break;
            case "SOAP 1.1":
                soapVer = 1.1;
                break;
            case "HTTP":
                soapVer = 0;
                break;
            case undefined:
                var useSOAP = options["useSOAP"];
                switch (useSOAP) {
                    case 1.2:
                        soapVer = 1.2;
                        break;
                    case "1.2":
                        soapVer = 1.2;
                        break;
                    case 1.1:
                        soapVer = 1.1;
                        break;
                    case "1.1":
                        soapVer = 1.1;
                        break;
                    case true:
                        soapVer = 1.2;
                        break;
                    case false:
                        soapVer = 0;
                        break;
                    case undefined:
                        throw("Unspecified binding type: set useBinding = 'SOAP 1.1' | 'SOAP 1.2' | 'HTTP'.");
                        break;
                    default:
                        throw("Unsupported useSOAP value '" + useSOAP + "'; set 'useBinding' option instead.");
                }
                break;
            default:
                throw("Unsupported useBinding value '" + options["useBinding"] + "': must be 'SOAP 1.2' | 'SOAP 1.1' | 'HTTP'.");
        }
        return soapVer;
    },


    /**
     * @description Determine which browser we're running.
     * @method _getBrowser
     * @private
     * @static
     * @return string
     */
    _getBrowser : function() {
        var ua = navigator.userAgent.toLowerCase();
        if (ua.indexOf('opera') != -1) { // Opera (check first in case of spoof)
            return 'opera';
        } else if (ua.indexOf('msie 7') != -1) { // IE7
            return 'ie7';
        } else if (ua.indexOf('msie') != -1) { // IE
            return 'ie';
        } else if (ua.indexOf('safari') != -1) { // Safari (check before Gecko because it includes "like Gecko")
            return 'safari';
        } else if (ua.indexOf('gecko') != -1) { // Gecko
            return 'gecko';
        } else {
            return false;
        }
    },


    /**
     * @description Build HTTP payload using given parameters.
     * @method _buildHTTPpayload
     * @private
     * @static
     * @param {Array} options Options given by user
     * @param {string} url Address the request will be sent to.
     * @param {string} content SOAP payload in string format.
     * @return {array} Containing the processed URL and request body.
     */
    _buildHTTPpayload : function(options, url, content) {
        // Create array to hold request uri and body.
        var resultValues = new Array();
        resultValues["url"] = "";
        resultValues["body"] = "";
        var paramSeparator = "&";
        var inputSerialization;

        var HTTPQueryParameterSeparator = "HTTPQueryParameterSeparator";
        var HTTPInputSerialization = "HTTPInputSerialization";
        var HTTPLocation = "HTTPLocation";
        var HTTPMethod = "HTTPMethod";

        // If a parameter separator has been identified, use it instead of the default &.
        if (options[HTTPQueryParameterSeparator] != null) {
            paramSeparator = options[HTTPQueryParameterSeparator];
        }

        // If input serialization is not specified, default based on HTTP Method.
        if (options[HTTPInputSerialization] == null) {
            if (options[HTTPMethod] == "GET" | options[HTTPMethod] == "DELETE") {
                inputSerialization = "application/x-www-form-urlencoded";
            } else {
                inputSerialization = "application/xml";
            }
        } else {
            inputSerialization = options[HTTPInputSerialization];
        }

        //create new document from string
        var xmlDoc;

        // Parser is browser specific.
        var browser = WSRequest.util._getBrowser();
        if (browser == "ie" || browser == "ie7") {
            //create a DOM from content string.
            xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
            if (content != null && content != "")
                xmlDoc.loadXML(content);
        } else {
            //create a DOMParser to get DOM from content string.
            var xmlParser = new DOMParser();
            if (content != null && content != "")
                xmlDoc = xmlParser.parseFromString(content, "text/xml");
        }

        // If the payload is to be URL encoded, other options have to be examined.
        if (inputSerialization == "application/x-www-form-urlencoded" || inputSerialization == "application/xml") {

            resultValues["url"] = options[HTTPLocation];

            // If templates are specified and a valid payload is available, process, else just return original URI.
            if (options[HTTPLocation] != null && xmlDoc != null && xmlDoc.hasChildNodes()) {
                // Ideally .documentElement should be used instead of .firstChild, but this does not work.
                var rootNode = xmlDoc.firstChild;

                // Process payload, distributing content across the URL and body as specified.
                resultValues = WSRequest.util._processNode(options, resultValues, rootNode, paramSeparator,
                        inputSerialization);

            }
            // Globally replace any remaining template tags with empty strings.
            var allTemplateRegex = new RegExp("\{.*\}", "ig");
            resultValues["url"] = resultValues["url"].replace(allTemplateRegex, "");

            // Append processed HTTPLocation value to URL.
            resultValues["url"] = WSRequest.util._joinUrlToLocation(url, resultValues["url"]);

            // Sending the XML in the request body.
            if (content != null && inputSerialization == "application/xml") {
                resultValues["body"] = content;
            }
        } else if (inputSerialization == "multipart/form-data") {
            // Just throw an exception for now - will try to use browser features in a later release.
            throw new WebServiceError("Unsupported serialization option.", "WSRequest.util._buildHTTPpayload doesn't yet support multipart/form-data serialization.");
        }
        return resultValues;
    },

    /**
     * @description Traverse the DOM tree below a given node, retreiving the content of each node and appending it to the
     *  URL or the body of the request based on the options specified.
     * @method _processNode
     * @private
     * @static
     * @param {Array} options Options given by user.
     * @param {Array} resultValues HTTP Location content and request body.
     * @param {XML} node SOAP payload as an XML object.
     * @param {string} paramSeparator Separator character for URI parameters.
     * @return {array} Containing the processed HTTP Location content and request body.
     */
    _processNode : function(options, resultValues, node, paramSeparator, inputSerialization) {
        var queryStringSep = '?';
        var HTTPLocationIgnoreUncited = "HTTPLocationIgnoreUncited";
        var HTTPMethod = "HTTPMethod";

        // Traverse the XML and add the contents of each node to the URL or body.
        do {

            // Recurse if node has children.
            if (node.hasChildNodes())
            {
                resultValues = WSRequest.util._processNode(options, resultValues, node.firstChild, paramSeparator,
                        inputSerialization);
            }

            // Check for availability of node name and data before processing.
            if (node.nodeValue != null) {
                var tokenName = WSRequest.util._nameForValue(node);

                // Create a regex to look for the token.
                var templateRegex = new RegExp("\{" + tokenName + "\}", "i");
                var unencTmpltRegex = new RegExp("\{!" + tokenName + "\}", "i");
                var tokenLocn;

                // If the token is in the URL - swap tokens with values.
                if ((tokenLocn = resultValues["url"].search(templateRegex)) != -1) {
                    // Replace the token with the URL encoded node value.
                    var isQuery = resultValues["url"].substring(0, tokenLocn).indexOf('?') != -1;
                    resultValues["url"] = resultValues["url"].replace(templateRegex,
                            WSRequest.util._encodeString(node.nodeValue, isQuery));
                } else if (resultValues["url"].search(unencTmpltRegex) != -1) {
                    // Replace the token with the node value, witout encoding.
                    resultValues["url"] = resultValues["url"].replace(templateRegex, node.nodeValue);
                } else {
                    var parameter = "";

                    // If the node has a list, create a bunch of name/value pairs, otherwise a single pair.
                    if (WSRequest.util._attributesContain(node.parentNode, "xsd:list")) {
                        var valueList = new Array();
                        valueList = node.nodeValue.split(' ');
                        for (var valueNum = 0; valueNum < valueList.length; valueNum++) {
                            parameter = parameter + tokenName + "=" + WSRequest.util._encodeString(valueList[valueNum],
                                    true);

                            // Add the parameter separator after each list value except the last.
                            if (valueNum < (valueList.length - 1)) {
                                parameter += paramSeparator;
                            }
                        }
                    } else {
                        parameter = tokenName + "=" + WSRequest.util._encodeString(node.nodeValue, true);
                    }

                    // If ignore uncited option has been set, append parameters to body else to the url.
                    if (options[HTTPLocationIgnoreUncited] != null && options[HTTPLocationIgnoreUncited]) {

                        // Add to request body if the serialization option and request type allows it.
                        if (inputSerialization == "application/x-www-form-urlencoded" && (options[HTTPMethod] == "POST"
                                || options[HTTPMethod] == "PUT")) {

                            // Assign or append additional parameters.
                            if (resultValues["body"] == "") {
                                resultValues["body"] = parameter;
                            } else {
                                resultValues["body"] = resultValues["body"] + paramSeparator + parameter;
                            }
                        }

                    } else {
                        // If he URL does not contain ? add it and then the parameter.
                        if (resultValues["url"].indexOf(queryStringSep) == -1) {
                            resultValues["url"] = resultValues["url"] + queryStringSep + parameter;
                        } else {
                            // ...otherwise just append the uncited value.
                            resultValues["url"] = resultValues["url"] + paramSeparator + parameter;
                        }
                    }
                }
            }
        } while (node = node.nextSibling)

        return resultValues;
    },

    /**
     * @description Build soap message using given parameters.
     * @method _buildSoapEnvelope
     * @private
     * @static
     * @param {string} soapVer SOAP version (1.1 or 1.2)
     * @param {Array} options   Options given by user
     * @param {string} url Address the request will be sent to.
     * @param {string} content SOAP payload
     * @param {string} username Optional username
     * @param {string} password Optional password
     * @return string
     */
    _buildSOAPEnvelope : function(soapVer, options, url, content, username, password) {
        var ns;
        if (soapVer == 1.1)
            ns = "http://schemas.xmlsoap.org/soap/envelope/";
        else
            ns = "http://www.w3.org/2003/05/soap-envelope";

        var headers = "";

        // addressing version/namespace
        var useWSA = options["useWSA"];
        var wsaNs = "";
        var wsaNsDecl = "";
        var usingWSA = false;
        if (useWSA != undefined && useWSA) {
            var standardversion;
            if (useWSA == "1.0" || useWSA) {
                wsaNs = "http://www.w3.org/2005/08/addressing";
                standardversion = true;
            } else if (useWSA == "submission") {
                wsaNs = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
                standardversion = false;
            } else throw ("Unknown WS-Addressing version '" + useWSA + "': must be '1.0' | 'submission' | true | false.");
            wsaNsDecl = ' xmlns:wsa="' + wsaNs + '"';
            headers = this._buildWSAHeaders(standardversion, options, url);
            usingWSA = true;
        }
        var useWSS = options["useWSS"];
        if (useWSS != undefined && useWSS) {
            if (!usingWSA) {
                throw ('In order to use WS Security, WS Addressing should be enabled. Please set "options["useWSA"] = true"');
            }
            var created = new Date();
            headers += '<o:Security s:mustUnderstand="1" xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" ' +
                       'xmlns:o="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">' +
                       '<u:Timestamp u:Id="uuid-c3cdb38b-e4aa-4467-9d0e-dd30f081e08d-5">' +
                       '<u:Created>' + WSRequest.util._toXSdateTime(created) + '</u:Created>' +
                       '<u:Expires>' + WSRequest.util._toXSdateTime(created, 5) + '</u:Expires>' +
                       '</u:Timestamp>' +
                       '<o:UsernameToken u:Id="Me" >' +
                       '<o:Username>' + username + '</o:Username>' +
                       '<o:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">' + password + '</o:Password>' +
                       '</o:UsernameToken>' +
                       '</o:Security>';
        }

        var request = '<?xml version="1.0" encoding="UTF-8"?>\n' +
                  '<s:Envelope xmlns:s="' + ns + '"' +
                   wsaNsDecl + '>\n' +
                  '<s:Header>' + headers + '</s:Header>\n' +
                  '<s:Body>' + (content != null ? content : '') + '</s:Body>\n' +
                  '</s:Envelope>';
        return request;
    },

    /**
     * @description Build WS-Addressing headers using given parameters.
     * @method _buildWSAHeaders
     * @private
     * @static
     * @param {boolean} standardversion true for 1.0, false for submission
     * @param {Array} options   Options given by user
     * @param {string} address Address the request will be sent to.
     * @return string
     */
    _buildWSAHeaders : function(standardversion, options, address) {
        if (options['action'] == null)
            throw("'Action' option must be specified when WS-Addressing is engaged.");

        // wsa:To (required)
        var headers = "<wsa:To>" + address + "</wsa:To>\n";

        // wsa:From (optional)
        // Note: reference parameters and metadata aren't supported.
        if (options['from'] != null)
            headers += "<wsa:From><wsa:Address>" + options['From'] + "</wsa:Address></wsa:From>\n";

        // wsa:ReplyTo (optional)
        // Note: reference parameters and metadata aren't supported.
        // Note: No way to specify that wsa:ReplyTo should be omitted (e.g., only in-out MEPs are supported).
        if (options['replyto'] != null) {
            headers += "<wsa:ReplyTo><wsa:Address>" + options['ReplyTo'] + "</wsa:Address></wsa:ReplyTo>\n";
        } else {
            // Note: although wsa:ReplyTo is optional on in-out MEPs in the standard version, we put it in
            //  explicitly for convenience.
            headers += "<wsa:ReplyTo><wsa:Address>" +
                       ( standardversion ?
                         "http://www.w3.org/2005/08/addressing/anonymous" :
                         "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"
                               ) +
                       "</wsa:Address></wsa:ReplyTo>\n";
        }

        // wsa:MessageID (required if a response is expected, e.g. wsa:ReplyTo is specified, which is always for us.)
        // If user doesn't supply an identifier, we'll make one up.
        var id;
        if (options['messageid'] != null) {
            id = options['messageid'];
        } else {
            // coin a unique identifier based on the time (in milliseconds) and a 10-digit random number.
            var now = (new Date()).valueOf();
            var randomToken = Math.floor(Math.random() * 10000000000);
            id = "http://identifiers.wso2.com/messageid/" + now + "/" + randomToken;
        }
        headers += "<wsa:MessageID>" + id + "</wsa:MessageID>\n";

        // wsa:FaultTo (optional)
        // Note: reference parameters and metadata aren't supported.
        if (options['faultto'] != null)
            headers += "<wsa:FaultTo><wsa:Address>" + options['FaultTo'] + "</wsa:Address></wsa:FaultTo>\n";

        // wsa:Action (required)
        headers += "<wsa:Action>" + options['action'] + "</wsa:Action>\n"

        return headers;
    }
    ,

    /**
     * @description Set scope for callbacks.
     * @method _getRealScope
     * @private
     * @static
     * @param {Function} fn
     * @return Function
     */
    _getRealScope : function(fn) {
        var scope = window;
        if (fn._cscope) scope = fn._cscope;
        return function() {
            return fn.apply(scope, arguments);
        }
    },

    /**
     * @description Bind a function to the correct scope for callbacks
     * @method _bind
     * @private
     * @static
     * @param {Function} fn
     * @param {Object} obj
     * @return Function
     */
    _bind : function(fn, obj) {
        fn._cscope = obj;
        return this._getRealScope(fn);

    },


    /**
     * @description Normalize browser-specific differences in getElementsByTagName
     * @method _firstElement
     * @private
     * @static
     * @param {dom} node
     * @param {string} namespace
     * @param {string} localName
     * @return element
     */
    _firstElement : function (node, namespace, localName) {
        if (node == null) return null;
        var browser = WSRequest.util._getBrowser();
        var doc, el;
        if (browser == "ie" || browser == "ie7") {
            if (node.nodeType == 9)
                doc = node;
            else
                doc = node.ownerDocument;
            doc.setProperty("SelectionNamespaces", "xmlns:soap='" + namespace + "'");
            el = node.selectSingleNode(".//soap:" + localName);
        } else {
            // Some Firefox DOMs recognize namespaces ...
            el = node.getElementsByTagNameNS(namespace, localName)[0];
            if (el == undefined)
            // ... and some don't.
                el = node.getElementsByTagName(localName)[0];
        }
        return el;
    },


    /**
     * @description Returns the name of a given DOM text node, managing browser issues
     * @method _nameForValue
     * @private
     * @static
     * @param {dom} node
     * @return string
     */
    _nameForValue : function(node) {
        var browser = WSRequest.util._getBrowser();
        var nodeNameVal;

        // IE localName property does not work, so extract from node name.
        if (browser == "ie" || browser == "ie7") {
            var fullName = WSRequest.util._isEmpty(node.nodeName) ? node.parentNode.nodeName : node.nodeName;
            nodeNameVal = fullName.substring(fullName.indexOf(":") + 1, fullName.length);
        } else {
            nodeNameVal = WSRequest.util._isEmpty(node.localName) ? node.parentNode.localName : node.localName;
        }
        return nodeNameVal;
    },


    /**
     * @description Determins if a node string value is null or empty, managing browser issues
     * @method _isEmpty
     * @private
     * @static
     * @param {*} value
     * @return boolean
     */
    _isEmpty : function(value) {
        // Regex for determining if a given string is empty.
        var emptyRegEx = /^[\s]*$/;

        // Short circuit if null, otherwise check for empty.
        return (value == null || value == "#text" || emptyRegEx.test(value));
    },


    /**
     * @description Returns true if the attributes of the node contain a given value.
     * @method _attributeContain
     * @private
     * @static
     * @param {dom node} node
     * @param {string} value
      * @return boolean
     */
    _attributesContain : function(node, value) {
        var hasValue = false;

        // If node has attributes...
        if (node.attributes.length > 0) {
            // ...cycle through them and check for the value.
            for (var attNum = 0; attNum < node.attributes.length; attNum++) {
                if (node.attributes[attNum].nodeValue == value) {
                    hasValue = true;
                    break;
                }
            }
        }
        return hasValue;
    },


    /**
     * @description Appends the template string to the URI, ensuring that the two are separated by a ? or a /. Performs a
     * merge if the start of the template is the same as the end of the URI, which will resolve at joining until a
     * full resolution function can be developed.
     * @method _joinUrlToLocation
     * @private
     * @static
     * @param {string} endpointUri Base URI.
     * @param {string} templateString Processed contents of the HTTPLocation option.
     * @return string URI with the template string appended.
     */
    _joinUrlToLocation : function(endpointUri, templateString) {

        // JS implementation of pseudo-code found at http://www.ietf.org/rfc/rfc3986.txt sec 5.2.2
        function parse(url) {
            var result = {"scheme" : null, "authority" : null, "path" : null, "query": null, "fragment" : null};

            result.fragment = url.indexOf("#") < 0 ? null : url.substring(url.indexOf("#") + 1);
            url = result.fragment == null ? url : url.substring(0, url.indexOf("#"));
            result.query = url.indexOf("?") < 0 ? null : url.substring(url.indexOf("?") + 1);
            url = result.query == null ? url : url.substring(0, url.indexOf("?"));
            if (url.indexOf(':') > 0) {
                result.scheme = url.substring(0, url.indexOf(":"));
                url = url.substring(url.indexOf(":") + 1);
            }
            if (url.indexOf("//") == 0){
                url = url.substring(2);
                result.authority = url.substring(0, url.indexOf("/"));
                result.path = url.substring(url.indexOf("/"));
            } else result.path = url;
            return result;
        }

        function merge(base, relative) {
            if (base.authority != null && base.path == "") {
                return "/" + relative.path;
            } else {
                if (base.path.indexOf("/") < 0) {
                    return relative.path;
                } else {
                    var path = base.path.substring(0, base.path.lastIndexOf("/") + 1);
                    return path + relative.path;
                }
            }
        }

        function removeDotSegments (path) {
            var input = path;
            var output = "";

            while (input.length > 0) {
                if (input.indexOf("../") == 0 || input.indexOf("./") == 0) {
                    input = input.substring(input.indexOf("./"));
                } else {
                    if (input.indexOf("/./") == 0 || (input.indexOf("/.") == 0 && input.length == 2)) {
                        input = input.substring(2);
                        if (input.length == 0) input = "/";
                    } else {
                        if (input.indexOf("/../") == 0 || (input.indexOf("/..") == 0 && input.length == 3)) {
                            input = input.substring(3);
                            if (input.length == 0) input = "/";
                            output = output.substring(0, output.lastIndexOf("/"));
                        } else {
                            if (input == "." || input == "..") {
                                input="";
                            } else {
                                if (input.indexOf("/") == 0) {
                                    output += "/";
                                    input = input.substring(1);
                                }
                                var i = input.indexOf("/");
                                if (i < 0) i = 10000;
                                output += input.substring(0, i);
                                input = input.substring(i);
                            }
                        }
                    }
                }
            }
            return output;
        }

        var base = parse(endpointUri);
        var relative = parse(templateString);
        var result = {"scheme" : null, "authority" : null, "path" : null, "query": null, "fragment" : null};

        if (relative.scheme != null) {
            result.scheme = relative.scheme;
            result.authority = relative.authority;
            result.path = removeDotSegments(relative.path);
            result.query = relative.query;
        } else {
            if (relative.authority != null) {
                result.authority = relative.authority;
                result.path = removeDotSegments(relative.path);
                result.query = relative.query;
            } else {
                if (relative.path == "") {
                   result.path = base.path;
                   if (relative.query != null) {
                      result.query = relative.query;
                   } else {
                      result.query = base.query;
                   }
                } else {
                   if (relative.path.indexOf("/") == 0) {
                      result.path = removeDotSegments(relative.path);
                   } else {
                      result.path = merge(base, relative);
                      result.path = removeDotSegments(result.path);
                   }
                   result.query = relative.query;
                }
                result.authority = base.authority;
            }
            result.scheme = base.scheme;
        }
        result.fragment = relative.fragment;

        var resultURI = "";
        if (result.scheme != null) resultURI += result.scheme + ":";
        if (result.authority != null) resultURI += "//" + result.authority;
        resultURI += result.path;
        if (result.query != null) resultURI += "?" + result.query;
        if (result.fragment != null) resultURI += "#" + result.fragment;
        return resultURI;
    },


    /**
     * @description Encodes a given string in either path or query parameter format.
     * @method _encodeString
     * @private
     * @static
     * @param {string} srcString String to be encoded.
     * @param {boolean} queryParm Indicates that the string is a query parameter and not a part of the path.
     * @return string URL encoded string.
     */
    _encodeString : function (srcString, queryParm) {
        var legalInPath = "-._~!$'()*+,;=:@";
        var legalInQuery = "-._~!$'()*+,;=:@/?";

        var legal = queryParm ? legalInQuery : legalInPath;
        var encodedString = "";
        for (var i = 0; i < srcString.length; i++) {
            var ch = srcString.charAt(i);
            if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || legal.indexOf(ch) > -1) {
                encodedString += ch;
            } else {
                // Function encodeURIComponent will not encode ~!*()' but they are legal anyway.
                encodedString += encodeURIComponent(ch);
            }
        }
        return encodedString;
    },


    /**
     * @description Convert a Date to an xs:dateTime string
     * @method _toXSdateTime
     * @private
     * @static
     * @param {Date} thisDate   Date to be serialized.
     * @param {number} delta    Optional offset to serialize a time delta seconds in the future.
     * @return string
     */
    _toXSdateTime : function (thisDate, delta) {
        if (delta == null) delta = 0;

        var year = thisDate.getUTCFullYear();
        var month = thisDate.getUTCMonth() + 1;
        var day = thisDate.getUTCDate();
        var hours = thisDate.getUTCHours();
        var minutes = thisDate.getUTCMinutes();
        var seconds = thisDate.getUTCSeconds() + delta;
        var milliseconds = thisDate.getUTCMilliseconds();

        return year + "-" +
            (month < 10 ? "0" : "") + month + "-" +
            (day < 10 ? "0" : "") + day + "T" +
            (hours < 10 ? "0" : "") + hours + ":" +
            (minutes < 10 ? "0" : "") + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds +
            (milliseconds == 0 ? "" : (milliseconds/1000).toString().substring(1)) + "Z";
    }
};

