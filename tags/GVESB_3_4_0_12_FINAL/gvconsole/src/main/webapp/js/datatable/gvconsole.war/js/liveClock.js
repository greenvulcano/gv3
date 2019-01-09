var LC_Style=[

	// Qui sotto puoi impostare il carattere da usare
	"Arial",

	// Qui sotto puoi impostare la dimensione del font in grandezza relativa (valori da 1 a 7)
	"1",

	// Qui sotto puoi impostare il colore del carattere in formato esadecimale
	// preceduto dal simbolo della celletta #
	"#000000",

	// Qui sotto puoi impostare il colore di sfondo in formato esadecimale
	// preceduto dal simbolo della celletta #
	"#FFFFFF",

	// Qui sotto puoi indicare la frase breve che preceder? l'orologio
	" ",

	// Qui sotto puoi indicare la frase breve che seguir? l'orologio
	" ",

	// Qui sotto puoi impostare la larghezza del campo dell'orologio in pixels
	300,

	// Qui sotto puoi decidere il formato dell'ora. 24 ore (0), AM/PM (1)
	1,

	// Qui sotto puoi decidere dopo quale intervallo di tempo l'orologio dovr? aggiornarsi:
	// 0 mai, 1 ogni secondo, 2 ogni minuto
	1,

	// Qui sotto puoi decidere il formato della data:
	// 0 niente datario, 1 gg/mm/aa, 2 mm/gg/aa, 3 GGGG MMMM
	// 4 GGGG MMMM AAAA
	3,

	// Qui sotto puoi decidere se i nomi dei giorni e dei mesi debbano essere abbreviati:
	// 1 si, 0 no
	0,

	// Qui sotto puoi impostare come debba apparire l'ora
	// rispetto al meridiano di Greenwich.
	// Per far s? che venga visualizzata l'ora di chi guarda il sito
	// imposta il valore null
	null
	// Per impostare l'ora di un meridiano diverso da quello di
	// Greenwich imposta un valore numerico che coincida con le
	// ore di differenza. Per il meridiano di Roma, ad esempio
	// imposta il valore numerico 1.
];


var LC_IE=(document.all);
var LC_NS=(document.layers);
var LC_N6=(window.sidebar);
var LC_Old=(!LC_IE && !LC_NS && !LC_N6);

var LC_Clocks=new Array();

var LC_DaysOfWeek=[
	["Domenica","Dom"],
	["Lunedi","Lun"],
	["Martedi","Mar"],
	["Mercoledi","Mer"],
	["Giovedi","Gio"],
	["Venerdi","Ven"],
	["Sabato","Sab"]
];

var LC_MonthsOfYear=[
	["Gennaio","Gen"],
	["Febbraio","Feb"],
	["Marzo","Mar"],
	["Aprile","Apr"],
	["Maggio","Mag"],
	["Giugno","Giu"],
	["Luglio","Lug"],
	["Agosto","Ago"],
	["Settembre","Set"],
	["Ottobre","Ott"],
	["Novembre","Nov"],
	["Dicembre","Dic"]
];

var LC_ClockUpdate=[0,1000,60000];

///////////////////////////////////////////////////////////

function LC_CreateClock(c) {
	if(LC_IE||LC_N6){clockTags='<span id="'+c.Name+'" style="width:'+c.Width+'px;background-color:'+c.BackColor+'"></span>'}
	else if(LC_NS){clockTags='<ilayer width="'+c.Width+'" bgColor="'+c.BackColor+'" id="'+c.Name+'Pos"><layer id="'+c.Name+'"></layer></ilayer>'}

	if(!LC_Old){document.write(clockTags)}
	else{LC_UpdateClock(LC_Clocks.length-1)}
}

function LC_InitializeClocks(){
	LC_OtherOnloads();
	if(LC_Old){return}
	for(i=0;i<LC_Clocks.length;i++){
		LC_UpdateClock(i);
		if (LC_Clocks[i].Update) {
			eval('var '+LC_Clocks[i].Name+'=setInterval("LC_UpdateClock("+'+i+'+")",'+LC_ClockUpdate[LC_Clocks[i].Update]+')');
		}
	}
}

function LC_UpdateClock(Clock){
	var c=LC_Clocks[Clock];

	var t=new Date();
	if(!isNaN(c.GMT)){
	var offset=t.getTimezoneOffset();
	if(navigator.appVersion.indexOf('MSIE 3') != -1){offset=offset*(-1)}
		t.setTime(t.getTime()+offset*60000);
		t.setTime(t.getTime()+c.GMT*3600000);
	}
	var day=t.getDay();
	var md=t.getDate();
	var mnth=t.getMonth();
	var hrs=t.getHours();
	var mins=t.getMinutes();
	var secs=t.getSeconds();
	var yr=t.getYear();

	if(yr<1900){yr+=1900}

	if(c.DisplayDate>=3){
		md+="";
		abbrev=" ";
		if(md.charAt(md.length-2)!=1){
			var tmp=md.charAt(md.length-1);
			if(tmp==1){abbrev=" "}
			else if(tmp==2){abbrev=" "}
			else if(tmp==3){abbrev=" "}
		}
		md+=abbrev;
	}

	var ampm="";
	if(c.Hour12==1){
		ampm="AM";
		if(hrs>=12){ampm="PM"; hrs-=12}
		if(hrs==0){hrs=12}
	}
	if(mins<=9){mins="0"+mins}
	if(secs<=9){secs="0"+secs}

	var html = '<font color="'+c.FntColor+'" face="'+c.FntFace+'" size="'+c.FntSize+'">';
	html+=c.OpenTags;
	html+=hrs+':'+mins;
	if(c.Update==1){html+=':'+secs}
	if(c.Hour12){html+=' '+ampm}
	if(c.DisplayDate==1){html=' '+md+'/'+(mnth+1)+'/'+yr+'  '+html}
	if(c.DisplayDate==2){html=' '+(mnth+1)+'/'+md+'/'+yr+'  '+html}
	if(c.DisplayDate>=3){html='  '+LC_DaysOfWeek[day][c.Abbreviate]+' '+md+' '+LC_MonthsOfYear[mnth][c.Abbreviate]+' ' +yr+''+html}
	if(c.DisplayDate>=4){html+=' '}
	html+=c.CloseTags;
	html+='</font>';

	if(LC_NS){
		var l=document.layers[c.Name+"Pos"].document.layers[c.Name].document;
		l.open();
		l.write(html);
		l.close();
	}else if(LC_N6||LC_IE){
		document.getElementById(c.Name).innerHTML=html;
	}else{
		document.write(html);
	}
}

function LiveClock(a,b,c,d,e,f,g,h,i,j,k,l){
	this.Name='LiveClock'+LC_Clocks.length;
	this.FntFace=a||LC_Style[0];
	this.FntSize=b||LC_Style[1];
	this.FntColor=c||LC_Style[2];
	this.BackColor=d||LC_Style[3];
	this.OpenTags=e||LC_Style[4];
	this.CloseTags=f||LC_Style[5];
	this.Width=g||LC_Style[6];
	this.Hour12=h||LC_Style[7];
	this.Update=i||LC_Style[8];
	this.Abbreviate=j||LC_Style[10];
	this.DisplayDate=k||LC_Style[9];
	this.GMT=l||LC_Style[11];
	LC_Clocks[LC_Clocks.length]=this;
	LC_CreateClock(this);
}

///////////////////////////////////////////////////////////

LC_OtherOnloads=(window.onload)?window.onload:new Function;
window.onload=LC_InitializeClocks;