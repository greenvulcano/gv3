// Apply the grey theme
Highcharts.setOptions({
	colors: ["#DDDF0D","#7798BF", "#55BF3B", "#DF5353", "#aaeeee","#ff0066","#eeaaee","#55BF3B", "#DF5353", "#7798BF", "#aaeeee"],
	chart: {
   			backgroundColor: {linearGradient: [0, 0, 0, 400],stops: [[0, 'rgb(96, 96, 96)'],[1, 'rgb(16, 16, 16)']]},
      		borderWidth: 0,
      		borderRadius: 15,
      		plotBackgroundColor: null,
      		plotShadow: false,
      		plotBorderWidth: 0
	},
	title: {style: {color: '#FFF',font: '16px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'}},
	subtitle: {style: {color: '#DDD',font: '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'}},
	xAxis: {gridLineWidth: 0,lineColor: '#999',tickColor: '#999',labels: {style: {color: '#999',fontWeight: 'bold'}},
			title: {style: {color: '#AAA',font:'bold 12px Lucida Grande,Lucida Sans Unicode, Verdana,Arial,Helvetica,sans-serif'}}},
	yAxis: {
      		alternateGridColor: null,
      		minorTickInterval: null,
      		gridLineColor: 'rgba(255, 255, 255, .1)',
      		lineWidth: 0,
      		tickWidth: 0,
      		labels: {style: {color: '#999',fontWeight: 'bold'}},
      		title: {style:{color:'#AAA',font:'bold 12px Lucida Grande,Lucida Sans Unicode,Verdana, Arial, Helvetica, sans-serif'}}
	},
	legend: {itemStyle: {color: '#CCC'},itemHoverStyle: {color: '#FFF'},itemHiddenStyle: {color: '#333'}},
	credits: {style: {right: '50px'}},
	labels: {style: {color: '#CCC'}},
	tooltip:{backgroundColor:{linearGradient:[0,0,0,50],stops:[[0,'rgba(96,96,96,.8)'],[1,'rgba(16,16,16,.8)']]},borderWidth:0,style:{color:'#FFF'}},
	plotOptions: {line: {dataLabels: {color: '#CCC'},marker: {lineColor: '#333'}},spline: {marker: {lineColor: '#333'}},scatter: {marker: {lineColor: '#333'}}},
	toolbar: {itemStyle: {color: '#CCC'}}
});
	
