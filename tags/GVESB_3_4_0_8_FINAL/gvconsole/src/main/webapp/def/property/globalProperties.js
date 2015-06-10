var buttonPressed;

$(document).ready(function() {
	//Reports any errors after the last attempt to save
	var warn = $('#warning').val();
	if (warn!="") {
		jAlert(warn, 'Save error');
	}

	//Disable all writable components if in "view" mode
	$("input[type=submit].view").attr("disabled", "disabled");
	$("input[type=text].view").attr("readonly","readonly");

	//Intercept the last button clicked
	$('#gpForm').click(function(e) {
		$(this).data('clicked',$(e.target));
	});
	//Confirmation question before every submit
	$('#gpForm').submit(function(e){
		var clicked = $(this).data('clicked');
		e.preventDefault();
		if (clicked.is('[id=revert]')){
			jConfirm('Restoring the initial values, any changes made will be lost. Continue?', 'Revert confirmation', function(result){
				if(result)  $('#revert').click();
			});
		}
		if (clicked.is('[id=save]')){
			jConfirm('Save the changes?\nThe previous global parameters configuration will be available in the <strong>XMLConfig.properties.old</strong> file.', 'Save confirmation', function(result){
				if(result)  $('#save').click();
			});
		}
	});
});


function toggleCrypted(id, root){
	var actual = '';
	var htmlContent = '';
	var onclick = ' onclick="toggleCrypted('+id+', \''+root+'\')"';
	if($('#cptd_'+id).val()=="false"){
		current = 'false';
		actual = 'true';
		htmlContent = '<img id="img_'+id+'" title="Encrypted. Click to decrypt." class="clckblico" alt="Encrypted" src="'+root+'/images/properties/encrypted.png"'+onclick+'/>';
		$('#img_'+id).replaceWith(htmlContent);
		$('#cptd_'+id).val(actual);
	}
	else{
		current = 'true';
		actual = 'false';
		htmlContent = '<img id="img_'+id+'" title="Decrypted. Click to encrypt." class="clckblico" alt="Decrypted" src="'+root+'/images/properties/decrypted.png"'+onclick+'/>';
		$('#img_'+id).replaceWith(htmlContent);
		$('#cptd_'+id).val(actual);
	}
}

function manageDescription(id, name, desc, mode, root){
	if (mode=='view'){
		jAlert(desc, 'Property '+name);
	}
	else{
		jPrompt('Insert new description', desc, 'Property '+name, function(newDesc){
			if(newDesc!=null){
				var htmlContent = '<img id="info_'+id+'" title="'+newDesc+'" class="clckblico" alt="Info" src="'+root+'/images/info.png" onClick="manageDescription('+id+', \''+name+'\', \''+newDesc+'\', \''+mode+'\', \''+root+'\')"/>';
				$('#info_'+id).replaceWith(htmlContent);
				$('#desc_'+id).val(newDesc);
			}
		});
	}
}
