/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.GVLogger;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains static methods to check AdapterHttp configuration and
 * static fields containing tag names and possible values for attributes within
 * AdapterHttp configuration XML file.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AdapterHttpConfig
{
    /**
     * Application-wide token separator char (it will be used to concatenate
     * tokens into a single string).
     */
    public static final String TOKEN_SEPARATOR                                     = ":";

    /**
     * Prefix used to indicate that a keyword refers to an GVBuffer field
     * (standard or extended).
     */
    public static final String GVBUFFER_FIELD_KEYWORD_PREFIX                       = "GVBuffer.";

    /**
     * Prefix used to indicate that a keyword refers to an GVTransactionInfo
     * field (standard or extended).
     */
    public static final String GVTRANSINFO_FIELD_KEYWORD_PREFIX                    = "GVTransInfo.";

    /**
     * Default separator char between parameter entries within a query string
     */
    public static final String DEFAULT_PARAM_ENTRY_SEPARATOR                       = "&";

    /**
     * Default separator char between parameter's name and value within a query
     * string
     */
    public static final String DEFAULT_PARAM_NAMEVALUE_SEPARATOR                   = "=";

    /**
     * HTTP Request method <code>GET</code>
     */
    public static final String HTTP_REQUEST_METHOD_GET                             = "GET";

    /**
     * HTTP Request method <code>POST</code>
     */
    public static final String HTTP_REQUEST_METHOD_POST                            = "POST";

    /**
     * EBAdapterHttp XML configuration file root element XPath.
     */
    public static final String CONFIG_FILE_ROOT_ELEM_XPATH                         = "/AdapterHTTP";

    /**
     * XPath of the <tt>HandlerFactoryProperties</tt> section within AdapterHTTP
     * XML configuration file.
     */
    public static final String HANDLER_FACTORY_PROPERTIES_XPATH                    = "/AdapterHTTP/HandlerFactoryProperties";

    /**
     * EBAdapterHttp XML configuration file <tt>InboundCommunications</tt>
     * element name.
     */
    public static final String CONFIG_FILE_INBOUND_COMM_ELEM_NAME                  = "InboundCommunications";

    /**
     * EBAdapterHttp XML configuration file <tt>OutboundCommunications</tt>
     * element name.
     */
    public static final String CONFIG_FILE_OUTBOUND_COMM_ELEM_NAME                 = "OutboundCommunications";

    /**
     * EBAdapterHttp XML configuration file <tt>SystemAuthentication</tt>
     * element name.
     */
    public static final String CONFIG_FILE_AUTHENTICATION_ELEM_NAME                = "SystemAuthentication";

    /**
     * GreenVulcano communication <tt>RequestReply</tt> paradigm
     */
    public static final String GV_REQUEST_REPLY                                    = "RequestReply";

    /**
     * GreenVulcano communication <tt>Request</tt> paradigm
     */
    public static final String GV_REQUEST                                          = "Request";

    /**
     * GreenVulcano communication <tt>SendReply</tt> paradigm
     */
    public static final String GV_SEND_REPLY                                       = "SendReply";

    /**
     * GreenVulcano communication <tt>GetRequest</tt> paradigm
     */
    public static final String GV_GET_REQUEST                                      = "GetRequest";

    /**
     * GreenVulcano communication <tt>GetReply</tt> paradigm
     */
    public static final String GV_GET_REPLY                                        = "GetReply";

    /**
     * XPath of <tt>CharacterEncoding</tt> attribute of
     * <tt>InboundCommunications</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_COMMUNICATIONS_CHAR_ENCODING_ATTR_XPATH     = "/AdapterHTTP/InboundCommunications/@CharacterEncoding";

    /**
     * XPath of <tt>ParamEntrySeparator</tt> attribute of
     * <tt>InboundRequestParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_REQ_PARAMS_PARAM_ENTRY_SEP_ATTR_XPATH       = "/AdapterHTTP/InboundCommunications/InboundRequestParams/@ParamEntrySeparator";

    /**
     * XPath of <tt>ParamNameValueSeparator</tt> attribute of
     * <tt>InboundRequestParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_REQ_PARAMS_PARAM_NAMEVALUE_SEP_ATTR_XPATH   = "/AdapterHTTP/InboundCommunications/InboundRequestParams/@ParamNameValueSeparator";

    /**
     * XPath of a <tt>Transaction</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_TRANSACTION_XPATH                           = "/AdapterHTTP/InboundCommunications/InboundTransactions/Transaction";

    /**
     * XPath of a <tt>InboundRequestParam</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_REQ_PARAM_XPATH                             = "/AdapterHTTP/InboundCommunications/InboundRequestParams/InboundRequestParam";

    /**
     * XPath of a <tt>InboundRequestContent</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_REQ_CONTENT_XPATH                           = "/AdapterHTTP/InboundCommunications/InboundRequestContent";

    /**
     * XPath of a <tt>GVBufferFieldDefaultValue</tt> section within AdapterHTTP
     * XML configuration file
     */
    public static final String INBOUND_REQ_GVDATA_DEFAULTS_XPATH                   = "/AdapterHTTP/InboundCommunications/InputGVBufferDefaultValues/GVBufferFieldDefaultValue";

    /**
     * XPath of a <tt>GVBufferPropertyDefaultValue</tt> section within
     * AdapterHTTP XML configuration file
     */
    public static final String INBOUND_REQ_GVBUFFER_PROPERTY_DEFAULTS_XPATH        = "/AdapterHTTP/InboundCommunications/InputGVBufferDefaultValues/GVBufferPropertyDefaultValue";

    /**
     * XPath of a <tt>OpTypeDefaultValue</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_REQ_OPTYPE_DEFAULT_XPATH                    = "/AdapterHTTP/InboundCommunications/OpTypeDefaultValue";

    /**
     * XPath of <tt>EncodeParams</tt> attribute of
     * <tt>InboundResponseParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_RESP_PARAMS_ENCODE_ATTR_XPATH               = "/AdapterHTTP/InboundCommunications/InboundResponseParams/@EncodeParams";

    /**
     * XPath of <tt>ParamEntrySeparator</tt> attribute of
     * <tt>InboundResponseParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_RESP_PARAMS_PARAM_ENTRY_SEP_ATTR_XPATH      = "/AdapterHTTP/InboundCommunications/InboundResponseParams/@ParamEntrySeparator";

    /**
     * XPath of <tt>ParamNameValueSeparator</tt> attribute of
     * <tt>InboundResponseParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_RESP_PARAMS_PARAM_NAMEVALUE_SEP_ATTR_XPATH  = "/AdapterHTTP/InboundCommunications/InboundResponseParams/@ParamNameValueSeparator";

    /**
     * XPath of a <tt>InboundResponseParam</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String INBOUND_RESP_PARAM_XPATH                            = "/AdapterHTTP/InboundCommunications/InboundResponseParams/InboundResponseParam";

    /**
     * XPath of <tt>CharacterEncoding</tt> attribute of
     * <tt>OutboundCommunications</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_COMMUNICATIONS_CHAR_ENCODING_ATTR_XPATH    = "/AdapterHTTP/OutboundCommunications/@CharacterEncoding";

    /**
     * XPath of <tt>EncodeParams</tt> attribute of
     * <tt>OutboundRequestParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_REQ_PARAMS_ENCODE_ATTR_XPATH               = "/AdapterHTTP/OutboundCommunications/OutboundRequestParams/@EncodeParams";

    /**
     * XPath of <tt>ParamEntrySeparator</tt> attribute of
     * <tt>OutboundRequestParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_REQ_PARAMS_PARAM_ENTRY_SEP_ATTR_XPATH      = "/AdapterHTTP/OutboundCommunications/OutboundRequestParams/@ParamEntrySeparator";

    /**
     * XPath of <tt>ParamNameValueSeparator</tt> attribute of
     * <tt>OutboundRequestParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_REQ_PARAMS_PARAM_NAMEVALUE_SEP_ATTR_XPATH  = "/AdapterHTTP/OutboundCommunications/OutboundRequestParams/@ParamNameValueSeparator";

    /**
     * XPath of a <tt>OutboundRequestParam</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_REQ_PARAM_XPATH                            = "/AdapterHTTP/OutboundCommunications/OutboundRequestParams/OutboundRequestParam";

    /**
     * XPath of <tt>ParamEntrySeparator</tt> attribute of
     * <tt>OutboundResponseParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_RESP_PARAMS_PARAM_ENTRY_SEP_ATTR_XPATH     = "/AdapterHTTP/OutboundCommunications/OutboundResponseParams/@ParamEntrySeparator";

    /**
     * XPath of <tt>ParamNameValueSeparator</tt> attribute of
     * <tt>OutboundResponseParams</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_RESP_PARAMS_PARAM_NAMEVALUE_SEP_ATTR_XPATH = "/AdapterHTTP/OutboundCommunications/OutboundResponseParams/@ParamNameValueSeparator";

    /**
     * XPath of a <tt>OutboundResponseParam</tt> section within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_RESP_PARAM_XPATH                           = "/AdapterHTTP/OutboundCommunications/OutboundResponseParams/OutboundResponseParam";

    /**
     * XPath of a <tt>OutboundResponseContent</tt> section within AdapterHTTP
     * XML configuration file
     */
    public static final String OUTBOUND_RESP_CONTENT_XPATH                         = "/AdapterHTTP/OutboundCommunications/OutboundResponseContent";

    /**
     * XPath of a <tt>GVBufferFieldDefaultValue</tt> section within AdapterHTTP
     * XML configuration file
     */
    public static final String OUTBOUND_RESP_GVDATA_DEFAULTS_XPATH                 = "/AdapterHTTP/OutboundCommunications/OutputGVBufferDefaultValues/GVBufferFieldDefaultValue";

    /**
     * XPath of a <tt>GVBufferPropertyDefaultValue</tt> section within
     * AdapterHTTP XML configuration file
     */
    public static final String OUTBOUND_RESP_GVBUFFER_PROPERTY_DEFAULTS_XPATH             = "/AdapterHTTP/OutboundCommunications/OutputGVBufferDefaultValues/GVBufferPropertyDefaultValue";

    /**
     * XPath of SystemAuthentication section within AdapterHTTP XML
     * configuration file
     */
    public static final String SYSTEM_AUTHENTICATION_SECTION_NODE_XPATH            = "/AdapterHTTP/SystemAuthentication";

    /**
     * XPath of UserSystem section within AdapterHTTP XML configuration file
     */
    public static final String USER_SYSTEM_SECTION_NODE_XPATH                      = "/AdapterHTTP/SystemAuthentication/UserSystem";

    /**
     * XPath of UserService section within AdapterHTTP XML configuration file
     */
    public static final String USER_SERVICE_SECTION_NODE_XPATH                     = "/AdapterHTTP/SystemAuthentication/UserService";

    /**
     * System Authentication <i>AuthType</i> attribute XPath within AdapterHTTP
     * XML configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String AUTHENTICATION_TYPE_XPATH                           = "./@AuthType";

    /**
     * System Authentication <i>CookieDateFormat</i> attribute XPath within
     * AdapterHTTP XML configuration file (relative to
     * <i>SystemAuthentication</i> node)
     */
    public static final String COOKIE_DATE_FORMAT_XPATH                            = "./@CookieDateFormat";

    /**
     * <tt>HttpUser</tt> <i>name</i> attribute XPath within AdapterHTTP XML
     * configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_NAME_XPATH                                = "./@Name";

    /**
     * <tt>HttpUser</tt> <i>User</i> attribute XPath within AdapterHTTP XML
     * configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_USERNAME_XPATH                            = "./Credentials/@User";

    /**
     * <tt>HttpUser</tt> <i>Password</i> attribute XPath within AdapterHTTP XML
     * configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_PWD_XPATH                                 = "./Credentials/@Pwd";

    /**
     * <tt>HttpUser</tt> <i>URL</i> attribute XPath within AdapterHTTP XML
     * configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_URL_XPATH                                 = "./Credentials/@Url";

    /**
     * <tt>HttpUser</tt> <i>LoginString</i> element XPath within AdapterHTTP XML
     * configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_LOGIN_STRING_XPATH                        = "./LoginString";

    /**
     * <tt>HttpUser</tt> <i>LoginStringParam</i> element XPath within
     * AdapterHTTP XML configuration file (relative to <i>LoginString</i> node)
     */
    public static final String HTTP_USER_LOGIN_STRING_PARAM_XPATH                  = "./LoginStringParam";

    /**
     * <tt>HttpUser</tt> <i>LogoutString</i> element XPath within AdapterHTTP
     * XML configuration file (relative to <i>SystemAuthentication</i> node)
     */
    public static final String HTTP_USER_LOGOUT_STRING_XPATH                       = "./LogoutString";

    /**
     * <tt>HttpUser</tt> <i>LogoutStringParam</i> element XPath within
     * AdapterHTTP XML configuration file (relative to <i>LogoutString</i> node)
     */
    public static final String HTTP_USER_LOGOUT_STRING_PARAM_XPATH                 = "./LogoutStringParam";

    /**
     * <tt>ItemType</tt> attribute name within AdapterHTTP XML configuration
     * file
     */
    public static final String ITEM_TYPE_ATTR_NAME                                 = "ItemType";

    /**
     * <tt>ItemType</tt> attribute value <i>'Handler'</i> within AdapterHTTP XML
     * configuration file
     */
    public static final String ITEM_TYPE_HANDLER_VALUE                             = "Handler";

    /**
     * <tt>ItemType</tt> attribute value <i>'ACKHandler'</i> within AdapterHTTP
     * XML configuration file
     */
    public static final String ITEM_TYPE_ACK_HANDLER_VALUE                         = "ACKHandler";

    /**
     * <tt>ItemType</tt> attribute value <i>'ErrorHandler'</i> within
     * AdapterHTTP XML configuration file
     */
    public static final String ITEM_TYPE_ERROR_HANDLER_VALUE                       = "ErrorHandler";

    /**
     * <tt>FieldName</tt> attribute name within AdapterHTTP XML configuration
     * file
     */
    public static final String FIELDNAME_ATTR_NAME                                 = "FieldName";

    /**
     * <tt>FieldValue</tt> attribute name within AdapterHTTP XML configuration
     * file
     */
    public static final String FIELDVALUE_ATTR_NAME                                = "FieldValue";

    /**
     * <tt>HandlerClassname</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_CLASSNAME_ATTR_NAME                         = "HandlerClassname";

    /**
     * <tt>ParamFormat</tt> attribute name within AdapterHTTP XML configuration
     * file
     */
    public static final String PARAM_FORMAT_ATTR_NAME                              = "ParamFormat";

    /**
     * <tt>CommunicationURL</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_COMM_URL_ATTR_NAME                         = "CommunicationURL";

    /**
     * <tt>CommunicationTimeout</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_COMM_TIMEOUT_ATTR_NAME                     = "CommunicationTimeout";

    /**
     * <tt>ContentType</tt> attribute name within AdapterHTTP XML configuration
     * file
     */
    public static final String OUTBOUND_COMM_CONTENT_TYPE_ATTR_NAME                = "ContentType";

    /**
     * <tt>RecoveryAttempts</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String OUTBOUND_COMM_RECOVERY_ATTEMPTS_ATTR_NAME           = "CommunicationRecoveryAttempts";

    /**
     * <tt>Request/Response parameter name</tt> attribute name within
     * AdapterHTTP XML configuration file
     */
    public static final String PARAM_NAME_ATTR_NAME                                = "Name";

    /**
     * <tt>Value</tt> attribute name within AdapterHTTP XML configuration file
     */
    public static final String PARAM_VALUE_ATTR_NAME                               = "Value";

    /**
     * <tt>SendParameterName</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String PARAM_SENDPARAMNAME_ATTR_NAME                       = "SendParameterName";

    /**
     * <tt>Authentication type</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String AUTHENTICATION_TYPE_ATTR_NAME                       = "AuthType";

    /**
     * <tt>Authentication type</tt> attribute value 'System' within AdapterHTTP
     * XML configuration file
     */
    public static final String AUTHENTICATION_TYPE_SYSTEM_VALUE                    = "System";

    /**
     * <tt>Authentication type</tt> attribute value 'Service' within AdapterHTTP
     * XML configuration file
     */
    public static final String AUTHENTICATION_TYPE_SERVICE_VALUE                   = "Service";

    /**
     * <tt>Authentication type</tt> attribute value 'None' within AdapterHTTP
     * XML configuration file
     */
    public static final String AUTHENTICATION_TYPE_NONE_VALUE                      = "None";

    /**
     * <tt>Cookie Date Format</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String COOKIE_FORMAT_ATTR_NAME                             = "CookieDateFormat";

    /**
     * EBAdapterHttp XML configuration file <tt>UserSystem</tt> element name.
     */
    public static final String USER_SYSTEM_ELEM_NAME                               = "UserSystem";

    /**
     * EBAdapterHttp XML configuration file <tt>UserService</tt> element name.
     */
    public static final String USER_SERVICE_ELEM_NAME                              = "UserService";

    /**
     * <tt>LoginRecoveryAttempts</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String LOGIN_RECOVERY_ATTEMPTS_ATTR_NAME                   = "LoginRecoveryAttempts";

    /**
     * <tt>LogoutRecoveryAttempts</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String LOGOUT_RECOVERY_ATTEMPTS_ATTR_NAME                  = "LogoutRecoveryAttempts";

    /**
     * <tt>Handler output type</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_ATTR_NAME                       = "OutputType";

    /**
     * <tt>Handler output type</tt> value "GVBuffer" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_GVDATA                    = "GVBuffer";

    /**
     * <tt>Handler output type</tt> value "OpType" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_OPTYPE                    = "OpType";

    /**
     * <tt>Handler output type</tt> value "HttpParam" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_HTTPPARAM                 = "HttpParam";

    /**
     * <tt>FlatParam</tt> element tagname within AdapterHTTP XML configuration
     * file
     */
    public static final String SPECIFIC_HANDLER_FLAT_PARAM_TAGNAME                 = "FlatParam";

    /**
     * <tt>SeparatorChar</tt> attribute name within AdapterHTTP XML
     * configuration file
     */
    public static final String SPECIFIC_HANDLER_SEPARATOR_CHAR_ATTR_NAME           = "SeparatorChar";

    /**
     * <tt>Length</tt> attribute name within AdapterHTTP XML configuration file
     */
    public static final String SPECIFIC_HANDLER_MAPPING_LENGTH_ATTR_NAME           = "Length";

    /**
     * <tt>XPath</tt> attribute name within <tt>XMLParamMapping</tt> tag of
     * AdapterHTTP XML configuration file
     */
    public static final String XMLMAPPING_XPATH_ATTR_NAME                          = "XPath";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.system</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_SYSTEM                       = "GVBuffer.system";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.service</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_SERVICE                      = "GVBuffer.service";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.ID</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_ID                           = "GVBuffer.ID";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.retCode</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_RETCODE                      = "GVBuffer.retCode";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.object</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_GVBUFFER                     = "GVBuffer.object";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.property</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_PROPERTY                     = "GVBuffer.property";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.property.list</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_PROPERTYLIST                 = "GVBuffer.property.list";

    /**
     * <tt>Variable</tt> attribute value <tt>OpType</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_OPTYPE                       = "OpType";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.system</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOSYSTEM              = "GVTransInfo.system";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.service</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOSERVICE             = "GVTransInfo.service";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.TID</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOTID                 = "GVTransInfo.TID";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.errorCode</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOERRORCODE           = "GVTransInfo.errorCode";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.errorMessage</tt>
     * within <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration
     * file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOERRORMESSAGE        = "GVTransInfo.errorMessage";

    /**
     * Log4j object used to write the logs
     */
    private static Logger      logger                                              = GVLogger.getLogger(AdapterHttpConfig.class);

    /**
     * Performs a 'logical' check of AdapterHttp XML configuration file limited
     * to communication configuration (checking everything that can't be checked
     * through XML validation). Doesn't perform XML validation (that should be
     * done by GV configuration tool).
     *
     * @param configFilename
     *        the name of the HTTP Adapter configuration file.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    public static void checkInboundConfiguration(String configFilename) throws AdapterHttpConfigurationException
    {
        try {
            String inboundCommNodeXPath = CONFIG_FILE_ROOT_ELEM_XPATH + "/" + CONFIG_FILE_INBOUND_COMM_ELEM_NAME;
            Node inboundCommNode = XMLConfig.getNode(configFilename, inboundCommNodeXPath);
            if (inboundCommNode != null) {
                NodeList inboundTransactionNodes = XMLConfig.getNodeList(configFilename, INBOUND_TRANSACTION_XPATH);
                NodeList inboundReqParamNodes = XMLConfig.getNodeList(configFilename, INBOUND_REQ_PARAM_XPATH);
                NodeList inboundRespParamNodes = XMLConfig.getNodeList(configFilename, INBOUND_RESP_PARAM_XPATH);

                checkInboundTransactions(inboundTransactionNodes);
                checkInboundRequestParams(inboundReqParamNodes);
                checkInboundResponseParams(inboundRespParamNodes);
            }
            else {
                logger.error("checkInboundConfiguration - Inbound communication NOT configured");
                throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                        "message", "Inbound communication NOT configured"}});
            }
        }
        catch (XMLConfigException exc) {
            logger.error("checkInboundConfiguration - Error while retrieving configuration informations: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Error while retrieving configuration informations"}}, exc);
        }
        catch (AdapterHttpConfigurationException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("checkInboundConfiguration - Runtime error during configuration check: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Runtime error during configuration check"}}, exc);
        }
    }

    /**
     * Performs a 'logical' check of AdapterHttp XML configuration file limited
     * to outbound communication configuration (checking everything that can't
     * be checked through XML validation). Doesn't perform XML validation (that
     * should be done by GV configuration tool).
     *
     * @param configFilename
     *        the name of the HTTP Adapter configuration file.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    public static void checkOutboundConfiguration(String configFilename) throws AdapterHttpConfigurationException
    {
        try {
            String outboundCommNodeXPath = CONFIG_FILE_ROOT_ELEM_XPATH + "/" + CONFIG_FILE_OUTBOUND_COMM_ELEM_NAME;
            Node outboundCommNode = XMLConfig.getNode(configFilename, outboundCommNodeXPath);
            if (outboundCommNode != null) {
                NodeList outboundReqParamNodes = XMLConfig.getNodeList(configFilename, OUTBOUND_REQ_PARAM_XPATH);
                NodeList outboundRespParamNodes = XMLConfig.getNodeList(configFilename, OUTBOUND_RESP_PARAM_XPATH);
                checkOutboundRequestParams(outboundReqParamNodes);
                checkOutboundResponseParams(outboundRespParamNodes);
            }
            else {
                logger.error("checkOutboundConfiguration - outbound communication NOT configured");
                throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                        "message", "Outbound communication NOT configured"}});
            }
        }
        catch (XMLConfigException exc) {
            logger.error("checkOutboundConfiguration - Error while retrieving configuration informations: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Error while retrieving configuration informations"}}, exc);
        }
        catch (AdapterHttpConfigurationException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("checkOutboundConfiguration - Runtime error during configuration check: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Runtime error during configuration check"}}, exc);
        }
    }

    /**
     * Performs a 'logical' check of AdapterHttp XML configuration file limited
     * to outbound authentication configuration (checking everything that can't
     * be checked through XML validation). Doesn't perform XML validation (that
     * should be done by GV configuration tool).
     *
     * @param configFilename
     *        the name of the HTTP Adapter configuration file.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    public static void checkAuthenticationConfiguration(String configFilename) throws AdapterHttpConfigurationException
    {
        try {
            String authenticationNodeXPath = SYSTEM_AUTHENTICATION_SECTION_NODE_XPATH;
            Node authenticationNode = XMLConfig.getNode(configFilename, authenticationNodeXPath);
            if (authenticationNode != null) {
                checkAuthenticationConfiguration((Element) authenticationNode);
            }
            else {
                logger.error("checkAuthenticationConfiguration - outbound authentication NOT configured");
                throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                        "message", "Outbound authentication NOT configured"}});
            }
        }
        catch (XMLConfigException exc) {
            logger.error("checkAuthenticationConfiguration - Error while retrieving configuration informations: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Error while retrieving configuration informations"}}, exc);
        }
        catch (AdapterHttpConfigurationException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("checkAuthenticationConfiguration - Runtime error during configuration check: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Runtime error during configuration check"}}, exc);
        }
    }

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing inbound transactions configuration info
     * (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing inbound transaction
     *        configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkInboundTransactions(NodeList paramNodes) throws AdapterHttpConfigurationException
    {
        // do nothing
    }

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing inbound request parameters configuration
     * info (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing inbound request
     *        parameters configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkInboundRequestParams(NodeList paramNodes) throws AdapterHttpConfigurationException
    {
        // TO BE RE-IMPLEMENTED WHEN CONFIGURATION DTD WILL BE STABLE
    }

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing inbound response parameters configuration
     * info (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing inbound response
     *        parameters configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkInboundResponseParams(NodeList paramNodes) throws AdapterHttpConfigurationException
    {
        // TO BE RE-IMPLEMENTED WHEN CONFIGURATION DTD WILL BE STABLE
    }

    /**
     * ---------- Outbound communication configuration -------------
     */

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing outbound response parameters configuration
     * info (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing outbound response
     *        parameters configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkOutboundRequestParams(NodeList paramNodes) throws AdapterHttpConfigurationException
    {
        // TO BE RE-IMPLEMENTED WHEN CONFIGURATION DTD WILL BE STABLE
    }

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing outbound response parameters configuration
     * info (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing outbound response
     *        parameters configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkOutboundResponseParams(NodeList paramNodes) throws AdapterHttpConfigurationException
    {
        // TO BE RE-IMPLEMENTED WHEN CONFIGURATION DTD WILL BE STABLE
    }

    /**
     * Performs a 'logical' check of the section of AdapterHttp XML
     * configuration file containing System Authentication configuration info
     * (checking everything that can't be checked through XML validation).
     *
     * @param section
     *        the DOM <tt>Element</tt> corresponding to the section of
     *        AdapterHttp XML configuration file containing System
     *        Authentication configuration info
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during check.
     */
    private static void checkAuthenticationConfiguration(Element section) throws AdapterHttpConfigurationException
    {
        boolean isValid = true;
        try {
            NodeList userServiceNodes = XMLConfig.getNodeList(section, "./" + USER_SERVICE_ELEM_NAME);
            NodeList userSystemNodes = XMLConfig.getNodeList(section, "./" + USER_SYSTEM_ELEM_NAME);
            String authType = section.getAttribute(AUTHENTICATION_TYPE_ATTR_NAME);
            String cookieFormat = section.getAttribute(COOKIE_FORMAT_ATTR_NAME);

            if (authType.equals(AUTHENTICATION_TYPE_SYSTEM_VALUE)) {
                // Authentication done at a system level:
                // - there must be NO UserService tags and only one UserSystem
                // tag;
                // - attribute 'CookieDateFormat' MUST be present
                logger.debug("checkAuthenticationConfiguration - authentication done at SYSTEM level");
                if ((userServiceNodes.getLength() > 0) || (userSystemNodes.getLength() != 1)) {
                    logger.error("checkAuthenticationConfiguration - Error in SYSTEM level authentication configuration");
                    isValid = false;
                }
                if ((cookieFormat == null) || cookieFormat.equals("")) {
                    logger.error("checkAuthenticationConfiguration - Error in SYSTEM level authentication configuration: no cookie date format specified");
                    isValid = false;
                }

            }
            else if (authType.equals(AUTHENTICATION_TYPE_SERVICE_VALUE)) {
                // Authentication done at a service level:
                // - there must be NO UserSystem tags and at least 1 UserService
                // tag
                // - attribute 'CookieDateFormat' MUST be present
                logger.debug("checkAuthenticationConfiguration - authentication done at SERVICE level");
                if ((userSystemNodes.getLength() > 0) || (userServiceNodes.getLength() < 1)) {
                    logger.error("checkAuthenticationConfiguration - Error in SERVICE level authentication configuration");
                    isValid = false;
                }
                if ((cookieFormat == null) || cookieFormat.equals("")) {
                    logger.error("checkAuthenticationConfiguration - Error in SERVICE level authentication configuration: no cookie date format specified");
                    isValid = false;
                }

            }
            else if (authType.equals(AUTHENTICATION_TYPE_NONE_VALUE)) {
                // No outbound authentication is required:
                // there must be NO UserSystem tags and NO UserService tags
                logger.debug("checkAuthenticationConfiguration - NO outbound authentication is required");
                if ((userSystemNodes.getLength() > 0) || (userServiceNodes.getLength() > 0)) {
                    logger.error("checkAuthenticationConfiguration - Error in NONE level authentication configuration");
                    isValid = false;
                }
            }

            if (!isValid) {
                throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                        "message", "Error during outbound authentication configuration check"}});
            }

        }
        catch (XMLConfigException exc) {
            logger.error("checkAuthenticationConfiguration - Error while retrieving configuration informations: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Error while retrieving configuration informations"}}, exc);
        }
        catch (AdapterHttpConfigurationException exc) {
            logger.error("checkAuthenticationConfiguration - Error during outbound authentication configuration check: "
                    + exc);
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("checkAuthenticationConfiguration - Runtime error during configuration check: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_CONFIGURATION_ERROR", new String[][]{{
                    "message", "Runtime error during configuration check"}}, exc);
        }
    }
}
