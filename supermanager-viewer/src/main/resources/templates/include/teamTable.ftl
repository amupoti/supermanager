
    <thead>
    <tr>
        <th class="text-center">Val. equipo</th>
        <th class="text-center">Han jugado</th>
        <th class="text-center">Media</th>
        <th class="text-center">Broker</th>
        <th class="text-center">Valor</th>
        <th class="text-center">Caja</th>
        <th class="text-center">Cambios</th>
    </tr>
    </thead>
    <tbody align="center">
        <tr>
            <td><b>${teamData.score}</b></td>
            <td>${teamData.usedPlayers}</td>
            <td>${teamData.meanScorePerPlayer}</td>
            <td>${teamData.totalBroker}</td>
            <td>${teamData.teamBroker}</td>
            <td>${teamData.cash}</td>
            <td><#assign remaining = teamData.maxChanges - teamData.changesUsed><span class="<#if remaining == 0>label label-danger<#elseif remaining == 1>label label-warning<#else>label label-success</#if>">${teamData.changesUsed}/${teamData.maxChanges}</span>
                <#if teamData.changesUsed gt 0>
                <form method="post" action="/users/cancel-all-changes.html" style="display:inline; margin-left:6px" onsubmit="return confirm('¿Cancelar todos los cambios de este equipo?')">
                    <input type="hidden" name="id" value="${id}"/>
                    <input type="hidden" name="teamId" value="${teamData.teamId}"/>
                    <button type="submit" class="btn btn-xs btn-danger">Cancelar cambios</button>
                </form>
                </#if>
            </td>
        </tr>
    </tbody>
