/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvrules.drools.config.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvrules.drools.RulesException;
import it.greenvulcano.gvesb.gvrules.drools.config.KnowledgeBaseConfig;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.SequentialOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 11/feb/2012
 * @author GreenVulcano Developer Team
 */
public class GVKnowledgeBaseConfig implements KnowledgeBaseConfig
{
    private static final Logger logger        = GVLogger.getLogger(GVKnowledgeBaseConfig.class);
    private KnowledgeBase       knowledgeBase = null;
    private String              name          = null;
    private String              changeSetDoc  = null;


    @Override
    public void init(Node node) throws RulesException
    {
        try {
            name = XMLConfig.get(node, "@name");
            logger.debug("BEGIN - Init GVKnowledgeBaseConfig[" + name + "]");
            buildRuleResources(node);
            logger.debug("GVKnowledgeBaseConfig[" + name + "]:\n" + changeSetDoc);
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newByteArrayResource(changeSetDoc.getBytes()), ResourceType.CHANGE_SET);

            if (kbuilder.hasErrors()) {
                throw new RulesException("Error initializing GVKnowledgeBaseConfig[" + name + "]: "
                        + kbuilder.getErrors().toString());
            }

            KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
            configuration.setOption(SequentialOption.YES);

            knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);
            knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }
        catch (RulesException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new RulesException("Error initializing GVKnowledgeBaseConfig[" + name + "]", exc);
        }
        finally {
            logger.debug("END - Init GVKnowledgeBaseConfig[" + name + "]");
        }
    }

    private void buildRuleResources(Node node) throws RulesException
    {
        String baseDoc = "<change-set xmlns='http://drools.org/drools-5.0/change-set'\n"
                + "xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'\n"
                + "xs:schemaLocation='http://drools.org/drools-5.0/change-set.xsd' >\n" + "<add></add>\n"
                + "</change-set>";
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Document chgSetDoc = parser.parseDOM(baseDoc, false, true);
            Node add = parser.selectSingleNode(chgSetDoc, "//drools-cs:add");
            NodeList ruleResList = XMLConfig.getNodeList(node, "RuleResource");
            for (int i = 0; i < ruleResList.getLength(); i++) {
                Node n = ruleResList.item(i);
                String rU = PropertiesHandler.expand(XMLConfig.get(n, "@url"));
                String rT = XMLConfig.get(n, "@resourceType");
                /*<resource source='file:/project/myrules.drl' type='DRL' />
                  <resource source='classpath:data/IntegrationExampleTest.xls' 
                            type="DTABLE">
                      <decisiontable-conf input-type="XLS" worksheet-name="Tables_2" />
                  </resource>*/
                Element res = chgSetDoc.createElementNS("http://drools.org/drools-5.0/change-set", "resource");
                res.setAttribute("source", rU);
                res.setAttribute("type", rT);
                add.appendChild(res);

                if ("DTABLE".equals(rT)) {
                    String iT = XMLConfig.get(n, "@inputType");
                    String wN = XMLConfig.get(n, "@worksheetName");

                    Element dt = chgSetDoc.createElementNS("http://drools.org/drools-5.0/change-set",
                            "decisiontable-conf");
                    dt.setAttribute("input-type", iT);
                    if ("XSL".equals(iT)) {
                        dt.setAttribute("worksheet-name", wN);
                    }
                    res.appendChild(dt);
                }
            }

            changeSetDoc = parser.serializeDOM(chgSetDoc, true, true);
        }
        catch (Exception exc) {
            throw new RulesException("Error initializing GVKnowledgeBaseConfig[" + name + "]", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public StatelessKnowledgeSession getStatelessKnowledgeSession() throws RulesException
    {
        return knowledgeBase.newStatelessKnowledgeSession();
    }

    @Override
    public StatefulKnowledgeSession getStatefulKnowledgeSession() throws RulesException
    {
        return knowledgeBase.newStatefulKnowledgeSession();
    }

    @Override
    public void destroy()
    {
        // do nothing
    }
}
