    <thead >
    <tr>
        <th>Jugador</th>
        <th class="text-center">Equipo</th>
        <th class="text-center">Precio</th>
        <th class="text-center">= broker</th>
        <th class="text-center">+15% broker</th>
        <th class="text-center">Últ 3</th>
        <th class="text-center">Pos.</th>
        <th class="text-center">Val.</th>
<!--
        #foreach($i in [$firstMatch..$lastMatch])
            <th align="center">J $i</th>
        #end
-->
    </tr>
    </thead>
    <tbody align="center">
        <#list teamData.playerList as smData>
        <tr>
            <td align="left">${smData.player.name}</td>
            <td>${(smData.player.marketData.get("TEAM_RDM"))!"-"}</td>
            <td>${(smData.player.marketData.get("PRICE"))!"-"}</td>
            <td>${(smData.player.marketData.get("KEEP_BROKER"))!"-"}</td>
            <td>${(smData.player.marketData.get("PLUS_15_BROKER"))!"-"}</td>
            <td>${(smData.player.marketData.get("LAST_THREE_VAL"))!"-"}</td>
            <td>${(smData.player.position)!"-"}</td>
            <td>${(smData.player.score)!"-"}</td>
 
        </tr>
      </#list>
    </tbody>
