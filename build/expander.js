// JavaScript Document
 function expander(ev){
        if (!ev) var ev = window.event; // for ie
        try {
		    var par;
		  	if (ev.target) par = ev.target;
	        else if (ev.srcElement) par = ev.srcElement;
	        if (par.nodeType == 3) // defeat Safari bug
		    par = par.parentNode;
          if ((par.nodeName == 'TD' || par.nodeName == 'td' ) && par.className == 'expander') {
            if (par.parentNode.className == 'expander-closed') {
	          par.parentNode.className = '';
              par.firstChild.data = '\u2212';
            }
            else 
			{
              par.parentNode.className = 'expander-closed';
              par.firstChild.data = '+';
            }
			
          }
        } catch (e) {
		alert(e);
        }
	}
