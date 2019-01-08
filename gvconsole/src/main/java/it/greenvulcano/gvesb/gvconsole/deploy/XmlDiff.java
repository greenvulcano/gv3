package it.greenvulcano.gvesb.gvconsole.deploy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
  *
  * Java program to compare two XML files using XMLUnit example

  * @author Javin Paul
  */
public class XmlDiff {
 
	public XmlDiff(){
		XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
	}
 
    public List<String> compareXML(Document source, Document target){
        List<String> listDiff= new ArrayList<String>();
        //creating Diff instance to compare two XML files
        Diff xmlDiff = new Diff(source, target);
     
        //for getting detailed differences between two xml files
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);
        for(int i=0; i<detailXmlDiff.getAllDifferences().size();i++){
        	Difference diff = (Difference) detailXmlDiff.getAllDifferences().get(i);
        	listDiff.add(diff.getTestNodeDetail().getXpathLocation()+":"+diff.getTestNodeDetail().getValue()+":"+diff.getDescription());
        }
        return listDiff;
    }
    public List<String> compareXML(Node source, Node target) throws ParserConfigurationException{
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(true);
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	Document sourceDocument = builder.newDocument();
    	Node importedNode = sourceDocument.importNode(source, true);
    	sourceDocument.appendChild(importedNode);
    	
    	Document targetDocument = builder.newDocument();
    	importedNode = targetDocument.importNode(target, true);
    	targetDocument.appendChild(importedNode);
    	return compareXML(sourceDocument,targetDocument);
    }
}
