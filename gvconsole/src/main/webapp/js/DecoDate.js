function DecoDate(a) {
	var d;
	if (!a) d = new Date();
	else if (!isNaN(a)) d = new Date(a);
	else if (a.weekStartDayIdx) d = new Date(a.getTime());
	else d = a;

	d.weekStartDayIdx = 1;

	d.setWeekStartDayIdx = function(dayIdx) { this.weekStartDayIdx = dayIdx; };

	d.findQuarterStartDay = DD_findQuarterStartDay;
	d.countDaysTo = DD_countDaysTo;
	d.getDaysInMonth = DD_getDaysInMonth;
	d.add = DD_add;
	d.sub = DD_sub;
	d.toStart = DD_toStart;
	d.toEnd = DD_toEnd;

	d.compareTo = DD_compareTo;
	d.lowerThan = DD_lowerThan;
	d.lowerOrEquals = DD_lowerOrEquals;
	d.greaterThan = DD_greaterThan;
	d.greaterOrEquals = DD_greaterOrEquals;
	d.equals = DD_equals;

	d.isLegalToSolar = DD_isLegalToSolar;
	d.isSolarToLegal = DD_isSolarToLegal;
    d.numHourInDay = DD_numHourInDay;

	d.format = DD_format;
	d.parse = DD_parse;
    d.resetTime = DD_resetTime;

	return d;
}

var DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
var HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
var MINUTE_IN_MILLISECONDS = 60 * 1000;

function DD_findQuarterStartDay() {
	var firstMonth = Math.floor(this.getMonth() / 3) * 3;
	var ret = new Date(this);
	ret.setMonth(firstMonth);
	ret.setDate(1);
	return new DecoDate(ret);
}

/*
	@to Una stringa che contiene una data in formato 'dd/MM/yyyy' o una Date.

	@return Il numero di giorni del periodo in argomento, estremi inclusi.
*/
function DD_countDaysTo(to) {
	var a;
	var b;
	var d = new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0, 0, 0);
	a = d.getTime();
	if (typeof(to) == 'string') {
		b = getDateFromFormat(to + ' 23:59:59', 'dd/MM/yyyy HH:mm:ss');
	} else {
		d = new Date(to.getFullYear(), to.getMonth(), to.getDate(), 23, 59, 59);
		b = d.getTime();
	}

	// Uso round perchè mi corregge automaticamente il problema del cambio di orario legale/solare.
	return Math.round((b - a + 1000) / DAY_IN_MILLISECONDS);
}

function DD_getDaysInMonth(m, y) {
	var dd = new Date(y, m, 1);
	m++;
	if (m == 12) {m = 0; dd.setFullYear(y + 1);}
	dd.setMonth(m);
	dd.setTime(dd.getTime() - DAY_IN_MILLISECONDS);
	return dd.getDate();
}

function DD_toStart(type) {
	if (type == 'WEEK') {
		// weekStartDay : 0 (domenica), ... , 6 (sabato)
		var wd = this.getDay() - this.weekStartDayIdx;
		if (wd < 0) wd = wd + 7;
		var ms = this.getTime() - wd * DAY_IN_MILLISECONDS;
		this.setTime(ms);
	} else if (type == 'MONTH') {
		this.setDate(1);
	} else if (type == 'YEAR') {
		this.setDate(1);
		this.setMonth(1);
	}

	return this; // Serve per poter chiamare i metodi in sequenza.
}

function DD_toEnd(type) {
	if (type == 'WEEK') {
		var wd = this.getDay() - this.weekStartDayIdx;
		if (wd < 0) wd = wd + 7;
		var ms = this.getTime() + (6 - wd) * DAY_IN_MILLISECONDS;
		this.setTime(ms);
	} else if (type == 'MONTH') {
		this.add(1, 'MONTH');
		this.sub(1, 'DAY');
	} else if (type == 'YEAR') {
		this.setMonth(11);
		this.setDate(31);
	}

	return this;
}

function DD_add(val, type) {
	if (type == 'DAY') {
		this.setTime(this.getTime() + DAY_IN_MILLISECONDS * val);
	} else if (type == 'MONTH') {
		var m = val + this.getMonth();
		var y = Math.floor(m / 12);
		var m = m % 12;
		this.setFullYear(this.getFullYear() + y);
		this.setMonth(m);
	} else if (type == 'YEAR') {
		this.setFullYear(this.getFullYear() + val);
	} else if (type == 'HOUR') {
		this.setTime(this.getTime() + HOUR_IN_MILLISECONDS * val);
	} else if (type == 'MINUTE') {
        this.setTime(this.getTime() + MINUTE_IN_MILLISECONDS * val);
    }

	return this;
}

function DD_sub(val, type) {
	return DD_add.call(this, -val, type);
}

function DD_format(format) {
	return formatDate(this, format);
}

function DD_parse(val, format) {
	return new DecoDate(new Date(getDateFromFormat(val, format)));
}

function DD_resetTime() {
    this.setHours(0);
    this.setMinutes(0);
    this.setSeconds(0);
    this.setMilliseconds(0);

    return this;
}

function DD_compareTo(d, format) {
	if (format) return this.getTime() - getDateFromFormat(d, format);
	return this.getTime() - d.getTime();
}

function DD_lowerThan(d) {
	return (this.getTime() - d.getTime() < 0);
}

function DD_lowerOrEquals(d) {
	return (this.getTime() - d.getTime() <= 0);
}

function DD_greaterThan(d) {
	return (this.getTime() - d.getTime() > 0);
}

function DD_greaterOrEquals(d) {
	return (this.getTime() - d.getTime() >= 0);
}

function DD_equals(d) {
	return (this.getTime() - d.getTime() == 0);
}

 /*
  * @return true se si tratta di una data di passaggio da ora legale a ora solare, false altrimenti.
  */
function DD_isLegalToSolar () {
    // sunday in october, less to 7 days to end of month
    return ( this.getMonth() == 9 &&
             this.getDay() == 0 &&
             31 - this.getDate() < 7 );
}

 /*
  * @return true se si tratta di una data di passaggio da ora solare a ora legale, false altrimenti.
  */
function DD_isSolarToLegal () {
    // sunday in march, less to 7 days to end of month
    return ( this.getMonth() == 2 &&
         this.getDay() == 0 &&
         31 - this.getDate() < 7 );
}

/* * * * * * * * * * * * * * * *
 * Aggiunta - Gianluca Di Maio *
 *  - numHourInDay             *
 * * * * * * * * * * * * * * * */

function DD_numHourInDay () {
    if (this.isSolarToLegal()) {
        return 23;
    }
    if (this.isLegalToSolar()) {
        return 25;
    }
    return 24;
}