/*--------------------------------------------------|
| dMenu 1.00 |                                      |
|---------------------------------------------------|
| Copyright (c) 2003 Maxime Informatica             |
|                                                   |
| This script can be used freely as long as all     |
| copyright messages are intact.                    |
|                                                   |
| Updated: 05.10.2004                               |
|--------------------------------------------------*/

//---------------------------------------------------
// FIELDS
//---------------------------------------------------

// Some information about the browser
//
dMenu.ie4 = document.all && navigator.userAgent.indexOf("Opera") == -1;
dMenu.ns6 = document.getElementById && !document.all;

if (dMenu.ie4) {
    dMenu.menutableStyle = "menutable-ie";
}
if (dMenu.ns6) {
    dMenu.menutableStyle = "menutable-ns";
}

// Prologue and epilogue of the menu items
//
dMenu.startHtml = '<table class="' + dMenu.menutableStyle + '" width="100%" border="0" cellpadding="0" cellspacing="0">';
dMenu.endHtml = '</table>';

/**
 * Currently open menu
 */
dMenu.menuobj = null;
dMenu.sourceElement = null;

//---------------------------------------------------
// CONSTRUCTOR
//---------------------------------------------------

/**
 * Creates a dMenu object with a given name.
 *
 * @param px the X position in %. Optional. Default = 75.
 * @param py the Y position in %. Optional. Default = 75.
 * @param w the menu width. Optional. Default = 120.
 */
function dMenu(px, py, w)
{
    this.linkset = "";
    if(w != undefined) {
        this.width = w;
    }
    else {
        this.width = 120;
    }
    if(px != undefined) {
        this.px = px;
    }
    else {
        this.px = 75;
    }
    if(py != undefined) {
        this.py = py;
    }
    else {
        this.py = 75;
    }
    this.lastInserted = '';
    this.lastKind = null;
}

//---------------------------------------------------
// INSTANCE METHODS
//---------------------------------------------------

/**
 * Add a menu item to the menu.
 *
 * @param item the menu item (required)
 * @param url invoked url (required)
 * @param target target frame (optional)
 * @param img url to an image (optional)
 * @param title menu item description (optional)
 */
dMenu.prototype.add = function(item, url, target, img, title)
{
    if(target) {
        target = ' target="' + target + '"';
    }
    else {
        target = '';
    }

    if(img) {
        img = '<img border="0" align="middle" src="' + img + '"/>';
    }
    else {
        img = '';
    }

    if(title) {
        title = ' title="' + title + '"';
    }
    else {
        title = '';
    }

    if(this.lastInserted) {
        this.linkset += this.lastInserted;
        this.lastInserted = '';

        if(this.lastKind == "label") {
            this.linkset += '<tr height="8"><td></td></tr>';
        }
    }

    var aTag = '<a href="' + url + '"' + target + title + '>';

    this.linkset +=
          '<tr valign="middle" class="menuitems">'
        + '<td>&nbsp;&nbsp;</td>'
        + '<td align="center">'
        + aTag
        + img
        + '</a>'
        + '</td>'
        + '<td width="100%">'
        + '<table border="0" width="100%" cellpadding="2" cellspacing="0" class="' + dMenu.menutableStyle + '"><tr><td>'
        + '<nobr>'
        + aTag
        + item
        + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>'
        + '</nobr>'
        + '</td></tr></table>'
        + '</td>'
        + '</tr>';

    this.lastKind = "item";
}


/**
 * Add a separator to the menu.
 */
dMenu.prototype.addSeparator = function()
{
    if(this.lastKind && (this.lastKind == "item")) {
        this.lastInserted += '<tr height="2"><td colspan="3"></td></tr>';
        this.lastInserted += '<tr height="1" bgcolor="gray"><td colspan="3"></td></tr>';
        this.lastInserted += '<tr height="2"><td colspan="3"></td></tr>';
        this.lastKind = "separator";
    }
}


/**
 * Add a separator to the menu.
 */
dMenu.prototype.addLabel = function(label)
{
    this.addSeparator();
    this.lastInserted += '<tr><td colspan="3">'
        + label
        + '</td></tr>';
    this.lastKind = "label";
}


/**
 * Shows the menu.
 *
 * @param e the event that causes the menu to show
 */
dMenu.prototype.show = function(e)
{
    if (!dMenu.ie4 && !dMenu.ns6) {
        return;
    }

    dMenu.clearHide();

    if(dMenu.ie4) {
        return this.show_ie4();
    }
    
    if(dMenu.ns6) {
    	return this.show_ns6(e);
    }
}

//---------------------------------------------------
// PRIVATE METHODS
//---------------------------------------------------

/**
 * Shows the menu on IE.
 */
dMenu.prototype.show_ie4 = function()
{
	return this.show_ie4_ns6(event.srcElement);
}

/**
 * Shows the menu on NS6.
 */
dMenu.prototype.show_ns6 = function(event)
{
	var j = 7;
	return this.show_ie4_ns6(event.target);
}

/**
 * Shows the menu on IE and NS6.
 */
