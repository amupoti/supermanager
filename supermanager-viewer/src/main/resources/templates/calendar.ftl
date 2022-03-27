<!DOCTYPE HTML>
<html>
<head>
    <title>Calendario</title>
    <#include "./include/header.vm">
    <script>


    </script>
</head>
</script>
<style type="text/css">
    body{
    background-color: #64b6ee
    }
</style>
<body>

    <div align="center" class="containerts">
        <h1>Calendario</h1>


        <table style="width: 90%" id="myTable-overview" class="tablesorter">
        <thead>
        <tr>
            <th>Equipo</th>
            <#list firstMatch..34 as i>
                <th align="center">J ${i}</th>
            </#list>
        </tr>
        </thead>
        <#list teamsData?sort_by("team") as team>
            <tr>
                <td align="center"><b>${team.team}</b></td>
                <#list firstMatch..34 as i>

                    <#assign match = team.matches[i-1] >
                    <td class="${match.againstTeam.quality}" align="center" style="font-size: 80%">
                         <div class="<#if match.local>localClass<#else>awayClass</#if>">
                             ${match.againstTeam}</td>
                         </div>
                     </td>
                 </#list>
            </tr>

        </#list>
        </table>
    </div>

</body>
</html>