 
 function checkParent(src, tag) {
 	if (src.tagName != null)
	{
   	 	while ("HTML" != src.tagName) {
    	    if (tag == src.tagName)
        	  return src;
       		src = src.parentElement;
    	}
    	return null;
	}
 }

 function selectItem(list) {
    var el = checkParent(event.srcElement, "LI");

	if (el.tagName != null)
	{
 	   if ("LI" == el.tagName) {
    	   if (null != list.selected)
        	  list.selected.className = "";
  		   if (list.selected != el) {
              el.className = "selected";
 	          list.selected = el;
           }
           else
              list.selected = null;
           } 
	}
 }

 function copy(src, dest) {
    var elSrc = document.all[src];
    var elDest = document.all[dest];
    if (elSrc.selected != null) {
       elSrc.selected.className = "";
       elDest.insertAdjacentHTML("beforeEnd",
          elSrc.selected.outerHTML);
       elSrc.selected.outerHTML = "";
       elSrc.selected = null; // reset selection
    }
 }

