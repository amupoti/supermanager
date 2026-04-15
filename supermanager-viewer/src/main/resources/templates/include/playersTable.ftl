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
            <td align="left">${smData.player.name}<#if smData.player.status.injured> <span class="label label-danger">LES</span><#if smData.player.idPlayer gt 0> <form method="post" action="/users/cancel-player.html" style="display:inline"><input type="hidden" name="id" value="${id}"/><input type="hidden" name="teamId" value="${teamData.teamId}"/><input type="hidden" name="idPlayer" value="${smData.player.idPlayer}"/><button type="submit" class="btn btn-xs btn-warning">Liberar</button></form></#if></#if></td>
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
      <#if teamData.candidateBuyPlayer??>
        <tr<#if !teamData.candidateAffordable> style="opacity:0.45"</#if>>
            <td align="left">
                ${teamData.candidateBuyPlayer.name}
                <#if teamData.candidateAffordable>
                    <form method="post" action="/users/buy-player.html" style="display:inline">
                        <input type="hidden" name="id" value="${id}"/>
                        <input type="hidden" name="teamId" value="${teamData.teamId}"/>
                        <input type="hidden" name="idPlayer" value="${teamData.candidateBuyPlayer.idPlayer}"/>
                        <button type="submit" class="btn btn-xs btn-success">Comprar</button>
                    </form>
                </#if>
            </td>
            <td>${(teamData.candidateBuyPlayer.marketData["TEAM"])!"-"}</td>
            <td>${(teamData.candidateBuyPlayer.marketData["PRICE_FORMATTED"])!"-"}</td>
            <td>-</td>
            <td>${(teamData.candidateBuyPlayer.marketData["KEEP_BROKER"])!"-"}</td>
            <td>${(teamData.candidateBuyPlayer.marketData["PLUS_15_BROKER"])!"-"}</td>
            <td>${(teamData.candidateBuyPlayer.marketData["MEAN_VAL"])!"-"}</td>
            <td>${teamData.candidateBuyPlayer.position}</td>
            <#list firstMatch..lastMatch as i>
                <td>-</td>
            </#list>
        </tr>
      </#if>
    </tbody>
