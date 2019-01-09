//Set the tool tip message you want for each link here.
function move(fbox, tbox) 
	{
		var arrFbox = new Array();
		var arrTbox = new Array();
		var arrLookup = new Array();
		var i;
		
		for (i = 0; i < tbox.options.length; i++) 
		{
			arrLookup[tbox.options[i].text] = tbox.options[i].value;
			arrTbox[i] = tbox.options[i].text;
		}
		
		var fLength = 0;
		var tLength = arrTbox.length;
		for(i = 0; i < fbox.options.length; i++) 
		{
			arrLookup[fbox.options[i].text] = fbox.options[i].value;
			if (fbox.options[i].selected && fbox.options[i].value != "") 
			{
				arrTbox[tLength] = fbox.options[i].text;
				tLength++;
			}
			else 
			{
				arrFbox[fLength] = fbox.options[i].text;
				fLength++;
			}
		}
		
		arrFbox.sort();
		arrTbox.sort();
		fbox.length = 0;
		tbox.length = 0;
		var c;
		
		for(c = 0; c < arrFbox.length; c++) 
		{
			var no = new Option();
			no.value = arrLookup[arrFbox[c]];
			no.text = arrFbox[c];
			fbox[c] = no;
		}
		
		for(c = 0; c < arrTbox.length; c++) 
		{
			var no = new Option();
			no.value = arrLookup[arrTbox[c]];
			no.text = arrTbox[c];
			tbox[c] = no;
		}
	}
	
	function check(field) {
	   if(field.length > 1)
	     {
		  for (j = 0; j < field.length; j++) {
		  	field[j].checked = true;
		  }
	     }
	   else
	      field.checked = true; 		  
	}
	
	function uncheck(field) {
	   if(field.length > 1)
	     {
		  for (i = 0; i < field.length; i++) {
		  	field[i].checked = false;
		  }
	     }
	     else
	      field.checked = false; 	 	      
	}
	
	function DoComboSelection(tbox)
	{
		if(tbox.options != null)
		{
			for (i = 0; i < tbox.options.length; i++) 
			{
				tbox.options[i].selected = true;
			}
		}
	}
	
	function DoAllSelection(src, dest)
	{
		DoComboSelection(src);
		move(src,dest);		
	}
	
	function form_control(theForm)
	{
		DoComboSelection(theForm.src);
		if (theForm.src.value) {
			if (theForm.src.value == 0)
			{
				alert( 'No element selected' );
				theForm.src.focus();
				return false;
			}
		} else if(theForm.src.length) {
			var flag = false;
			for (i=0; i<theForm.src.length; i++) {
				if(theForm.src.item(i).checked) {
					flag = true;
				}
			}			
			if(!flag) {
				alert( 'No element selected' );
				return false;
			}
		} else if(theForm.src.checked) {
			if(!theForm.src.checked) {
				alert( 'No element selected' );
				return false;
			}
		}
		if (confirm("Do you want to confirm the operation?"))      
				theForm.submit();
		else return false;
		
	}
    
		   	function checkAll(theForm)
			{
				theForm.throughputSelected[0].checked = true;
				theForm.throughputSelected[1].checked = true;
				theForm.throughputSelected[2].checked = true;
				theForm.throughputSelected[3].checked = true;
		    }
		    
			function checkAllNodes(theForm)
			{
				theForm.throughputSelected[0].checked = true;
				theForm.throughputSelected[1].checked = false;
				theForm.throughputSelected[2].checked = true;
				theForm.throughputSelected[3].checked = false;
			}
			
			function checkAllServices(theForm)
			{
				theForm.throughputSelected[0].checked = false;
				theForm.throughputSelected[1].checked = true;
				theForm.throughputSelected[2].checked = false;
				theForm.throughputSelected[3].checked = true;
		    }
		    
   			function openWindow(theForm)
			{
			    var locationValues = theForm.locationValues;
			    var locationValues = theForm.locationValues;
			    var flag = false;
			    var i=0;
				for (i=0; i<locationValues.length; i++) {
					if(locationValues[i].checked) {
						flag = true;
					}
				}			
				if(!flag) {
					alert( 'No location selected' );
					return;
				}
				theForm.submit();    
			    var selectedWindows = theForm.graphicWindow;
			    var j=0;
				for (j=0; j<selectedWindows.length; j++) {
			    	var selectedWindow = selectedWindows[j];			
			    	if (selectedWindow.checked) {
						if (selectedWindow.value == "unique")
						{    
							window.open("monitoring/throughput/applet.jsp", "_blank", "left=50,top=50,width=340,height=260,resizable, scrollbars");
						}
						else {
							for (i = 0; i < theForm.locationValues.length; i++) 
							{
								if(locationValues[i].checked) {
									theForm.location.value = theForm.locationValues[i].value;
									var loc = theForm.locationValues[i].value;
									window.open("monitoring/throughput/applet.jsp?location=" + loc , "_blank", "left=50,top=50,width=340,height=260,resizable, scrollbars");
								}
							}
					}
				}
			 }
		}

	function viewWindow(src, dest)
	{
		window.open("MoveMessagesAction.do?action=viewMessage&source=" + src +"&dest=" + dest, "_blank", "left=50,top=50,width=740,height=860,resizable, scrollbars");
	}
	
	function targetBlank (url) {
		blankWin = window.open(url,'_blank','left=50,top=50,width=540,height=460,resizable, scrollbars');
	}
	
	function remove_field(id){
		// remove all children from element
		var element = document.getElementById(id);
		while (element.hasChildNodes()) {
  			element.removeChild(element.firstChild);
		}
	}
	
	function copyValue(elemSource, elemDest){
		elemDest.value = elemSource.value;
	}
