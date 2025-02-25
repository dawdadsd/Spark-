@echo off
setlocal

:: 设置目标目录
set HADOOP_DIR=%~dp0..\hadoop
set BIN_DIR=%HADOOP_DIR%\bin

:: 创建目录
if not exist "%HADOOP_DIR%" mkdir "%HADOOP_DIR%"
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

:: 下载 winutils.exe 和 hadoop.dll
powershell -Command "& {
    $baseUrl = 'https://github.com/cdarlint/winutils/raw/master/hadoop-3.3.5/bin'
    $files = @('winutils.exe', 'hadoop.dll')

    foreach ($file in $files) {
        $url = "$baseUrl/$file"
        $output = Join-Path '%BIN_DIR%' $file

        Write-Host "正在下载 $file..."
        Invoke-WebRequest -Uri $url -OutFile $output
    }
}"

echo Hadoop 二进制文件下载完成！
pause