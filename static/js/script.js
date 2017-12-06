function updateTable() {
    var xhttp = new XMLHttpRequest();
    var key = document.getElementById("myInput").value;
    var e = document.getElementById("citySelect");
    var city = e.options[e.selectedIndex].text;
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById("hotelsTable").innerHTML = this.responseText;
        }
    };
    xhttp.open("GET", "searchtable?city=" + city +"&key="+key, true);
    xhttp.send();

}

