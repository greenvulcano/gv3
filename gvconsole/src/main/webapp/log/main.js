
function getMessage(typeDialog,messageID) {
  $.ajax({ url: contextRoot+'/log/HandleLogAction.do?skipValidation=true&methodToCall=' + btnShowMsgL,
      cache: false,
      type: 'POST',
      data: ({
                id_msg:messageID,
                dialogType:typeDialog
            }),
      success: function(data) {
                    //alert(data);
                    $("#textMessage").html("<pre>" + data + "</pre>");
                },
      error: function(request, textStatus) {
                alert('Errore nella richiesta: ' + textStatus);
              },
      dataType: 'text',
      async:   false
  });
}

function showMessage(typeDialog,messageID){
  getMessage(typeDialog,messageID);
  $("#dialog-message" ).dialog("open");
}

$(function() {
	$( "#dialog:ui-dialog" ).dialog( "destroy" );
	$( "#dialog-message" ).dialog({
		modal: true,
		autoOpen: false,
		width: 800,
		height:400,
		buttons: {
			Ok: function() {
				$( this ).dialog( "close" );
			}
		}
	});
});

$(document).ready(function() {
		oTable = $('#tableResult').dataTable({
			"bJQueryUI": true,
            "iDisplayLength": 100,
			"sPaginationType": "two_button", //"full_numbers"
            "bSort": false
		});
	} );