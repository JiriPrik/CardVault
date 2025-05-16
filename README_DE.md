# Karten - Kartenverwaltungsanwendung

*Dieses README ist auch verfügbar in [Tschechisch](README_CZ.md) und [Englisch](README.md)*

Karten ist eine mobile Anwendung für Android, mit der Sie alle Ihre Karten bequem an einem Ort verwalten können. Sie können Treuekarten, Rabattkarten, Mitgliedskarten, Zahlungskarten und andere Kartentypen speichern.

<!-- Anwendungslogo wird später hinzugefügt -->

## Funktionen

- **Verwaltung verschiedener Kartentypen** - Treue-, Rabatt-, Mitglieds-, Zahlungs- und andere Karten
- **Kartenfotos** - Speichern der Vorder- und Rückseite von Karten
- **Barcodes** - Generieren und Scannen von Barcodes
- **Sicherheit** - Verschlüsseln von Fotos mit einem Passwort
- **Suche** - Schnelle Suche nach Karten nach Namen
- **Mehrsprachige Unterstützung** - Tschechisch, Englisch und Deutsch

## Screenshots

*Screenshots werden später hinzugefügt*

## Anforderungen

- Android 13.0 (API-Level 33) oder neuer
- Kamera zum Erfassen von Karten und Barcodes

## Installation

### Herunterladen und Installieren der APK

1. Laden Sie die neueste APK-Datei von [Releases](https://github.com/JiriPrik/karty/releases) oder direkt aus dem [Releases-Verzeichnis](releases/app-release.apk) herunter
2. Öffnen Sie den Dateimanager auf Ihrem Gerät
3. Suchen Sie die heruntergeladene APK-Datei und klicken Sie darauf
4. Erlauben Sie die Installation aus unbekannten Quellen, falls erforderlich
5. Folgen Sie den Installationsanweisungen

### Aus dem Quellcode erstellen

1. Klonen Sie das Repository:
   ```
   git clone https://github.com/JiriPrik/karty.git
   ```

2. Öffnen Sie das Projekt in Android Studio

3. Erstellen Sie das Projekt:
   ```
   ./gradlew assembleDebug
   ```
   oder
   ```
   ./gradlew assembleRelease
   ```

4. Installieren Sie die APK auf Ihrem Gerät:
   - Verbinden Sie Ihr Gerät über ein USB-Kabel mit Ihrem Computer
   - Aktivieren Sie das USB-Debugging auf Ihrem Gerät
   - Führen Sie aus:
     ```
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```
     oder
     ```
     adb install app/build/outputs/apk/release/app-release.apk
     ```

## Verwendung

1. **Erster Start**
   - Beim ersten Start der Anwendung legen Sie ein Passwort fest, das Sie für die Anmeldung verwenden werden
   - Dieses Passwort wird auch zur Verschlüsselung von Kartenfotos verwendet

2. **Hinzufügen einer Karte**
   - Klicken Sie auf die Schaltfläche + in der unteren rechten Ecke des Hauptbildschirms
   - Geben Sie die Karteninformationen ein (Name, Typ, Nummer)
   - Machen Sie Fotos von der Vorder- und Rückseite der Karte
   - Scannen Sie den Barcode oder geben Sie die Barcode-Daten manuell ein
   - Klicken Sie auf die Schaltfläche Speichern

3. **Anzeigen einer Karte**
   - Klicken Sie in der Liste auf eine Karte, um deren Details anzuzeigen
   - Klicken Sie auf ein Foto, um es im Vollbildmodus anzuzeigen
   - Klicken Sie auf die Schaltfläche Barcode anzeigen, um den Barcode anzuzeigen

4. **Bearbeiten einer Karte**
   - Klicken Sie in den Kartendetails auf die Schaltfläche Bearbeiten
   - Nehmen Sie die gewünschten Änderungen vor
   - Klicken Sie auf die Schaltfläche Speichern

5. **Löschen einer Karte**
   - Klicken Sie in den Kartendetails auf die Schaltfläche Löschen
   - Bestätigen Sie das Löschen der Karte

6. **Suchen nach Karten**
   - Klicken Sie auf das Lupensymbol in der oberen Leiste der Anwendung
   - Geben Sie den Kartennamen ein
   - Die Anwendung zeigt passende Karten an

7. **Ändern der Sprache**
   - Klicken Sie auf dem Startbildschirm auf die Schaltfläche CZ, EN oder DE
   - Die Anwendung wird neu gestartet und in der ausgewählten Sprache angezeigt

## Beitragen

Beiträge sind willkommen! Wenn Sie zur Entwicklung der Anwendung beitragen möchten, folgen Sie diesen Schritten:

1. Forken Sie das Repository
2. Erstellen Sie einen neuen Branch (`git checkout -b feature/amazing-feature`)
3. Nehmen Sie Ihre Änderungen vor
4. Committen Sie Ihre Änderungen (`git commit -m 'Add some amazing feature'`)
5. Pushen Sie in den Branch (`git push origin feature/amazing-feature`)
6. Öffnen Sie einen Pull Request

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die Datei [LICENSE](LICENSE) für weitere Informationen.

## Kontakt

PiKi

Projektlink: [https://github.com/JiriPrik/karty](https://github.com/JiriPrik/karty)

## Danksagungen

- Danke an alle, die zur Entwicklung dieser Anwendung beigetragen haben
- Danke für Ihre Unterstützung durch Lightning Network-Zahlungen an: jiprik@bitlifi.com
