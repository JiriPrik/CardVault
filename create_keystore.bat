@echo off
echo Vytvarim keystore.jks soubor...

REM Hledani cesty k JDK
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

REM Vytvoreni keystore souboru
"%JAVA_HOME%\bin\keytool" -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias karty1key -storepass karty1password -keypass karty1password -dname "CN=CardVault, OU=Development, O=JiriPrik, L=Prague, S=Prague, C=CZ"

echo Keystore soubor byl vytvoren.
pause
