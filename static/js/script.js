function myFunction() {
    // Declare variables
    var input, filter, table, tr, name, i;
    input = document.getElementById("myInput");
    filter = input.value.toUpperCase();
    table = document.getElementById("myTable");
    tr = table.getElementsByTagName("tr");

    // Loop through all table rows, and hide those who don't match the search query
    for (i = 0; i < tr.length; i++) {
        name = tr[i].getElementsByTagName("td")[0];
        if (name) {
            if (name.innerHTML.toUpperCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        }
    }
}

function searchHotel() {
    // Declare variables
    var input,select, nameFilter, cityFilter, table, tr, name, city, i;

    input = document.getElementById("myInput").value;
    console.debug(input);
    select = document.getElementById("citySelect").value;
    nameFilter = input.value.toUpperCase();
    cityFilter = select.value.toUpperCase();

    table = document.getElementById("myTable");
    tr = table.getElementsByTagName("tr");

    // Loop through all table rows, and hide those who don't match the search query
    for (i = 0; i < tr.length; i++) {
        name = tr[i].getElementsByTagName("td")[0];
        city = tr[i].getElementsByTagName("td")[1];

        if (name) {
            if (name.innerHTML.toUpperCase().indexOf(nameFilter) > -1 ) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        }
    }
}