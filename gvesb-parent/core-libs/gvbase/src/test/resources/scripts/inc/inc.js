var services = {"LIST_EXCEL":"1", "LIST_PDF":"1", "LIST_BIRT":"1"};
function findMatch(svc) {
	return ("1" == services[svc]);
}