Projekt: Mobilní aplikace "Karty"
1. Cíl aplikace
   Aplikace "Karty" bude sloužit jako digitální peněženka pro ukládání a správu různých typů karet, jako jsou věrnostní karty, členské karty, pojišťovací kartičky, a případně i vizuální záznamy platebních karet (bez ukládání citlivých údajů způsobem, který by umožnil přímou platbu). Cílem je snížit počet fyzických karet, které uživatel musí nosit.

2. Cílová platforma
   Primárně: Android

3. Funkční požadavky
   Přidávání karet:
   Ruční zadání údajů (název karty, číslo, typ, poznámky).
   Naskenování čárového/QR kódu karty.
   Vyfocení přední a zadní strany karty.
   Zobrazení seznamu karet: Přehledný seznam uložených karet.
   Detail karty: Zobrazení všech informací o kartě, včetně obrázků a čárového kódu (možnost zobrazení čárového kódu na celou obrazovku pro snadné skenování u pokladny).
   Editace a mazání karet.
   Kategorizace/tagování karet (volitelné).
   Vyhledávání a filtrování karet.

4.  Doporučené úložiště dat

Pro ukládání dat o kartách (textové informace, cesty k obrázkům, data čárových kódů) se doporučuje použít databázi SQLite.

Proč SQLite?
Je to lehká, souborová databáze, která nevyžaduje samostatný server.
Výborně se integruje s Pythonem (vestavěný modul sqlite3).
Je standardem pro lokální úložiště na mobilních zařízeních.
Umožňuje strukturované ukládání dat a dotazování.
Ukládání obrázků: Samotné obrázky karet (fotografie) budou uloženy jako soubory v interním úložišti aplikace. V SQLite databázi se budou ukládat pouze cesty k těmto souborům.
Šifrování: Pro citlivé údaje (pokud byste se rozhodli ukládat např. celé číslo karty, i když se to obecně nedoporučuje pro platební karty) je nutné zvážit šifrování. Můžete šifrovat konkrétní textová pole před uložením do SQLite pomocí Python knihovny jako cryptography, nebo zvážit použití SQLCipher (což je rozšíření SQLite pro šifrování celé databáze, ale zvyšuje komplexitu sestavení). Pro začátek je vhodné se zaměřit na ukládání pouze nezbytných a méně citlivých údajů. Pro platební karty by se mělo jednat spíše o referenční záznam (poslední 4 čísla, banka, typ) než o kompletní údaje.