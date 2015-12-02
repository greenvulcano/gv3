var task;
var ind = 0;

function initPage()
{
	window.returnValue = "";
	task = window.dialogArguments;
	
    valuesAttr = getValuesAttrs(task.taskElement.childNodes,task.taskElement.getAttribute("fd_type"));
	
	isNew = (valuesAttr == null);
	
	attributeModel = task.getAttributeModel();
	var attributes = attributeModel.childNodes;
	for (i = 0; i < attributes.length; i++) {
	    attributeNode = attributes.item(i);	
	    var name = attributeNode.selectSingleNode("@name").nodeValue;
	    document.getElementById("label_"+ind.toString()).innerText = name;
	    if (!isNew) {
		    value = valuesAttr.selectSingleNode("@" + name);
		    if (value != null)
		        document.getElementById(ind.toString()).innerText = value.nodeValue;
		}	    
	    ind++;
	}
	
}

function returnData()
{
	task = window.dialogArguments;
	valuesAttr = getValuesAttr(task.taskElement.childNodes);
	
	names = new Array();
	values = new Array();
    for (i = 0; i < ind; i++){        
        names.push(document.getElementById("label_"+i.toString()).value);
        values.push(document.getElementById(i.toString()).value);        
	}
	var saveType = false;
	var type = task.taskElement.getAttribute("fd_type");
	if (type == null && (valuesAttr != null))
	    saveType = false;
	else if (type == null && (valuesAttr == null))
	    saveType = true;
	else if (type != null && ((valuesAttr != null)))
	    saveType = true;
    task.setAttrValue(names, values, saveType);	
	window.close();
}


function getValuesAttr(childNodes) {
    if (childNodes != null) {
		for (i = 0; i < childNodes.length; i++) {
		    if (childNodes.item(i).nodeName.toString() == 'values') {
		        return childNodes.item(i);		        
		    }		        
		}
	}	
	return null;
}

function getValuesAttrs(childNodes, type) {
    el = null;
    if (childNodes != null) {
		for (i = 0; i < childNodes.length; i++) {
		    if (childNodes.item(i).nodeName.toString() == 'values') {
		        el = childNodes.item(i);		        
		    }		        
		}
	}
    if (type == null)
        return el;
    else {		
		
    	if (el != null) {
    	    var domDoc = new ActiveXObject("MSXML.DomDocument");
        	domDoc.async = false;
        	domDoc.loadXML("<values />");
        	
        	var root = domDoc.documentElement;
        	for(i = 0; i < el.attributes.length; i++) {
        	    if (el.attributes(i).nodeName.indexOf("__") != -1)
        	        root.setAttribute(el.attributes(i).nodeName.substring(2), el.attributes(i).nodeValue);
        	}
        	return root;
    	}
    }    
}