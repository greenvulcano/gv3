var buttonPressed;

function buttonPress(num) {
	this.buttonPressed = num;
}

function checkForm(form) {
	if(buttonPressed==0){
		a = confirm('Restoring the initial values, any changes made will be lost. Continue?');
		return a;
	}
	if(buttonPressed==1){
		a = confirm('Save the changes?\nThe previous global parameters configuration will be available in the XMLConfig.properties.old file.');
		return a;
	}
	return true;
}

function toggleCrypted(id, root){
	var actual = '';
	var htmlContent = '';
	var onclick = ' onclick="toggleCrypted('+id+', \''+root+'\')"';
	if($('#cptd_'+id).val()=="false"){
		current = 'false';
		actual = 'true';
		htmlContent = '<img id="img_'+id+'" title="Encrypted. Click to decrypt." class="crptico" alt="Encrypted" src="'+root+'/images/properties/encrypted.png"'+onclick+'/>';
		$('#img_'+id).replaceWith(htmlContent);
		$('#cptd_'+id).val(actual);
	}
	else{
		current = 'true';
		actual = 'false';
		htmlContent = '<img id="img_'+id+'" title="Decrypted. Click to encrypt." class="crptico" alt="Decrypted" src="'+root+'/images/properties/decrypted.png"'+onclick+'/>';
		$('#img_'+id).replaceWith(htmlContent);
		$('#cptd_'+id).val(actual);
	}
}

//Disable all writable components if in "view" mode
$( document ).ready(function() {
	$("input[type=submit].view").attr("disabled", "disabled");
	$("input[type=text].view").attr("readonly","readonly");
});
