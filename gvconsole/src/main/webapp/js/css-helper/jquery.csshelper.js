/* Il costrutto '(function($) { ... })(jQuery);' implementa una tecnica javascript per eseguire codice privato (non accessibile da altri script)
 * utilizzando anche funzioni private. La tecnica è la seguente:
 * 1. Tutto il codice è incapsulato, e quindi reso privato, all'interno di una funzione.
 * 2. La funzione incapsulante è anonima, per non essere accessibile essa stessa da codice esterno.
 * 3. La funzione incapsulante è invocata subito, e per far ciò vanno usate le parentesi tonde attorno ad essa visto che è anonima e quindi non può 
 *   essere invocata per nome.
 * 4. Alla funzione incaspulante viene passato come parametro l'oggetto jQuery per poterlo utilizzare con un alias privato (in questo caso '$').
 */
(function($) {

	function removeCSS_Std(css) {
		css = css.replace(/([A-Z]+)/g, "-$1").toLowerCase();
		var style = $(this).attr('style');
		var idx = style.indexOf(css);
		if (idx > -1) {
			var idx2 = style.indexOf(';', idx);
			if (idx2 > -1 && idx2 < style.length) style = style.substring(0, idx) + style.substring(idx2 + 1);
			else style = style.substring(0, idx);
		}
		$(this).attr('style', style);
	}

	function removeCSS_ie(css) {
		//$(this)[0].style[css] = '';
		$(this)[0].style.removeAttribute(css);
	}

	/** Aggiunge a jQuery la capacità di rimuovere una proprietà CSS dall'elemento corrente.
	 * jQuery permette solamente di leggere o modificare le proprietà CSS di un elemento. In tal modo, la proprietà CSS viene aggiunta direttamente
	 * all'elemento e jQuery non permette di rimuovere le proprietà in tal modo aggiunte, che hanno precedenza su altre definite tramite selettori CSS
	 * con minore priorità.
	 * Il nome della proprietà CSS deve camelCase in caso di nome di proprietà composto da più parole 
	 * (es.: 'marginTop', non 'margin-top').
	 */
	$.fn.removeCss = (window.getComputedStyle ? removeCSS_Std : removeCSS_ie);

    var darkingBackup = {};
    
	$.fn.darking = darking;
	
	/** Accetta un colore nei tre formati seguenti: '#fff', '#fafafa', 'rgb(250, 13, 7)'.
		 Restituisce un colore più scuro (a meno che il colore di partenza non sia già troppo scuro).
		 Se reset == true, ripristina i colori di partenza.
     
		 @param propName: Il nome camelCase di una proprietà css che contiene un colore.
		 @param reset: Se false il colore viene scurito, altrimenti viene schiarito (attenzione ai colori limite che non vengono scuriti!!).
		 */
	function darking(propName, reset) {
		var offset = 20;
		var el = $(this);
		
		var backupName = propName + 'CssHelperBackup';
		if (reset) {
			var backup = darkingBackup[backupName];
			if (backup) el.css(propName, darkingBackup[backupName]);
			darkingBackup[backupName] = null;
			return el;
		}
		
		var color = el.css(propName);
		
		if (color == 'transparent') {
			darkingBackup[backupName] = color;
			el.css(propName, 'rgb(235,235,235)');
			return el;
		}
		
		var colors;
		if (color.charAt(0) == '#') {
			colors = new Array();
			if (color.length == 4) {
				colors[0] = parseInt(color.charAt(1) + color.charAt(1), 16);
				colors[1] = parseInt(color.charAt(2) + color.charAt(2), 16);
				colors[2] = parseInt(color.charAt(3) + color.charAt(3), 16);
			} else {
				colors[0] = parseInt(color.substr(1, 2), 16);
				colors[1] = parseInt(color.substr(3, 2), 16);
				colors[2] = parseInt(color.substr(5, 2), 16);
			}
		} else if (color.charAt(3) == '(') {
			colors = color.substring(4, color.length - 1).split(',');
		} else {
			if (color == 'red') colors = [255,0,0];
			else return el;
		}
		
		for (var i = 0; i < 3; i++) {
			if (colors[i] > offset) colors[i] = colors[i] - offset;
		}
		
		darkingBackup[backupName] = color;
		el.css(propName, 'rgb(' + colors[0] + ',' + colors[1] + ',' + colors[2] + ')');
		return el;
	}
	
})(jQuery);
