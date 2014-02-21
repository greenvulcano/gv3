var line;
var domMap;
var maxAttributeFields = 20;
var ind = 0;

function initPage()
{
	window.returnValue = "";
	line = window.dialogArguments;

    attributeModel = line.getAttributeModel();
    var attributes = attributeModel.childNodes;    
    valuesAttr = getValuesAttrs(line.lineElement);
    isNew = (valuesAttr == null);
    
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
	line = window.dialogArguments;
	
	names = new Array();
	values = new Array();
    for (i = 0; i < ind; i++){        
        names.push(document.getElementById("label_"+i.toString()).value);        
        values.push(document.getElementById(i.toString()).value);        
	}

	line.setAttrValue(names, values);

	window.close();
}

function getValuesAttrs(line) {
    el = null;
    childNodes = line.childNodes;
    
    if (childNodes != null) {
		for (i = 0; i < childNodes.length; i++) {
		    if (childNodes.item(i).nodeName.toString() == 'values') {
		        el = childNodes.item(i);
		    }		        
		}
	}
	return el;
}