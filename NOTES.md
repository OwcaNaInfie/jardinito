## Server

```
pupcia@Pupciolot:~/AndroidStudioProjects/Jardinito/backend$ node server.js

[dotenv@17.2.3] injecting env (2) from .env --
tip: ⚙️  specify custom .env file path with { path: '/custom/path/.env' }
Server running on port 5000
MongoDB connected
```
Backend działa, łączy się z MongoDB Atlas i nasłuchuje.

✔ Node.js + Express uruchomiony

✔ Połączenie z MongoDB Atlas

#### Backend zawsze testujemy osobno.

#### Retrofit
Retrofit to biblioteka w Androidzie, która umożliwia komunikację aplikacji z backendem przez HTTP. wysyłanie zapytań do serwera wygląda jak wywoływanie zwykłych funkcji w Kotlinie.

Gdyby nie Retrofit, musiałabym: ręcznie otwierać połączenia HTTP, budować requesty, parsować JSON, obsługiwać błędy, mapować dane na obiekty.

## Bezpieczeństwo rejestracji i logowania
Proces rejestracji i logowania użytkowników został zaprojektowany z uwzględnieniem podstawowych zasad bezpieczeństwa, takich jak haszowanie haseł, brak przechowywania danych wrażliwych w postaci jawnej oraz przeniesienie całej logiki autoryzacyjnej do warstwy serwerowej. Dzięki temu system jest odporny na podstawowe zagrożenia związane z nieautoryzowanym dostępem.
- hasła nigdy nie są przechowywane w postaci jawnej
- podczas rejestracji hasło użytkownika jest haszowane przy użyciu biblioteki bcrypt.
  do bazy danych trafia hash, nie hasło
- nawet administrator bazy nie zna haseł użytkowników
- w przypadku wycieku danych hasła pozostają chronione

**Podczas logowania:**
- użytkownik wysyła hasło, a backend porównuje je z hashem w bazie (bcrypt.compare)
- aplikacja mobilna nigdy nie dostaje informacji o haśle
- brak logiki bezpieczeństwa po stronie klienta
- odporność na manipulację aplikacją

Adres e-mail użytkownika jest oznaczony jako unique w schemacie MongoDB.
Uniemożliwia założenie wielu kont na ten sam e-mail
integralność danych użytkowników

**Cały proces rejestracji i logowania realizowany jest w warstwie backendowej:**

Android odpowiada tylko za UI i wysyłanie żądań

**Backend odpowiada za:**
- walidację
- bezpieczeństwo
- komunikację z bazą danych
- zgodność z architekturą klient–serwer
- łatwa rozbudowa (np. JWT, role użytkowników)

**Komunikacja odbywa się przez jasno zdefiniowane endpointy REST:**
- POST /api/auth/register
- POST /api/auth/login
- dane przesyłane w formacie JSON
- łatwe testowanie i rozwój
- możliwość integracji z innymi klientami (np. web)



## Nowe pojęcia
#### suspend
oznacza, że funkcja może się zatrzymać (zawiesić) bez blokowania aplikacji.
**bez suspend:**
aplikacja próbowałaby wykonać to na głównym wątku, UI by się zawiesiło, Android rzuciłby wyjątek

suspend pozwala: pauzować funkcję, oddać wątek innym zadaniom, wznowić ją, gdy odpowiedź przyjdzie


#### sealed class
o reprezentacji stanu interfejsu użytkownika zastosowano klasę typu sealed, co pozwala na jednoznaczne
określenie wszystkich możliwych stanów ekranu oraz zapewnia bezpieczeństwo typów na etapie kompilacji.

Kompilator wie, że stan może być TYLKO:
- Idle
- Loading
- Success
- Error


26.12.25
UI (Compose) → ViewModel → Retrofit → Express → MongoDB Atlas → odpowiedź → UI
Rejestracja i Logowanie