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

29.12.25
29.12.25
```agsl
LaunchedEffect(state) {
    if (state is AuthState.Success) {
        onLoginSuccess()
    }
}
```
Compose reaguje na zmianę stanu.
Gdy backend zwróci sukces → zmienia się uiState i UI automatycznie przechodzi dalej.

To się nazywa: **state-driven navigation**

### Folder "navigation"
Zasada: „Group by feature, not by type”

nie
```agsl
ui
 ├── objects
 ├── composables
 ├── utils
```
tylko
```agsl
ui
 ├── login
 ├── register
 ├── navigation

```

## Gmail Login
- LoginScreen.kt - wyświetla przycisk „Sign in with Google”, wywołuje callback przekazany z NavGraph
- AppNavGraph.kt – przekazanie akcji w dół, zero logiki, tylko routing zdarzeń, przekazuje onGoogleSignInClick do LoginScreen
- MainActivity.kt – Google Sign-In (Android), konfiguruje Google Sign-In odpala popup Google odbiera ID TOKEN
- AuthViewModel.kt – logika aplikacji: przyjmuje idToken woła backend ustawia AuthState
- AuthRepository.kt – komunikacja z backendem, wysyła idToken do backendu (Retrofit)
- Backend /auth/google – prawdziwe logowanie weryfikuje idToken u Google sprawdza sub (unikalne ID użytkownika)tworzy użytkownika (jeśli nowy)zwraca użytkownika + JWT JEDYNE miejsce, które ufa Google

# Do zrobienia:
- moduł tłumaczeń
- walidacja inputów
- informacja zwrotna przy wypełnianiu formularza
- edytowalne konto uzytkownika
- potwierdzenie rejestracji na email

484586685422-jtqr2pc4g8q4sbrg8higek83h7d4g6qf.apps.googleusercontent.com

co to authrepository i api service

Na jutro:
- inputy i informacje pod nimi
- ekran onboardingu
- ostylować logowanie i rejestrację
- upewnić się że zostaję zalogowana na urządzeniu

Jeżeli ekran kończy „flow” (login, register, onboarding),
to NAWIGACJA POWINNA CZYŚCIĆ BACKSTACK


Onboarding → tylko UI + eventy
AuthEntryScreen → decyduje co pokazać
AuthBottomSheet → tylko kontener
Login/Register → jedyne miejsca użycia ViewModelu


# TODO
weryfikacja poprawności danych przez backend przed rejestracją.
Wyczyszczenie formularza po przejściu do ekranu głównego

Avatar - możliwość edycji avatara, usuwania, ładowania zdjęcia z telefonu.
Klikam edycje avatara -> wybieram zdjecie z telefonu -> przycinam zdjęcie -> zapisuję zdjęcie w bazie -> odświeżam ProfileScreen
Kilkam edycję avatara -> usuwam obecne zdjęcie -> baza przywraca default avatar -> odświeżam ProfileScreen

Obiekt w bazie:
avatar {
  default: "default_8.png"
  custom: "adres_customowego_zdjęcia.png"
  google: ""
}


Do zrobienia:
-dodać trass bg do ekranu tagów
-widok portfela w FocusScreen
-dodać serduszko ulubiony do DetailsPlantScreen
-pamięć podręczna kiedy nie mamy połączenia z internetem?
-ładniejsze permissions

z jakiego filtra korzysta tagsScreen?
ustawienia zapisywane w sharedPreferences?
serduszko jako komponent żeby umieścić je w details
zapisywanie pobranych danych w cache
czy można połączyć funkcje ze statistics i garden? (SessionRepository)

Dodać: ilość ukończonych i nieukończonych sesji w liczbie, procencie i wykresie, użyte tagi,
ogólne statystyki - najbardziej aktywny dzień, najdłuższa sesja