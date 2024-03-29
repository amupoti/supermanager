    <thead >
    <tr>
        <th>Jugador</th>
        <th class="text-center">Equipo</th>
        <th class="text-center">Precio</th>
        <th class="text-center">Val.</th>
        <th class="text-center">= broker</th>
        <th class="text-center">+15% broker</th>
        <th class="text-center">Media</th>
        <th class="text-center">Pos.</th>

        <#list firstMatch..lastMatch as i>
            <th align="center">J ${i}</th>
        </#list>
    </tr>
    </thead>
    <tbody align="center">
        <#list teamData.playerList as smData>
        <tr>
            <td align="left">${smData.player.name}</td>
            <td>${(smData.player.marketData["TEAM_RDM"])!"-"}</td>
            <td>${(smData.player.marketData["PRICE_FORMATTED"])!"-"}</td>
            <td><b>${(smData.player.score)!"-"}</b></td>
            <td>${(smData.player.marketData["KEEP_BROKER"])!"-"}</td>
            <td>${(smData.player.marketData["PLUS_15_BROKER"])!"-"}</td>
            <td>${(smData.player.marketData["MEAN_VAL"])!"-"}</td>
            <td>${(smData.player.position)!"-"}</td>
            <#list smData.matches as match>
                 <td class="${match.againstTeam.quality}" align="center" style="font-size: 80%">
                     <div class="<#if match.local>localClass<#else>awayClass</#if>">
                         ${match.againstTeam}</td>
                     </div>
                 </td>
             </#list>
        </tr>
      </#list>
    </tbody>
