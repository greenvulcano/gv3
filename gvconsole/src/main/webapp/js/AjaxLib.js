
// variabili globali
var handler=null;

var divPerMostrareInfo = "loadingDiv";

var serverUrl =document.location.protocol +"//"+ document.location.hostname +":"+document.location.port ;

// creazione oggetto XmlHttpReq
function createXmlHttpReq(handler)
{
    var agent = navigator.userAgent.toLowerCase();
    var is_ie5 = (agent.indexOf('msie 5') != -1);
    var http = null;
    
    try
    {
        http = new XMLHttpRequest();
    }
    catch(e)
    {
        try
        {
            var axObject = (is_ie5) ? "Microsoft.XMLHTTP" : "Msxml2.XMLHTTP";
            http = new ActiveXObject(axObject);
        }
        catch(e)
        {
            alert("Could not create XMLHttpRequest:"+e.description);
            http = null;
        }
    }
    if (http!=null && handler!=null)
    {
        http.onreadystatechange = handler;
    }
    
    return http;
}


function mostraDifferenzeXML(servizio){
	alert(servizio);
	urlServlet = "mostraDifferenzeXmlAction";
	var url = serverUrl + "/" + urlServlet + "?idServizio=" + escape(servizio);

	    req = createXmlHttpReq(responseOnHandler);
	    
	    alert("req.open(\"GET\", "+url + ")");
	    
	    // simulo chiamata AJAX
	    document.getElementById("xmlNEW").innerHTML = "nuovo XML di ESEMPIO";
	    document.getElementById("xmlOLD").innerHTML = "vecchio XML di ESEMPIO";
	    
/*	    req.open("GET", url);
	    if (req!=null && handler!=null)
	    {
	        http.onreadystatechange = handler;
	    }
	    req.send(null);
*/
	}

function responseOnHandler(){
    //divPerMostrareRisultato = 'risultatoTestAjax';
	divPerMostrareRisultato = "xmlNEW";
    if (req && req.readyState == 4)
    {
        if (req.status == 200)
        {
            //document.getElementById('loadingDiv').style.visibility = "hidden";
            document.getElementById(divPerMostrareRisultato).innerHTML = req.responseText;
            
        }
    }
    else {
            //document.getElementById(divPerMostrareRisultato).value = "Caricamento in corso... " + req.readyState;
            //document.getElementById('loadingDiv').style.visibility = "visible";
    }
}
