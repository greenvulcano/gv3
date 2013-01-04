<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
    String contextRoot=request.getContextPath();

    session.invalidate();
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<body onload=window.location.href='<%=contextRoot%>'>
</body>

</html>