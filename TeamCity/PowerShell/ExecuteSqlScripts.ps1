param(
    [string] $scriptsDir,
    [string] $DBMS,
    [string] $DeployedDatabaseName,
    [string] $PublishDBServerName,
    [string] $OracleHost
)

set nls_lang=RUSSIAN_CIS.RU8PC866
Write-Output "SriptsDirectory - $scriptsDir"

#ищем все sql файлы в папке епика
$sqlScripts = (Get-ChildItem -Path $scriptsDir -Include *.sql -Exclude *CreateNewUser*, *CreateDBData* -Recurse)
Write-Output "Found Sql scripts - $sqlScripts"
#для возможности выполнения скриптов в определенном порядке сортируем их
$sqlScripts = $sqlScripts | Sort-Object Name
Write-Output "Sorted SqlScripts - $sqlScripts"

$variables = "database=$DeployedDatabaseName"

#если скрипты были найдены то выполняем каждый из их
If ($sqlScripts) {
    ForEach ($item In $sqlScripts) {
        $sqlScriptDir = $item.Fullname
        $scriptName = $item.name
        If ("$DBMS" -eq "MSSQL") {
            #в параметрах ServerInstance- адрес сервера, Database- название базы данных
            Invoke-Sqlcmd -QueryTimeout 1500 -inputfile "$item" -ServerInstance "$PublishDBServerName" -Database "$DeployedDatabaseName" -Variable $variables
        }
        Elseif ("$DBMS" -eq "Oracle") {
            sqlplus $DeployedDatabaseName/$DeployedDatabaseName@$OracleHost/BPMBUILD @$item
        }
        Write-Output "Script $scriptName successfully executed"
    }
}
else {
    Write-Warning "Warning: There are no 'SQL scripts' in $scriptsDir"
}