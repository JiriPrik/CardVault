# Plán vývoje aplikace "Karty" - Checklist

## 1. Analýza požadavků a návrh architektury

### 1.1 Shrnutí požadavků
- [ ] Definice cíle aplikace: Digitální peněženka pro ukládání a správu různých typů karet
- [ ] Určení cílové platformy: Android
- [ ] Definice hlavních funkcí:
  - [ ] Přidávání karet (ruční zadání, skenování kódů, focení)
  - [ ] Zobrazení seznamu karet
  - [ ] Detail karty s možností zobrazení čárového kódu na celou obrazovku
  - [ ] Editace a mazání karet
  - [ ] Kategorizace/tagování karet
  - [ ] Vyhledávání a filtrování karet

### 1.2 Technická specifikace
- [ ] Výběr programovacího jazyka: Python s Kivy/KivyMD pro multiplatformní vývoj
- [ ] Výběr databáze: SQLite pro ukládání dat o kartách
- [ ] Určení způsobu ukládání obrázků: Interní úložiště aplikace
- [ ] Návrh zabezpečení: Základní šifrování citlivých údajů

### 1.3 Návrh architektury
- [ ] Definice architektury: Model-View-Controller (MVC)
- [ ] Návrh modulů:
  - [ ] Databázový modul (správa SQLite)
  - [ ] Modul pro správu karet (CRUD operace)
  - [ ] Modul pro skenování kódů
  - [ ] Modul pro práci s fotoaparátem
  - [ ] UI modul (obrazovky aplikace)

## 2. Návrh databáze

### 2.1 Struktura databáze
- [ ] Návrh tabulky `cards`:
  - [ ] `id` (INTEGER PRIMARY KEY)
  - [ ] `name` (TEXT) - název karty
  - [ ] `card_number` (TEXT) - číslo karty
  - [ ] `card_type` (TEXT) - typ karty
  - [ ] `notes` (TEXT) - poznámky
  - [ ] `barcode_data` (TEXT) - data čárového/QR kódu
  - [ ] `barcode_type` (TEXT) - typ kódu (QR, EAN, apod.)
  - [ ] `front_image_path` (TEXT) - cesta k obrázku přední strany
  - [ ] `back_image_path` (TEXT) - cesta k obrázku zadní strany
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)

- [ ] Návrh tabulky `categories`:
  - [ ] `id` (INTEGER PRIMARY KEY)
  - [ ] `name` (TEXT) - název kategorie

- [ ] Návrh tabulky `card_categories` (vazební tabulka pro M:N vztah):
  - [ ] `card_id` (INTEGER) - cizí klíč na cards.id
  - [ ] `category_id` (INTEGER) - cizí klíč na categories.id

- [ ] Vytvoření SQL skriptu pro inicializaci databáze

## 3. Vývoj uživatelského rozhraní

### 3.1 Návrh obrazovek
- [ ] Návrh hlavní obrazovky (seznam karet):
  - [ ] Zobrazení karet v seznamu/mřížce
  - [ ] Vyhledávací pole
  - [ ] Filtrování podle kategorií
  - [ ] Tlačítko pro přidání nové karty

- [ ] Návrh obrazovky detailu karty:
  - [ ] Zobrazení všech informací o kartě
  - [ ] Možnost zobrazení čárového kódu na celou obrazovku
  - [ ] Tlačítka pro editaci a smazání

- [ ] Návrh obrazovky pro přidání/editaci karty:
  - [ ] Formulář pro zadání údajů
  - [ ] Tlačítka pro skenování kódu a focení karty
  - [ ] Výběr kategorií

- [ ] Návrh obrazovky nastavení:
  - [ ] Obecná nastavení aplikace
  - [ ] Správa kategorií

### 3.2 Implementace UI
- [ ] Vytvoření základního layoutu aplikace
- [ ] Implementace jednotlivých obrazovek:
  - [ ] Hlavní obrazovka
  - [ ] Obrazovka detailu karty
  - [ ] Obrazovka pro přidání/editaci karty
  - [ ] Obrazovka nastavení
- [ ] Implementace navigace mezi obrazovkami
- [ ] Vytvoření responzivního designu pro různé velikosti obrazovek

## 4. Implementace funkcionality