$(document).ready(function() {
	
	var alfab = ['a','b','c','d','e'];
	var overview='Overview',memory='Memory',threads='Threads',classes='Classes',vmSummary = 'VM Summary';
	var labelInnerTabs = [overview/*,memory,threads,classes,vmSummary*/];
	var numInterval = 0;
	var labelTabs = new Array(),
		intervals = new Array();
	
	var serverName = "";
	
	var initAction		= "loadTabs.action",
	 	memoryAction 	= "memory.action",
	 	threadAction 	= "thread.action",
	 	classAction		= "class.action",
	 	cpuAction 		= "cpu.action";
	
	var memoryChart = null, //highchart per la memory usata
		threadChart = null, //highchart per i thread usati
		classChart  = null, //highchart le classi usate
		cpuChart 	= null; //highchart per la cpu usata
	
	function getHighcharts(container,funcLoad,title,seriesOpt,yText,funcTip){
		return {
			chart: {renderTo: container, /*margin: [50, 130, 60, 80],*/events: {load: funcLoad}},
			credits: {enabled: true,href: "http://www.greenvulcano.com",target: "_self",text: "www.greenvulcano.com"},
			title: {text: title/*, style: {margin: '10px 80px 0 0'}*/},
			xAxis: {type: 'datetime'},
			yAxis: {title: {text: yText}, plotLines: [{value: 0, width: 1, color: '#808080'}]},
			tooltip: {formatter: funcTip},
			legend: {layout: 'vertical',style: {left: '100px',bottom: 'auto',right: 'auto',top: '30px'}},
			plotOptions: {line: {dataLabels: {enabled: true}}},
			series: seriesOpt
		};
	}
	
	function loadInfoData(){
		var tabs = $('#tabs');
		tabs.tabs();
		var contentTabs = $('#content-tabs');
		$.ajax({type: "GET", url: initAction, dataType: "json", success: function(msg) {
			for ( var i = 0; i < msg.length; i++) {
				labelTabs.push(msg[i].name);
				contentTabs.append("<div id='"+msg[i].name+"'></div>");
				var contentInnerTabs = $('#'+msg[i].name);
				contentInnerTabs.append("<div id='"+msg[i].name+i+"'><ul></ul></div>");
				var innerTabs = $('#'+msg[i].name+i);
				innerTabs.tabs();
				for ( var n = 0; n < labelInnerTabs.length; n++) {
					contentTabs.append("<div id='"+msg[i].name+i+alfab[n]+"'></div>");
					innerTabs.tabs("add",'#'+msg[i].name+i+alfab[n],labelInnerTabs[n]);
					loadDivsCharts(labelInnerTabs[n], msg[i].name, msg[i].name+i+alfab[n]);
				}				
				tabs.tabs("add",'#'+msg[i].name,msg[i].name);
				contentInnerTabs.tabs({select: function(event, ui) {loadCharts(labelInnerTabs[ui.index], serverName);}});
				if(i==0){loadCharts(overview, msg[i].name);}
			}
			serverName = labelTabs[0];
			}
		});
		tabs.tabs({
			select:function(event,ui){serverName=labelTabs[ui.index];$("#"+serverName).tabs('select',0);loadCharts(overview,serverName);}
		});
	}
	loadInfoData();
	function loadCharts(tabName, serverName){
		resetInterval();
		var div = serverName+tabName;
		switch (tabName) {
		case overview:loadMemory(div+memory);loadThreads(div+threads);loadClasses(div+classes);loadCpu(div+'cpu');break;
		//case memory:;break;
		default:break;
		}
	}
	
	function loadDivsCharts(tabName, serverName, contentDiv){
		var content = $('#'+contentDiv);
		var divs = "";
		switch (tabName) {
		case overview:
			content.css('height','560px');
			divs ="<div id='"+serverName+tabName+memory+"' class='overviewChart left'></div>" +
				  "<div id='"+serverName+tabName+threads+"' class='overviewChart right'></div>" +
				  "<div id='memoryInfo' class='ui-widget-header ui-corner-all overviewInfoChart left'></div>" +
				  "<div id='threadsInfo' class='ui-widget-header ui-corner-all overviewInfoChart right'></div>" +
				  "<div id='"+serverName+tabName+classes+"' class='overviewChart left'></div>" +
				  "<div id='"+serverName+tabName+"cpu' class='overviewChart right'></div>" +
				  "<div id='classInfo' class='ui-widget-header ui-corner-all overviewInfoChart left'></div>" +
				  "<div id='cpuInfo' class='ui-widget-header ui-corner-all overviewInfoChart right'></div>";
			content.append(divs);
			break;
		case memory:
			break;
		default:break;
		}
	}
	
	function loadCpu(id){
		if(cpuChart!=null){cpuChart.destroy();}
		var sOpt = [{type: 'area', name:'CPU Usage',data: getInitArray()}];
		cpuChart = new Highcharts.Chart(getHighcharts(id,reqCpuData,'CPU Usage',sOpt,'%',cpuTip));
	}
	function loadCpuData(){
		$.ajax({type: "GET",url: cpuAction,data:"server="+serverName,dataType:"json",success: function(msg){
				var cpu=cpuChart.series[0];
				var x=(new Date()).getTime(),y=msg[0].cpuUsage;
				cpu.addPoint([x,y],true,true);
				reloadCpuInfo(msg[0].cpuUsage);
			}
		});
	}
	function reqCpuData(){loadCpuData();numInterval = setInterval(loadCpuData, 5000);intervals.push(numInterval);}
	function reloadCpuInfo(usage){
		$('#cpuInfo').html('<table cellpadding="2" cellspacing="2"><tbody><tr><td>CPU Usage: '+usage+'%</td></tr></tbody></table>');
	}
	function cpuTip(){return '<b>'+this.series.name+'</b><br/>'+Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x)+'<br/>'+this.y+'%';}
	
	function loadClasses(id){
		if(classChart!=null){classChart.destroy();}
		var sOpt = [{type: 'area', name:'Classes loaded',data: getInitArray()}];
		classChart = new Highcharts.Chart(getHighcharts(id,reqClassData,'Classes',sOpt,'',classTip));
	}
	function loadClassData(){
		$.ajax({type: "GET",url: classAction,data:"server="+serverName,dataType:"json",success: function(msg){
				var clazz=classChart.series[0];
				var loaded=msg[0].classesLoad, unloaded=msg[0].classesUnload, total=msg[0].classesTotal;
				var x=(new Date()).getTime(),y=loaded;
				clazz.addPoint([x,y],true,true);
				reloadClassInfo(loaded,unloaded,total);
			}
		});
	}
	function reqClassData(){loadClassData();numInterval = setInterval(loadClassData, 5000);intervals.push(numInterval);}
	function reloadClassInfo(loaded,unloaded,total){
		$('#classInfo').html('<table cellpadding="2" cellspacing="2"><tbody><tr><td>Loaded: '+loaded+'</td>'+
			'<td>Unloaded: '+unloaded+'</td><td>Total: '+total+'</td></tr></tbody></table>');
	}
	function classTip(){return '<b>'+this.series.name+'</b><br/>'+Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x)+'<br/>'+this.y;}
	
	function loadMemory(id){
		if(memoryChart!=null){memoryChart.destroy();}
		var sOpt = [{type: 'area', name:'Heap Memory Usage',data: getInitArray()}];
		memoryChart = new Highcharts.Chart(getHighcharts(id,reqMemoryData,'Heap Memory Usage',sOpt,'Mb',memoryTip));
	}
	function loadMemoryData(){
		$.ajax({type: "GET",url: memoryAction,data:"server="+serverName,dataType:"json",success: function(msg){
				var heap=memoryChart.series[0];
				var used=msg[0].heapMemoryUsed/1048576, committed=msg[0].heapMemoryCommitted/1048576, max=msg[0].heapMemoryMax/1048576;
				var x=(new Date()).getTime(),y=used;
				heap.addPoint([x,y],true,true);
				reloadMemoryInfo(used,committed,max);
			}
		});
	}
	function reqMemoryData(){loadMemoryData();numInterval = setInterval(loadMemoryData, 5000);intervals.push(numInterval);}
	function reloadMemoryInfo(used,committed,max){
		$('#memoryInfo').html('<table cellpadding="2" cellspacing="2"><tbody><tr><td>Used: '+used.toFixed(3)+'Mb</td>'+
			'<td>Committed: '+committed+'Mb</td><td>Max: '+max.toFixed(3)+'Mb</td></tr></tbody></table>');
	}
	function memoryTip(){return '<b>'+this.series.name+'</b><br/>'+Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x)+'<br/>'+Highcharts.numberFormat(this.y,2);}
	
	function loadThreads(id){
		if(threadChart!=null){threadChart.destroy();}
		var sOpt = [{type: 'area', name:'Live threads',data: getInitArray()}];
		threadChart = new Highcharts.Chart(getHighcharts(id,reqThreadsData,'Threads',sOpt,'',threadsTip));
	}
	function loadThreadsData(){
		$.ajax({type: "GET",url: threadAction,data:"server="+serverName,dataType:"json",success: function(msg){
				var threads=threadChart.series[0];
				var x=(new Date()).getTime(),y=msg[0].liveThread;
				threads.addPoint([x,y],true,true);
				reloadThreadsInfo(msg[0].liveThread,msg[0].peakThread,msg[0].totalThread);
			}
		});
	}
	function reqThreadsData(){loadThreadsData();numInterval = setInterval(loadThreadsData, 5000);intervals.push(numInterval);}
	function reloadThreadsInfo(live,peak,total){
		$('#threadsInfo').html('<table cellpadding="2" cellspacing="2"><tbody><tr><td>Live: '+live+'</td>'+
			'<td>Peak: '+peak+'</td><td>Total: '+total+'</td></tr></tbody></table>');
	}
	function threadsTip(){return '<b>'+this.series.name+'</b><br/>'+Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x)+'<br/>'+this.y;}
	
	function getInitArray(){var data=[],t=(new Date()).getTime(),i;for(i=-25;i<=0;i++){data.push({x:t+i*1000,y:0});}return data;}
	function resetInterval(){for(var i=0; i<intervals.length;i++){clearInterval(intervals[i]);}intervals = new Array();}
});