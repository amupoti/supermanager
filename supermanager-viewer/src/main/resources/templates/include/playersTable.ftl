    <thead>
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
        <th class="text-center">Acciones</th>
    </tr>
    </thead>
    <tbody align="center">
        <#assign canAct = (teamData.changesUsed < teamData.maxChanges)>
        <#list teamData.rows as row>
            <#if !row.slotRow>
                <#-- Real player row -->
                <#assign p = row.realPlayer.player>
                <tr>
                    <td align="left">${p.name}<#if p.status.injured> <span class="label label-danger">LES</span></#if></td>
                    <td>${(p.marketData["TEAM_RDM"])!"-"}</td>
                    <td>${(p.marketData["PRICE_FORMATTED"])!"-"}</td>
                    <td><b>${(p.score)!"-"}</b></td>
                    <td>${(p.marketData["KEEP_BROKER"])!"-"}</td>
                    <td>${(p.marketData["PLUS_15_BROKER"])!"-"}</td>
                    <td>${(p.marketData["MEAN_VAL"])!"-"}</td>
                    <td>${(p.position)!"-"}</td>
                    <#list row.realPlayer.matches as match>
                        <td class="${match.againstTeam.quality}" align="center" style="font-size: 80%">
                            <div class="<#if match.local>localClass<#else>awayClass</#if>">
                                ${match.againstTeam}
                            </div>
                        </td>
                    </#list>
                    <td>
                        <#if p.pendingAction == 2>
                            <form method="post" action="/users/undo-change.html" style="display:inline">
                                <input type="hidden" name="id" value="${id}"/>
                                <input type="hidden" name="idUserTeamPlayerChange" value="${p.idUserTeamPlayerChange}"/>
                                <button type="submit" class="btn btn-xs btn-warning">Deshacer compra</button>
                            </form>
                        <#elseif p.pendingAction == 1>
                            <form method="post" action="/users/undo-change.html" style="display:inline">
                                <input type="hidden" name="id" value="${id}"/>
                                <input type="hidden" name="idUserTeamPlayerChange" value="${p.idUserTeamPlayerChange}"/>
                                <button type="submit" class="btn btn-xs btn-info">Deshacer venta</button>
                            </form>
                        <#elseif canAct && (p.idPlayer > 0)>
                            <form method="post" action="/users/cancel-player.html" style="display:inline">
                                <input type="hidden" name="id" value="${id}"/>
                                <input type="hidden" name="teamId" value="${teamData.teamId}"/>
                                <input type="hidden" name="idPlayer" value="${p.idPlayer}"/>
                                <button type="submit" class="btn btn-xs btn-danger">Vender</button>
                            </form>
                        </#if>
                    </td>
                </tr>
            <#else>
                <#-- Empty roster slot (candidate or truly empty) -->
                <tr style="background-color:#f0f8ff">
                    <#if row.candidate??>
                        <td align="left"><em>${row.candidate.name}</em></td>
                        <td>${(row.candidate.marketData["TEAM"])!"-"}</td>
                        <td>${(row.candidate.marketData["PRICE_FORMATTED"])!"-"}</td>
                        <td>-</td>
                        <td>${(row.candidate.marketData["KEEP_BROKER"])!"-"}</td>
                        <td>${(row.candidate.marketData["PLUS_15_BROKER"])!"-"}</td>
                        <td>${(row.candidate.marketData["MEAN_VAL"])!"-"}</td>
                        <td>${row.candidate.position}</td>
                        <#list firstMatch..lastMatch as i><td>-</td></#list>
                        <td>
                            <#if canAct && (row.candidate.idPlayer > 0)>
                                <form method="post" action="/users/buy-player.html" style="display:inline">
                                    <input type="hidden" name="id" value="${id}"/>
                                    <input type="hidden" name="teamId" value="${teamData.teamId}"/>
                                    <input type="hidden" name="idPlayer" value="${row.candidate.idPlayer}"/>
                                    <button type="submit" class="btn btn-xs btn-success">Comprar</button>
                                </form>
                            </#if>
                        </td>
                    <#else>
                        <#-- Slot with no candidate available -->
                        <td align="left"><em style="color:#aaa">— plaza libre —</em></td>
                        <td colspan="7" style="color:#aaa">Sin candidato disponible (${row.missingPosition})</td>
                        <#list firstMatch..lastMatch as i><td>-</td></#list>
                        <td></td>
                    </#if>
                </tr>
            </#if>
        </#list>
    </tbody>