### 4.1 Databázový modul
- [ ] Vytvoření třídy pro správu databáze
- [ ] Implementace CRUD operací:
  - [ ] Vytvoření záznamu (Create)
  - [ ] Čtení záznamu (Read)
  - [ ] Aktualizace záznamu (Update)
  - [ ] Mazání záznamu (Delete)
- [ ] Implementace metod pro vyhledávání a filtrování

### 4.2 Modul pro správu karet
- [ ] Implementace třídy Card pro reprezentaci karty
- [ ] Implementace metod pro práci s kartami:
  - [ ] Přidání nové karty
  - [ ] Editace existující karty
  - [ ] Mazání karty
- [ ] Implementace logiky pro kategorizaci karet

### 4.3 Modul pro skenování kódů
- [ ] Výběr a integrace knihovny pro skenování čárových/QR kódů (např. ZBar, ZXing)
- [ ] Implementace rozhraní pro skenování
- [ ] Testování skenování různých typů kódů

### 4.4 Modul pro práci s fotoaparátem
- [ ] Implementace přístupu k fotoaparátu zařízení
- [ ] Implementace pořizování snímků
- [ ] Implementace ukládání a načítání obrázků

## 5. Testování

### 5.1 Unit testy
- [ ] Příprava testovacího prostředí
- [ ] Implementace unit testů pro jednotlivé moduly:
  - [ ] Testy databázového modulu
  - [ ] Testy modulu pro správu karet
  - [ ] Testy modulu pro skenování kódů
  - [ ] Testy modulu pro práci s fotoaparátem

### 5.2 Integrační testy
- [ ] Návrh integračních testů
- [ ] Implementace testů spolupráce mezi moduly
- [ ] Testování datových toků v aplikaci

### 5.3 UI testy
- [ ] Implementace testů uživatelského rozhraní
- [ ] Testování uživatelských scénářů:
  - [ ] Přidání nové karty
  - [ ] Editace karty
  - [ ] Mazání karty
  - [ ] Vyhledávání a filtrování

### 5.4 Testování na různých zařízeních
- [ ] Testování na různých verzích Androidu
- [ ] Testování na různých velikostech obrazovek
- [ ] Testování na různých typech zařízení (telefony, tablety)

## 6. Optimalizace a ladění

### 6.1 Výkonnostní optimalizace
- [ ] Profilování aplikace a identifikace úzkých míst
- [ ] Optimalizace databázových dotazů
- [ ] Optimalizace práce s obrázky
- [ ] Optimalizace paměťové náročnosti

### 6.2 Optimalizace uživatelského rozhraní
- [ ] Optimalizace plynulosti animací
- [ ] Zlepšení responzivity UI
- [ ] Optimalizace načítání seznamu karet

### 6.3 Optimalizace spotřeby baterie
- [ ] Efektivní práce s kamerou a skenováním
- [ ] Optimalizace běhu na pozadí
- [ ] Snížení spotřeby při zobrazení čárových kódů

## 7. Dokumentace

### 7.1 Uživatelská dokumentace
- [ ] Vytvoření návodu k použití aplikace
- [ ] Popis všech funkcí aplikace
- [ ] Vytvoření FAQ (Frequently Asked Questions)
- [ ] Příprava nápovědy přímo v aplikaci

### 7.2 Vývojářská dokumentace
- [ ] Popis architektury aplikace
- [ ] Dokumentace API a modulů
- [ ] Instalační instrukce
- [ ] Dokumentace databázového schématu
- [ ] Popis build procesu

## 8. Nasazení

### 8.1 Příprava pro publikaci
- [ ] Vytvoření instalačního balíčku (APK)
- [ ] Podepsání aplikace
- [ ] Optimalizace velikosti aplikace
- [ ] Příprava marketingových materiálů (ikona, snímky obrazovky)

### 8.2 Testování před publikací
- [ ] Finální testování na různých zařízeních
- [ ] Beta testování s vybranými uživateli
- [ ] Oprava nalezených chyb

### 8.3 Publikace
- [ ] Vytvoření účtu vývojáře na Google Play
- [ ] Příprava stránky aplikace na Google Play
- [ ] Publikace aplikace na Google Play

## 9. Údržba a aktualizace

### 9.1 Sběr zpětné vazby
- [ ] Implementace mechanismu pro sběr zpětné vazby v aplikaci
- [ ] Monitorování recenzí na Google Play
- [ ] Analýza uživatelského chování pomocí analytických nástrojů

