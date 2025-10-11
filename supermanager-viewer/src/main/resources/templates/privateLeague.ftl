<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Supermanager - Resumen</title>
  <#include "./include/header.vm">

  <link rel="stylesheet" href="supermanager.css"> <!-- el teu CSS separat -->

  <script>
    $(document).ready(function() {
      $('.containerts').each(function() {
        $(this).children(".tablesorter").tablesorter({
          widthFixed: false,
          widgets: ['zebra']
        });
      });

      // Countdown auto-refresh
      (function countdown(remaining) {
        if (remaining === 0) location.reload(true);
        document.getElementById('countdown').innerHTML =
          "Se actualizará en " + remaining + " segundos";
        setTimeout(function(){ countdown(remaining - 1); }, 1000);
      })(120);
    });
  </script>
</head>

<body>
  <header>
    <div id="countdown"></div>
  </header>

  <main class="containerts">
    <h1>Resumen de tus equipos</h1>

    <table class="tablesorter" id="myTable-overview">
      <thead>
        <tr>
          <th>Equipo</th>
          <th>Val.</th>
          <th>Han jugado</th>
          <th>Media</th>
          <th>Proyección</th>
        </tr>
      </thead>
      <tbody>
        <#list teamMap as key, teamData>
          <tr>
            <td>${key}</td>
            <td id="biggerNum">${teamData.computedScore}</td>
            <td>${teamData.usedPlayers}</td>
            <td>${teamData.meanScorePerPlayer}</td>
            <td>${teamData.scorePrediction}</td>
          </tr>
        </#list>
      </tbody>
    </table>
  </main>
</body>
</html>
