function popup(mylink, windowname, wwidth, wheight)
{
	if (! window.focus)
		return true;
	
	var href;

	if (typeof(mylink) == 'string')
	   href=mylink;
	else
	   href=mylink.href;

	window.open(href, windowname, 'width='+ wwidth+",height="+wheight+",scrollbars=yes");
	return false;
}