dMenu.prototype.show_ie4_ns6 = function(element)
{
    dMenu.sourceHighlight(element);
	
    dMenu.menuobj = document.getElementById("popmenu");
    dMenu.menuobj.thestyle = dMenu.menuobj.style;
    dMenu.menuobj.thestyle.width = this.width;
    dMenu.menuobj.innerHTML = dMenu.startHtml + this.linkset + dMenu.endHtml;
    dMenu.menuobj.thestyle.width = dMenu.menuobj.firstChild.offsetWidth;

    var width = dMenu.menuobj.offsetWidth;
    var height = dMenu.menuobj.offsetHeight;
    var scrollx = document.body.scrollLeft;
    var scrolly = document.body.scrollTop;
    var windoww = document.body.clientWidth;
    var windowh = document.body.clientHeight;
    var elementx = this.xCoord(element);
    var elementy = this.yCoord(element);
    var elementw = element.offsetWidth;
    var elementh = element.offsetHeight;

    var x = elementx + (elementw * this.px) / 100;
    var y = elementy + (elementh * this.py) / 100;

    var rightedge = scrollx + windoww;
    var bottomedge = scrolly + windowh;

    //if the horizontal distance isn't enough to accomodate the width of the context menu
    //
    if(rightedge < x + width) {
        x = elementx + (elementw * 25) / 100 - width;
    }
    if(x < scrollx) {
        x = scrollx;
    }
    dMenu.menuobj.thestyle.left = x;

    //same concept with the vertical position
    //
    if(bottomedge < y + height) {
        y = elementy + (elementh * 25) / 100 - height;
    }
    if(y < scrolly) {
        y = scrolly;
    }
    dMenu.menuobj.thestyle.top = y;

    // make the menu visible
    //
    dMenu.menuobj.thestyle.visibility = "visible";
    return false;
}

dMenu.prototype.xCoord = function(comp)
{
    var x = comp.offsetLeft;

    while((comp = comp.offsetParent) != null) {
        x += comp.offsetLeft;
    }
    return x;
}


dMenu.prototype.yCoord = function(comp)
{
    var y = comp.offsetTop;

    while((comp = comp.offsetParent) != null) {
        y += comp.offsetTop;
    }
    return y;
}

//---------------------------------------------------
// PUBLIC STATIC METHODS
//---------------------------------------------------

/**
 * Hides immediately the current open menu.
 */
dMenu.hide_ie4 = function()
{
	dMenu.hideIfClickOut(event.srcElement);
}

/**
 * Hides immediately the current open menu.
 */
dMenu.hide_ns6 = function(event)
{
	dMenu.hideIfClickOut(event.target);
}

dMenu.hideIfClickOut = function(element)
{
    if(element == dMenu.sourceElement) {
        return;
    }
    while(element != null) {
        if(element == dMenu.menuobj) {
            return;
        }
        element = element.parentElement;
    }
    dMenu.hide();
}

/**
 * Hides immediately the current open menu.
 */
dMenu.hide = function()
{
    dMenu.sourceHighlight();

    if (window.dMenu.menuobj) {
        dMenu.menuobj.thestyle.visibility = (dMenu.ie4 || dMenu.ns6) ? "hidden" : "hide";
    }
}

/**
 * Hides the current open menu. Wait a given delay before hide the menu.
 *
 * @param delay delay time in milliseconds to wait before hide the menu.
 *      Optional. Default = 500.
 */
dMenu.delayHide = function(delay)
{
    if(!delay) {
        delay = 500;
    }

    if (dMenu.ie4 || dMenu.ns6) {
        delayhide = setTimeout("dMenu.hide()", delay);
    }
}

/**
 * Initialize the document
 */
dMenu.init = function()
{
    document.write('<div id="popmenu" class="menuskin"');
    document.write(' onMouseover="dMenu.clearHide();dMenu.highlight(event,\'on\')"');
    document.write(' onMouseout="dMenu.highlight(event,\'off\');dMenu.dynamicHide(event)">');
    document.write('</div>');
}

//---------------------------------------------------
// IMPLEMENTATION STATIC METHODS
//---------------------------------------------------

/**
 * Shows/Hides the source element.
 * Spegne l'elemento precedente e se source != null lo accende.
 */
dMenu.sourceHighlight = function(source)
{
    if(dMenu.sourceElement) {
        dMenu.sourceElement.id = "";
        dMenu.sourceElement = null;
    }
    if(source) {
        dMenu.sourceElement = source;
        dMenu.sourceElement.id = "mouseoverstyle";
    }
}

/**
 * Determines if the element b is contained in the element a
 */
dMenu.contains_ns6 = function(a, b)
{
    while (b.parentNode) {
        if ((b = b.parentNode) == a) {
            return true;
        }
    }
    return false;
}

dMenu.dynamicHide = function(e)
{
    if(dMenu.ie4 && !dMenu.menuobj.contains(e.toElement)) {
        dMenu.hide();
    }
    else if(dMenu.ns6 && (e.currentTarget != e.relatedTarget)
            && !dMenu.contains_ns6(e.currentTarget, e.relatedTarget)) {
        dMenu.hide();
    }
}

/**
 * Abort the effect of dMenu.delayHide().
 */
dMenu.clearHide = function()
{
    if (window.delayhide) {
        clearTimeout(delayhide)
    }
}

/**
 * Highlights a menu item
 */
dMenu.highlight = function(e, state)
{
    if(document.all) {
        source_el = event.srcElement;
    }
    else if(document.getElementById) {
        source_el = e.target;
    }
    if (source_el.className == "menuitems") {
        source_el.id = (state == "on")? "mouseoverstyle" : "";
    }
    else {
        while(source_el.id != "popmenu"){
            source_el = document.getElementById ? source_el.parentNode : source_el.parentElement;
            if (source_el.className == "menuitems") {
                source_el.id = (state == "on") ? "mouseoverstyle" : "";
            }
        }
    }
}

//---------------------------------------------------
// A LITTLE INITIALIZATION
//---------------------------------------------------

// Forces the menu hiding on the mouse click
//
if (dMenu.ie4) {
    document.onclick = dMenu.hide_ie4;
}
if (dMenu.ns6) {
    document.onclick = dMenu.hide_ns6;
}
