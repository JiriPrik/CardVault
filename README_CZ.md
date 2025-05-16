# Karty - Aplikace pro správu karet

*Tento README je také dostupný v [angličtině](README.md) a [němčině](README_DE.md)*

Karty je mobilní aplikace pro Android, která umožňuje pohodlně spravovat všechny vaše karty na jednom místě. Můžete ukládat věrnostní karty, slevové karty, členské karty, platební karty a další typy karet.

<!-- Logo aplikace bude přidáno později -->

## Funkce

- **Správa různých typů karet** - věrnostní, slevové, členské, platební a jiné
- **Fotografie karet** - uložení přední a zadní strany karty
- **Čárové kódy** - generování a skenování čárových kódů
- **Zabezpečení** - šifrování fotografií pomocí hesla
- **Vyhledávání** - rychlé vyhledávání karet podle názvu
- **Vícejazyčná podpora** - čeština, angličtina a němčina

## Snímky obrazovky

*Snímky obrazovky budou přidány později*

## Požadavky

- Android 13.0 (API úroveň 33) nebo novější
- Fotoaparát pro snímání karet a čárových kódů

## Instalace

### Stažení a instalace APK

1. Stáhněte si nejnovější APK soubor z [releases](https://github.com/JiriPrik/karty/releases) nebo přímo z [adresáře releases](releases/app-release.apk)
2. Na zařízení otevřete správce souborů
3. Najděte stažený APK soubor a klikněte na něj
4. Povolte instalaci z neznámých zdrojů, pokud je to vyžadováno
5. Postupujte podle pokynů pro instalaci

### Sestavení ze zdrojového kódu

1. Naklonujte repozitář:
   ```
   git clone https://github.com/JiriPrik/karty.git
   ```

2. Otevřete projekt v Android Studiu

3. Sestavte projekt:
   ```
   ./gradlew assembleDebug
   ```
   nebo
   ```
   ./gradlew assembleRelease
   ```

4. Nainstalujte APK na zařízení:
   - Připojte zařízení k počítači pomocí USB kabelu
   - Povolte ladění USB na zařízení
   - Spusťte:
     ```
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```
     nebo
     ```
     adb install app/build/outputs/apk/release/app-release.apk
     ```

## Použití

1. **První spuštění**
   - Při prvním spuštění aplikace si nastavíte heslo, které budete používat pro přihlášení
   - Toto heslo je také použito pro šifrování fotografií karet

2. **Přidání karty**
   - Klikněte na tlačítko + v pravém dolním rohu hlavní obrazovky
   - Vyplňte informace o kartě (název, typ, číslo)
   - Vyfotografujte přední a zadní stranu karty
   - Naskenujte čárový kód nebo zadejte data čárového kódu ručně
   - Klikněte na tlačítko Uložit

3. **Zobrazení karty**
   - Klikněte na kartu v seznamu pro zobrazení detailů
   - Klikněte na fotografii pro zobrazení na celou obrazovku
   - Klikněte na tlačítko Zobrazit čárový kód pro zobrazení čárového kódu

4. **Úprava karty**
   - V detailu karty klikněte na tlačítko Upravit
   - Proveďte požadované změny
   - Klikněte na tlačítko Uložit

5. **Smazání karty**
   - V detailu karty klikněte na tlačítko Smazat
   - Potvrďte smazání karty

6. **Vyhledávání karet**
   - Klikněte na ikonu lupy v horní liště aplikace
   - Zadejte název karty
   - Aplikace zobrazí odpovídající karty

7. **Změna jazyka**
   - Na úvodní obrazovce klikněte na tlačítko CZ, EN nebo DE
   - Aplikace se restartuje a zobrazí se ve vybraném jazyce

## Přispívání

Příspěvky jsou vítány! Pokud chcete přispět k vývoji aplikace, postupujte takto:

1. Forkněte repozitář
2. Vytvořte novou větev (`git checkout -b feature/amazing-feature`)
3. Proveďte změny
4. Commitněte změny (`git commit -m 'Add some amazing feature'`)
5. Pushněte do větve (`git push origin feature/amazing-feature`)
6. Otevřete Pull Request

## Licence

Tento projekt je licencován pod licencí MIT - viz soubor [LICENSE](LICENSE) pro více informací.

## Kontakt

PiKi

Odkaz na projekt: [https://github.com/JiriPrik/karty](https://github.com/JiriPrik/karty)

## Poděkování

- Děkujeme všem, kteří přispěli k vývoji této aplikace
- Děkujeme za podporu prostřednictvím Lightning Network plateb na adresu: jiprik@bitlifi.com
