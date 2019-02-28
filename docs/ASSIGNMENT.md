Mestská polícia
------
Vašou úlohou je navrhnúť databázový systém mestskej polície. Systém sa sústredí menej na úradné práce a organizačnú štruktúru, no kladie dôraz na činnosť polície, vyšetrovanie zločinov, udeľovanie pokút, zatykanie a uzatváranie prípadov.

Uvažujme len jednu stanicu (abstraktnú, môže mať veľmi veľa zamestnancov). Na stanici pracujú zamestnanci, ktorí patria do rôznych oddelení, pričom každé oddelenie má vedúceho. Zamestnanci sa venujú prípadom a akciám, ktoré môžu byť spojené s jedným alebo viacerými miestami činu, pričom miesta činu sa viažu na mestské časti. S prípadom tiež môžu súvisieť osoby (svedkovia, podozriví a poškodení). Každý typ prípadu je nejakej kategórie (napr. “dopravná nehoda”, “vražda” atď., vymyslite si).

Rozpoznávame tieto typy prípadov a akcií:
- **priestupok**, kde sú potvrdeným podozrivým udeľované pokuty,
- **vyšetrovanie trestného činu**, kde sa potvrdeným podozrivým udeľujú zatykače a
- **ochranná akcia**, ktorá sa spája s miestom, no nie s ďalšími osobami.

Zamestnanci sú týchto typov:
- **Administratívny alebo IKT pracovník** je bežným zamestnancom. Nemôže sa zaoberať prípadmi.
- **Policajný inšpektor** vyšetruje priestupky a trestné činy a môže sa zúčastniť na ochranných akciách. V rámci prípadov môže identifikovať a potvrdiť stav osoby na mieste činu ako podozrivého alebo poškodeného. Pri priestupkoch udeľuje pokuty, pri trestných činoch vydáva zatykače.
- **Príslušník policajného zboru** má činnosť podobnú ako policajný inšpektor, ale nezaoberá sa pokutami. Pri ochrannej akcii môže byť veliteľom. Má hodnosť a nemôže sa stať, že by príslušník zboru velil prípadu, na ktorom pracuje iný príslušník zboru vyššej hodnosti.
- **Policajný vyšetrovateľ** sa venuje iba trestným činom, ktorých vyšetrovanie môže aj viesť. Vie identifikovať a potvrdiť stav osoby miesta činu ako podozrivého alebo poškodeného a môže vydávať zatykače.
- **Forenzný analytik** sa venuje prípadom vyšetrovania, ktoré si vyžadujú expertízu v určitej oblasti. Môže ísť o analýzu účtovníctva, digitálnu analýzu, audio a video forenznú analýzu a forenznú psychológiu. Môže identifikovať a potvrdiť stav osoby ako podozrivého alebo poškodeného.

Osoba spojená s prípadom a miestom činu je buď svedok, poškodený alebo podozrivý. Poškodený alebo podozrivý musí byť pred uzavretím prípadu potvrdený. Akonáhle je podozrivý potvrdený, môže sa mu udeliť trest – vydať pokuta (priestupok) alebo zatykač (trestný čin). S pokutou sa viaže výška pokuty.

## CRUD operácie
### Zamestnanec
- vypíš zamestnancov a ich údaje
- vytvor zamestnanca
- aktualizuj údaje a oddelenie zamestnanca
- aktualizuj prípady, na ktorých zamestnanec pracuje
- vymaž zamestnanca
### Oddelenie
- vypíš oddelenia s ich vedúcimi
- vytvor oddelenie
- aktualizuj oddelenie
- vymaž oddelenie
### Prípad
- vypíš údaje prípadu a zamestnancov, osoby a miesta, ktoré s ním súvisia
- vytvor prípad
- aktualizuj zamestnancov, osoby alebo miesta, ktoré s ním súvisia
### Miesto činu
- vypíš miesta činu, súvisiace mestské časti a prípady
- vytvor a naviaž miesto činu k prípadu a osobám
- aktualizuj prípady alebo osoby, ktoré sa s miestom činu viažu
### Trest
- vypíš zoznam trestov
- vytvor trest (pokutu alebo zatykač)

## Zložitejšie doménové operácie

### Pridelenie vyšetrovateľov
Systém prípadu pridelí vyšetrovateľov a vedúceho prípadu podľa ich typu. Prideľovanie by malo byť “inteligentné”, t.j. k prípadu by sa mali prioritne prideliť zamestnanci, ktorí sa podieľajú na menšom počte prípadov. Ak ide o príslušníka policajného zboru, prípadu nemôže veliť taký príslušník, ktorého hodnosť je nižšia ako iného príslušníka, ktorý na prípade už pracuje.
### Udelenie trestu
Podozrivému udelíme trest. Ak ide o priestupok, podľa typu sa mu udelí pokuta s adekvátnou výškou. Ak ide o trestný čin, vydá sa zatykač. Podozrivý však pred udelením trestu musí byť potvrdený.
### Uzavretie prípadu
Prípad sa môže uzavrieť, ak všetci poškodení a podozriví boli potvrdení a všetkým podozrivým bol udelený trest. Ak je v prípade poškodený či podozrivý, ktorý bol potvrdený, no nebol mu udelený trest, udelí sa a prípad sa uzavrie. Ak niektorá osoba okrem svedkov ešte nebola potvrdená, prípad sa uzavrieť nemôže.
### Povýšenie
Prezrite každého zamestnanca a uzavreté prípady, na ktorých pracoval. Ak množstvo takýchto prípadov, ktorým velil, o 20% presiahne množstvo veliteľa jeho oddelenia, stáva sa veliteľom on (bývalý veliteľ prestáva veliť oddeleniu). Ak ide o príslušníka policajného zboru, zároveň zvýšte jeho hodnosť.
### Presun vyšetrovateľov
Zamestnanci policajnej stanice by mali pokrývať prípady rovnomerne podľa počtu podozrivých a poškodených prípadu. Ak existuje prípad pokrytý neadekvátne veľkým množstvom zamestnancov, presuňte niektorých z nich na menej zastúpený prípad.

## Štatistiky
### Nebezpečné mestské časti
Pre každý mesiac, štvrťrok a rok (jedným príkazom SELECT) vypíšte tri najnebezpečnejšie mestské časti (také, kde sa udialo najviac priestupkov a trestných činov) s počtom prípadov a ich typov.
### Policajt mesiaca
Pre každý mesiac vypíšte zamestnanca, ktorý (1) uzavrel najviac prípadov a (2) potvrdil najviac podozrivých. Pre oboch vypíšte aj ich predchodcov.
