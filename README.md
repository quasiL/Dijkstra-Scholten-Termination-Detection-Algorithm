# Detekce ukončení - Algoritmus Dijkstra-Scholten

## Popis
Tento Java program implementuje detekci ukončení pomocí algoritmu Dijkstra-Scholten. Algoritmus je navržen pro 
distribuované systémy k detekci, kdy všechny uzly v síti dokončily své úkoly.

## Použití

Pro spuštění programu postupujte podle níže uvedených pokynů:

## Spuštění prvního uzlu
    java -jar dsv.jar <ip_adresa> <port>

Nahraďte <ip_adresa> a <port> požadovanou IP adresou a portem pro první uzel.

## Spuštění dalších uzlů

    java -jar dsv.jar <ip_adresa> <port> <existující_uzel_ip> <existující_uzel_port>

Nahraďte <ip_adresa> a <port> IP adresou a portem nového uzlu. <existující_uzel_ip> a <existující_uzel_port> jsou IP adresa a port 
libovolného již běžícího uzlu v síti, odkud si nový uzel může získat informace.

## Příkazy uzlu

Po spuštění uzlu můžete vykonávat následující příkazy:
</br>
1. Inicializace výpočtů.

        init

2. Získání informací.

        info [-a] [-v] [-b] [-n]

   - -a: Zobrazit všechny informace
   - -v: Zobrazit proměnné
   - -b: Zobrazit základní informace
   - -n: Zobrazit informace o sousedech

Můžete kombinovat parametry pro získání konkrétních informací. Například:

        info -vb
