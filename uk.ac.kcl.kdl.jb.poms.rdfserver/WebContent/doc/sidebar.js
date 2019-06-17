function jumpTo(idName){
   var ele = document.getElementById(idName);
   if(ele == null)return;
   var scrollPos = ele.offsetTop;
   if(scrollTo < 180){
      window.scrollTo(0,0)
	  return;
   }
   window.scrollTo(0, scrollPos-180);
}

//   <a class="w3-bar-item w3-button w3-hover-black" href="javascript:jumpTo('about')">About DPRR</a>

function buildSidebar(){
   var cont = document.getElementById("sidebar-contents");
   var builtHTML = "";
   var topOne = document.getElementById("top");
   if(topOne != null){
      builtHTML = "<a class=\"w3-bar-item w3-button w3-hover-black\" href=\"javascript:jumpTo('top')\">Top</a>"
   }
   var h3s = document.getElementsByTagName("h3");
   for (var i = 0; i < h3s.length; i++) {
      var theItem = h3s[i];
      if(theItem.hasAttribute("id")){
         var theID = theItem.id;
		 var theContents = theItem.innerHTML;
		 builtHTML += "<a class=\"w3-bar-item w3-button w3-hover-black\" href=\"javascript:jumpTo('"+theID+"')\">"+theContents+"</a>"
      }
   }
   cont.innerHTML = builtHTML;
}

