<!DOCTYPE html>
<html>
<head>
    <#include "./include/header.vm">
    <script>

 $(document).ready(function() {

   $('.containerts').each(function(i) {
     $(this).children(".tablesorter")
       .tablesorter({
         widthFixed: false,
         widgets: ['zebra']
       });
   });

   (function countdown(remaining) {
            if(remaining === 0)
                location.reload(true);
            document.getElementById('countdown').innerHTML = "Se actualizará en " + remaining + " segundos";
            setTimeout(function(){ countdown(remaining - 1); }, 1000);
        })(120);
 });





    </script>
    <style type="text/css">
body{
background-color: #64b6ee
}




    </style>
</head>
<body>

<div align="right" id="countdown"></div>


<div align="center" class="containerts">
    <h1>Resumen de tus equipos</h1>

    <table class="tablesorter" id="myTable-overview" style="width: 90%">
        <thead>
        <tr>
            <th>Equipo</th>
            <th>Val.</th>
            <th>Han jugado</th>
            <th>Media</th>
            <th>Proyección</th>

            <!-- <th>Actualizado</th> -->
        </tr>
        </thead>
        <#list teamMap as key,teamData>
            <tr>
                <td>${key}</td>
                <td id="biggerNum">${teamData.computedScore}</td>
                <td>${teamData.usedPlayers}</td>
                <td>${teamData.meanScorePerPlayer}</td>
                <td>${teamData.scorePrediction}</td>
            </tr>
        </#list>
    </table>
</div>

</body>
</html>