### 9.2 Plánování aktualizací
- [ ] Vytvoření systému pro sledování a opravu chyb
- [ ] Plánování nových funkcí na základě zpětné vazby
- [ ] Pravidelné aktualizace pro udržení kompatibility s novými verzemi Androidu

## 10. Časový harmonogram

### Fáze 1: Analýza a návrh (2 týdny)
- [ ] Týden 1: Analýza požadavků a návrh architektury
  - [ ] Analýza požadavků
  - [ ] Návrh architektury
  - [ ] Výběr technologií
- [ ] Týden 2: Návrh databáze a uživatelského rozhraní
  - [ ] Návrh databáze
  - [ ] Návrh UI
  - [ ] Vytvoření prototypu

### Fáze 2: Základní implementace (4 týdny)
- [ ] Týden 3-4: Implementace databázového modulu a základního UI
  - [ ] Implementace databázového modulu
  - [ ] Implementace základního UI
- [ ] Týden 5-6: Implementace základních funkcí pro správu karet
  - [ ] Implementace CRUD operací pro karty
  - [ ] Implementace seznamu karet
  - [ ] Implementace detailu karty

### Fáze 3: Pokročilá implementace (3 týdny)
- [ ] Týden 7: Implementace skenování kódů
  - [ ] Integrace knihovny pro skenování
  - [ ] Implementace rozhraní pro skenování
- [ ] Týden 8: Implementace práce s fotoaparátem
  - [ ] Implementace přístupu k fotoaparátu
  - [ ] Implementace ukládání obrázků
- [ ] Týden 9: Implementace vyhledávání a filtrování
  - [ ] Implementace vyhledávacího rozhraní
  - [ ] Implementace filtrování podle kategorií

### Fáze 4: Testování a optimalizace (2 týdny)
- [ ] Týden 10: Testování
  - [ ] Unit testy
  - [ ] Integrační testy
  - [ ] UI testy
- [ ] Týden 11: Optimalizace
  - [ ] Výkonnostní optimalizace
  - [ ] Optimalizace UI
  - [ ] Optimalizace spotřeby baterie

### Fáze 5: Finalizace a nasazení (1 týden)
- [ ] Týden 12: Finalizace a nasazení
  - [ ] Dokumentace
  - [ ] Příprava pro publikaci
  - [ ] Beta testování
  - [ ] Publikace na Google Play

## 11. Technické požadavky

### 11.1 Vývojové prostředí
- [ ] Instalace a konfigurace vývojového prostředí:
  - [ ] Python 3.8+
  - [ ] Kivy/KivyMD framework
  - [ ] Android SDK
  - [ ] Buildozer pro kompilaci na Android
  - [ ] IDE (PyCharm, VS Code nebo jiné)

### 11.2 Knihovny a závislosti
- [ ] Instalace a konfigurace potřebných knihoven:
  - [ ] SQLite pro Python
  - [ ] Knihovna pro práci s čárovými kódy (ZBar, ZXing)
  - [ ] Knihovna pro práci s fotoaparátem
  - [ ] Případně knihovna pro šifrování (cryptography)

### 11.3 Hardwarové požadavky pro vývoj
- [ ] Zajištění hardwarových požadavků:
  - [ ] Počítač s dostatečným výkonem pro vývoj
  - [ ] Android zařízení pro testování (nebo emulátor)

### 11.4 Minimální požadavky pro cílové zařízení
- [ ] Definice minimálních požadavků pro cílové zařízení:
  - [ ] Android 6.0 (API level 23) nebo vyšší
  - [ ] Fotoaparát
  - [ ] Dostatečné úložiště pro ukládání obrázků karet

## 12. Sledování postupu

### 12.1 Systém pro sledování úkolů
- [ ] Vytvoření systému pro sledování úkolů (např. Trello, GitHub Projects)
- [ ] Rozdělení úkolů podle fází vývoje
- [ ] Pravidelná aktualizace stavu úkolů

### 12.2 Pravidelné kontrolní body
- [ ] Stanovení pravidelných kontrolních bodů (milestones)
- [ ] Kontrola plnění časového harmonogramu
- [ ] Vyhodnocení postupu a případná úprava plánu
