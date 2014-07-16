function prettyForms(){
	fixInputText();
	fixPasswordText();
	fixSubmits();
}
	
// functions that apply the look to the form elements

	//this function is run for all form elements
	//this function accepts one element, and wraps it in four divs that are styled with shadows
	function appendParentsTo(currItem){
		//create the divs
		tl = document.createElement("div");
		br = document.createElement("div");
		bl = document.createElement("div");
		tr = document.createElement("div");
		if(document.all){							//IE
			//give them the proper class
			tl.className="frmShdwTopLt";
			br.className="frmShdwBottomRt";
			bl.className="frmShdwBottomLt";
			tr.className="frmShdwTopRt";
			//insert the top level div
			t1=currItem.insertAdjacentElement("BeforeBegin",tl);
		}else{										//FFX
			//give them the proper class
			tl.setAttribute("class", "frmShdwTopLt");
			br.setAttribute("class", "frmShdwBottomRt");
			bl.setAttribute("class", "frmShdwBottomLt");
			tr.setAttribute("class", "frmShdwTopRt");
			inputParent = currItem.parentNode;
			//insert the top level div
			tl = inputParent.insertBefore(tl, currItem);
		}		
		//append children
		br = tl.appendChild(br);
		bl = br.appendChild(bl);
		tr = bl.appendChild(tr);
		//move input to child of divs
		tr.appendChild(currItem);
	}

	//apply look to text boxes
	function fixInputText(){
		inputs = document.getElementsByTagName("input");
		for(i=0;i<inputs.length;i++){
			if(inputs[i].type=="text"){
				appendParentsTo(inputs[i]);
			}
		}
	}
	
	//apply look to password boxes
	function fixPasswordText(){
		inputs = document.getElementsByTagName("input");
		for(i=0;i<inputs.length;i++){
			if(inputs[i].type=="password"){
				appendParentsTo(inputs[i]);
			}
		}
	}

	//apply look to submit buttons
	function fixSubmits(){
		inputs = document.getElementsByTagName("input");
		for(i=0;i<inputs.length;i++){
			if(inputs[i].type=="submit"){
				appendParentsTo(inputs[i]);
				inputs[i].className="frmShdwSubmit";
			}
		}
	}		