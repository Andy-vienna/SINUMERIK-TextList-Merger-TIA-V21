::---------------------------------------------------
:: MSI-Package creator v1.0.0.8 (Optimized)
::---------------------------------------------------
@echo off
:: JDK Pfad festlegen
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: JVM-Warnungen von Maven im Batch unterdrücken
set "MAVEN_OPTS=--enable-native-access=ALL-UNNAMED"

cd /d "%~dp0"

set "JAR_NAME="

:: --- DATEN AUS POM EXTRAHIEREN (Silent Mode via Temp-Files) ---
echo Lese Projektdaten aus pom.xml...

call mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout > temp_name.txt 2>nul
set /p APP_NAME=<temp_name.txt

call mvn help:evaluate -Dexpression=project.version -q -DforceStdout > temp_version.txt 2>nul
set /p RAW_VERSION=<temp_version.txt

call mvn help:evaluate -Dexpression=app.upgrade.guid -q -DforceStdout > temp_guid.txt 2>nul
set /p APP_GUID=<temp_guid.txt

:: --- FEHLERPRÜFUNG ---
if "%APP_NAME%"=="" (echo Fehler: APP_NAME konnte nicht gelesen werden. & pause & exit /b)

:: --- VERSION KÜRZEN (für MSI) ---
for /f "tokens=1,2,3 delims=." %%a in ("%RAW_VERSION%") do set "APP_VERSION=%%a.%%b.%%c"

echo ---------------------------------------
echo pom.xml - APP_NAME: %APP_NAME%
echo pom.xml - RAW_VERSION: %RAW_VERSION%
echo pom.xml - APP_GUID: %APP_GUID%
echo ---------------------------------------

:: tmp-files wieder Aufräumen
del temp_name.txt temp_version.txt temp_guid.txt 2>nul

:: Prüfen, ob der target-Ordner überhaupt existiert
if not exist "target\" (
    echo Fehler: Der Ordner "target" existiert nicht. 
    echo Bitte zuerst "mvn package" ausfuehren!
    pause
    exit /b
)

:: Sucht automatisch nach dem passenden JAR im target Ordner, falls kein Parameter übergeben wurde
if "%~1"=="" (
    for %%f in (target\%APP_NAME%-*.jar) do set "JAR_NAME=%%~nxf"
) else (
    set "JAR_NAME=%~1"
)

:: Fehlerprüfung, falls kein JAR gefunden wurde
if "%JAR_NAME%"=="" (echo Fehler: Kein JAR in /target gefunden! & pause & exit /b)
echo gefundene jar-Datei: %JAR_NAME%
echo ---------------------------------------

:: --- Daten vorbereiten ---
set "APP_ICON=bin/icon.ico"
md "bin" 2>nul
copy /Y "target\%JAR_NAME%" "bin\%JAR_NAME%" >nul
copy /Y "icon.ico" "bin\icon.ico" >nul
copy /Y "gpl-3.0.pdf" "bin\gpl-3.0.pdf" >nul
rd /s /q "release" 2>nul

echo Erstelle MSI-Paket v%APP_VERSION%...

:: --- JPackage ausführen ---
:: HINWEIS: --main-class wurde entfernt, da jpackage sie direkt aus der Spring-Boot-JAR liest.
:: HINWEIS: --strip-native-commands und --no-header-files reduzieren die JRE-Größe im MSI.
jpackage ^
  --type msi ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --input "bin" ^
  --main-jar %JAR_NAME% ^
  --icon %APP_ICON% ^
  --dest "release" ^
  --win-shortcut ^
  --win-menu ^
  --vendor "Andy (www.aw-net.eu)" ^
  --win-upgrade-uuid %APP_GUID%

:: nach erfolgter Erzeugung Verzeichnis bin wieder löschen
rd /s /q "bin" 2>nul

echo ---------------------------------------
echo MSI erstellt: %APP_NAME%
echo GUID: %APP_GUID%
echo ---------------------------------------

:: den Maven-Zielordner (target) aufräumen
call mvn clean -q 2>nul

echo Projekt bereinigt (mvn clean)
echo ---------------------------------------
