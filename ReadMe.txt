4.4.1

ogólna zasada działania programu: oryginalny node wysyła do wszystkich sąsiadów zadanie, po czym one robią to samo, a następnie oczekują na odpowiedź którą wysyłają dalej, aż wróci do oryginalnego węzła

(3.2) aby wyłączyć całą sieć, należy podłączyć klienta, który wyśle polecenie terminate do wszystkich węzłów

Komunikacja węzłów:

-„node”: sygnał dla węzła, że próbuje się połączyć inny węzeł. do listy zostaje dodane nowe połączenie, w którym węzeł nasłuchuje nadchodzących wiadomości

-„UNDONE”: sygnał rozsyłany między węzłami, że zadanie zostało zakończone i wysłane do klienta; przygotowanie na kolejnego klienta.

-„return <arg>”: każdy kolejny węzeł otrzymuje wiadomość od swoich podwęzłów, aż ostatecznie nie trafi ona do listy odpowiedzi węzła połączonego z klientem, który jest odpowiedzialny za wysłanie odpowiedzi do klienta

-komendy z projektu typu "get-value <arg>": Po otrzymaniu node wykonuje polecenie. następnie przesyła zadanie dalej do następnych węzłów i oczekuje na odpowiedź. po uzyskaniu odpowiedzi zbiera je w całość i zwraca je dalej,
	aż do węzła odpowiedzialnego za komunikacje z klientem.

-"terminate": węzeł oznajmia, że zostaje zamkniety. sąsiadujące węzły usuwają go z listy sąsiadów po czym zamykane jest połączenie.

Komunikacja z klientem:

Jeżeli komunikacja nie rozpocznie się od "node", węzeł zakłada, że połączył się klient i zaczyna wykonywać zadanie. 

4.4.2

1. wypakować archiwum
2. wejść do folderu projektu \SKJ_projekt\src i skopiować ścieżkę
3. uruchomić wiersz poleceń i wpisać komendę "cd <ścieżka skopiowana w punkcie 2>"
4. następnie uruchomić komendę "javac *.java"
5. uruchomić skrypt np. "script-7-1"

4.4.3

wszystko poza punktem 3.4

działające skrypty sprawdzające:
"script-1-0.bat"
"script-1-1_1.bat"
"script-1-1_2.bat"
"script-1-1_3.bat"
"script-1-1_4.bat"
"script-2-0_1.bat"
"script-2-1_1.bat"
"script-2-1_2.bat"
"script-3-0_1.bat"
"script-3-1_1.bat"
"script-5-1.bat"
"script-7-1.bat"
"script-7-2.bat"

4.4.4

nie działa skrypt "script-7-p.bat", który sprawdza implementację systemu umożliwiającą
pracę przy większej niż 1 liczbie klientów korzystających w danym momencie z bazy (punkt 3.4).