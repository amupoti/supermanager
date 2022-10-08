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
        })(60);
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

    <table style="width: 90%" id="myTable-overview" class="tablesorter">
        <thead>
        <tr>
            <th>Equipo</th>
            <th>Val.</th>
            <th>Han jugado</th>
            <th>Media</th>
            <th>Proyección</th>
        </tr>
        </thead>
        <#list teamMap as key,teamData>
        <tr>

            <td><a href="${teamData.teamUrl}">${key}</a></td>
            <td id="biggerNum">${teamData.computedScore}</td>
            <td>${teamData.usedPlayers}</td>
            <td>${teamData.meanScorePerPlayer}</td>
            <td>${teamData.scorePrediction}</td>
        </tr>
        </#list>
    </table>
</div>

<div align="center" class="containerts">

   <#list teamMap as key,teamData>


            <h3>${key} - ${teamData.score} (${teamData.computedScore}) </h3>
            <table style="width: 90%" id="myTeamTable-$key" class="tablesorter">
                <#include "./include/teamTable.ftl">
            </table>

            <table style="width: 90%" id="myTable-$key" class="tablesorter">
                <#include "./include/playersTable.ftl">
            </table>

   </#list>
</div>


<#if  username == "amupoti" >
  <li> <b><a href="/cache/clear.html" class="btn btn-info" role="button">Clear cache</a></b>
 </#if>

</body>
</html>
