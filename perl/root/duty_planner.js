
function select(el){
        if(el.className != "selected"){
            el.className = "selected";
        }
        else{
            el.className = "";
        }
}
function remove_items(pagename){
    var list = document.getElementsByClassName("selected");
    var url = "http://127.0.0.1:3000/"+pagename+"?delete=";
    for(var i = 0; i < list.length;i++){

        url += i > 0? ","+list[i].id : list[i].id;
    }
    httpGetAsync(url);
}

function httpGetAsync(theUrl)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, true); // true
    xmlHttp.send(null);
}


