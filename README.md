# Roadies

Roadies è una piattaforma per l'organizzazione, la prenotazione e la recensione di viaggi e attività, con chat in tempo reale tra organizzatori e viaggiatori. Il backend è realizzato con un'architettura a **microservizi Spring Boot**, il frontend mobile è un'app Android nativa (Kotlin).

## Indice

- [Architettura](#architettura)
- [Stack tecnologico](#stack-tecnologico)
- [Avvio del progetto](#avvio-del-progetto)
- [Servizi e funzionalità](#servizi-e-funzionalità)
- [Ruoli e utenti di test](#ruoli-e-utenti-di-test)
- [Database](#database)
- [Licenza](#licenza)

## Architettura

Il backend (`backend/`) è un progetto Maven multi-modulo composto da 10 moduli:

| Servizio | Ruolo | Porta |
|---|---|---|
| `eureka-service` | Service discovery (Netflix Eureka) | 8761 |
| `config-service` | Configurazione centralizzata (Spring Cloud Config Server, config native da classpath) | 8888 |
| `api-gateway` | Reverse proxy (Spring Cloud Gateway) con routing verso i servizi, circuit breaker e relay del token OAuth2 | 8443 (HTTPS) |
| `user-service` | Profili utente, amicizie, gamification, moderazione admin | interno (8080) |
| `travel-service` | Viaggi, attività, partenze, liste dei preferiti, raccomandazioni | interno (8080) |
| `booking-service` | Prenotazioni, pagamenti Stripe, saga di riserva posti | interno (8080) |
| `review-service` | Recensioni e risposte alle recensioni | interno (8080) |
| `chat-service` | Chat 1:1 tra viaggiatore e organizzatore via WebSocket/STOMP | interno (8080) |
| `notification-service` | Invio email transazionali (consumer RabbitMQ, nessun endpoint REST) | interno |
| `shared-lib` | Libreria condivisa: sicurezza JWT/Keycloak, MinIO, RabbitMQ, auditing, i18n, OpenAPI, Feign, DTO di evento | — |

Tutti i servizi business si registrano su Eureka e sono raggiungibili solo attraverso l'`api-gateway` (unico servizio esposto via HTTPS oltre a Eureka/Config/Keycloak/RabbitMQ/MinIO). La comunicazione asincrona tra servizi avviene tramite **RabbitMQ** (eventi di dominio: riserva posti, gamification, amicizie, recensioni, email), mentre le chiamate sincrone dirette usano **Feign** con retry/circuit breaker (Resilience4j).

Ogni microservizio business ha il proprio database PostgreSQL dedicato (`user_db`, `travel_db`, `booking_db`, `review_db`, `chat_db`), oltre al DB di Keycloak.

## Stack tecnologico

- **Java 21**, **Spring Boot 4.0.5**, **Spring Cloud 2025.1.2**
- Spring Cloud Gateway, Netflix Eureka, Spring Cloud Config
- Spring Data JPA + PostgreSQL, Hibernate Envers (audit), Hibernate `@SoftDelete`
- Spring Security OAuth2 Resource Server + **Keycloak** (autenticazione/autorizzazione a ruoli)
- Spring AMQP (**RabbitMQ**) per la messaggistica tra servizi
- Spring WebSocket/STOMP per la chat in tempo reale
- **MinIO** (S3-compatible) per lo storage di documenti, avatar e immagini
- **Stripe** per i pagamenti
- Spring Mail (SMTP Gmail) per le notifiche via email
- MapStruct, springdoc-openapi (Swagger), Zipkin (tracing distribuito)
- Docker / Docker Compose per l'orchestrazione locale

## Avvio del progetto

### Prerequisiti

- [Docker](https://www.docker.com/) e Docker Compose
- JDK 21 e Maven (solo se vuoi buildare/eseguire i servizi al di fuori di Docker)

### 1. Clona il repository

```bash
git clone <repository-url>
cd Roadies
```

### 2. Certificato TLS per API Gateway

Assicurati di inserire il certificato `roadies.p12` all'interno della cartella `resources` dell'API Gateway, nel percorso: `backend/api-gateway/src/main/resources/roadies.p12`.

### 3. Configura le variabili d'ambiente

Copia il file di esempio e valorizza le variabili con le tue credenziali (Postgres, Keycloak, Stripe, MinIO, SMTP, keystore TLS):

```bash
cp .env.example .env
```

Variabili richieste (vedi `.env.example`):

| Variabile | Descrizione |
|---|---|
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | Credenziali condivise da tutti i database PostgreSQL |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | Credenziali admin dell'istanza Keycloak |
| `CLIENT_SECRET` | Client secret del client Keycloak `gateway-client` |
| `STRIPE_sk` / `STRIPE_pk` | Chiavi Stripe (test) segreta/pubblica |
| `WHSEC` | Signing secret del webhook Stripe |
| `MINIO_ADMIN` / `MINIO_ADMIN_PASSWORD` | Credenziali root MinIO |
| `SECRET_KEY` | Chiave applicativa generica |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenziali SMTP (Gmail, richiede una App Password) |
| `KEY_STORE_PASS` | Password del keystore TLS `roadies.p12` usato dall'api-gateway |

### 4. Builda i moduli backend

I `Dockerfile` dei servizi si aspettano il jar già compilato in `target/` (`COPY target/*.jar app.jar`), quindi prima di buildare le immagini è necessario compilare tutto il progetto Maven multi-modulo:

**macOS / Linux**

```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

**Windows (cmd / PowerShell)**

```bat
cd backend
mvnw.cmd clean package -DskipTests
cd ..
```

### 5. Avvia l'intero stack con Docker Compose

```bash
docker compose up --build
```

Questo comando builda le immagini di tutti i microservizi e avvia, in ordine di dipendenza (via `depends_on`/healthcheck):

1. Database PostgreSQL (uno per Keycloak e uno per ciascun servizio business), RabbitMQ, MinIO, Zipkin, Keycloak (con import automatico dei realm da `keycloak/import`)
2. `eureka-service` → `config-service`
3. I microservizi business (`travel`, `review`, `chat`, `user`, `booking`, `notification`)
4. `api-gateway`
5. `stripe-cli`, che inoltra automaticamente gli eventi webhook di Stripe verso `booking-service`

### 6. Verifica che tutto sia attivo

| Servizio | URL |
|---|---|
| API Gateway (HTTPS, entry point unico per l'app) | `https://localhost:8443` |
| Eureka Dashboard | `http://localhost:8761` |
| Keycloak Admin Console | `http://localhost:8081` |
| RabbitMQ Management | `http://localhost:15672` |
| MinIO Console | `http://localhost:9001` |
| Zipkin (tracing) | `http://localhost:9411` |

> Il certificato TLS dell'api-gateway (`roadies.p12`) è autofirmato: è normale che il browser/i client mostrino un warning in ambiente locale.

### 7. Arresto

```bash
docker compose down          # ferma i container
docker compose down -v       # ferma i container e rimuove anche i volumi
```

## Servizi e funzionalità

### api-gateway

Punto di ingresso unico dell'applicazione (Spring Cloud Gateway). Instrada le richieste ai servizi tramite Eureka (`lb://`), applica **circuit breaker** (Resilience4j) con endpoint di fallback e inoltra il token OAuth2 dell'utente ai servizi a valle (**token relay**).

| Path | Servizio target |
|---|---|
| `/api/v1/travels/**`, `/api/v1/activities/**`, `/api/v1/metadata/**`, `/api/v1/favourite-lists/**` | travel-service |
| `/api/v1/users/**`, `/api/v1/user-documents/**`, `/api/v1/friends/**`, `/api/v1/admin/**` | user-service |
| `/api/v1/reviews/**` | review-service |
| `/api/v1/conversations/**`, `/ws/**` | chat-service |
| `/api/v1/bookings/**`, `/api/v1/payments/**`, `/api/v1/booking-documents/**` | booking-service |

### user-service — Gestione utenti, amicizie, gamification

- **Profilo utente**: sincronizzazione automatica al login (`POST /sync`), consultazione (`GET /me`), modifica profilo, upload avatar, ricerca pubblica per username.
- **Ruolo organizzatore**: richiesta di upgrade a `ORGANIZER` (`POST /request-organizer`) e relativa approvazione/rifiuto da parte di un admin.
- **Amicizie**: invio/risposta a richieste di amicizia, lista amici (sintetica e dettagliata), richieste pendenti inviate/ricevute, rimozione amico.
- **Documenti d'identità**: upload, consultazione e cancellazione documenti
- **Amministrazione**: blocco/sblocco utenti, elenco richieste organizzatore pendenti, elenco utenti per stato, classifica dei migliori viaggiatori per punti gamification.
- **Gamification**: punteggio e badge assegnati/rimossi automaticamente in reazione agli eventi di prenotazione (via RabbitMQ).
- **Integrazioni**: Keycloak Admin API (blocco utenti, gestione ruoli), MinIO (documenti/avatar), RabbitMQ (eventi amicizia consumati da travel-service, eventi gamification da booking-service).

### travel-service — Viaggi, attività e raccomandazioni

- **Viaggi**: creazione, modifica, cancellazione, ricerca pubblica con filtri (destinazione, prezzo, durata, continente, paese), dettaglio pubblico, elenco per organizzatore, upload immagini.
- **Partenze (departures)**: aggiunta, modifica, cancellazione, conferma e consultazione delle date di partenza di un viaggio, con gestione posti disponibili.
- **Attività**: stessa gestione dei viaggi (CRUD, partenze/sessioni, immagini) per attività standalone o collegate a un viaggio.
- **Raccomandazioni**: elenco di viaggi consigliati, personalizzato per utente autenticato o generico per utenti anonimi (basato su tag/punteggi di affinità).
- **Liste preferiti**: creazione liste con visibilità (privata/pubblica/condivisa con amici specifici), aggiunta/rimozione viaggi o attività, condivisione con amici.
- **Metadati**: elenco tag disponibili, elenco continenti, creazione nuovi tag (admin).
- **Integrazioni**: MinIO (immagini), RabbitMQ (riserva/rilascio posti richiesto da booking-service, aggiornamento rating da review-service, mirror locale delle amicizie da user-service), Feign verso booking-service per validare le prenotazioni attive di un utente.

### booking-service — Prenotazioni e pagamenti

- **Flusso di prenotazione a step**: creazione bozza (`DRAFT`) → riserva posti (`PENDING`) → inserimento partecipanti → conferma/pagamento, con consultazione stato e cancellazione in ogni fase.
- **Scadenza automatica**: le prenotazioni non pagate scadono tramite code RabbitMQ a TTL (5 min di proroga pagamento, 15 min di scadenza definitiva), con rilascio automatico dei posti e cancellazione dei documenti caricati.
- **Documenti partecipanti**: upload della foto del documento d'identità di ogni membro della prenotazione.
- **Pagamenti**: creazione di un Payment Intent Stripe e gestione del webhook Stripe (verifica firma, aggiornamento stato prenotazione).
- **Notifiche**: invio automatico di email di conferma/cancellazione prenotazione tramite notification-service.
- **Integrazioni**: Stripe, MinIO (documenti), RabbitMQ (saga di riserva posti con travel-service, punti gamification verso user-service, invio email), Feign verso travel-service.

### review-service — Recensioni

- Creazione, modifica e cancellazione di recensioni (voto + commento) su un viaggio o un'attività, con vincolo di una recensione per utente.
- Consultazione pubblica delle recensioni e del voto medio.
- Risposta dell'organizzatore a una recensione.
- **Integrazioni**: RabbitMQ (notifica a travel-service per l'aggiornamento del rating medio), Feign verso travel-service per validare la relazione utente/viaggio prima di consentire la recensione.

### chat-service — Chat in tempo reale

- Conversazioni 1:1 tra viaggiatore e organizzatore: creazione/recupero, elenco conversazioni dell'utente, storico messaggi paginato, marcatura messaggi come letti.
- Messaggistica in tempo reale via **WebSocket/STOMP** (endpoint `/ws`, con fallback SockJS): invio messaggio (`/app/chat.sendMessage`) e broadcast sul topic `/topic/conversation/{id}`.
- Autenticazione JWT anche sulle connessioni STOMP (intercettore dedicato sul frame CONNECT).

### notification-service — Notifiche email

- Servizio "worker" privo di API REST: consuma eventi dalla coda RabbitMQ `send-mail-queue` e invia email transazionali via SMTP (es. conferma/cancellazione prenotazione generate da booking-service).

### Servizi infrastrutturali

- **eureka-service**: server di service discovery Netflix Eureka.
- **config-service**: Spring Cloud Config Server, distribuisce la configurazione a tutti i servizi da file bundled nel classpath (`src/main/resources/configs`).
- **shared-lib**: libreria comune con configurazione di sicurezza JWT/Keycloak, client MinIO, converter JSON per RabbitMQ, auditing JPA (`@CreatedBy`/`@LastModifiedBy`), i18n, configurazione Feign e OpenAPI, DTO degli eventi di dominio condivisi tra i servizi.

## Ruoli e utenti di test

L'autorizzazione è gestita da Keycloak tramite ruoli realm (`TRAVELER`, `ORGANIZER`, `ADMIN`) mappati su `ROLE_*` in ogni microservizio. Il realm `roadies-app` importato automaticamente all'avvio (`keycloak/import/roadies-app-realm.json`) contiene già tre utenti di test, uno per ruolo. Per ciascuno, **email e password coincidono con il nome del ruolo in inglese**:

| Ruolo | Username | Email | Password |
|---|---|---|---|
| Traveler | `traveler` | `traveler@traveler` | `traveler` |
| Organizer | `organizer` | `org@org` | `organizer` |
| Admin | `admin` | `admin@email.com` | `admin` |

### TRAVELER — il viaggiatore

- Gestisce il proprio profilo (sincronizzazione al login, modifica dati, upload avatar) e può richiedere l'upgrade a `ORGANIZER`.
- Cerca viaggi/attività pubblici, consulta dettagli, recensioni e raccomandazioni personalizzate.
- Crea e gestisce le proprie **liste dei preferiti** (private, pubbliche o condivise con amici specifici).
- Effettua l'intero flusso di **prenotazione**: bozza → riserva posti → inserimento partecipanti → pagamento (Stripe) → conferma, incluso l'upload dei documenti d'identità dei partecipanti.
- Consulta le proprie prenotazioni attive/passate e può cancellarle.
- Scrive, modifica ed elimina **recensioni** sui viaggi/attività prenotati.
- Gestisce le **amicizie**: invia/accetta/rifiuta richieste, consulta e rimuove amici.
- Carica i propri documenti d'identità.
- Chatta in tempo reale con l'organizzatore di un viaggio (creazione conversazione, invio/lettura messaggi).

### ORGANIZER — chi organizza viaggi e attività

Eredita le funzionalità base di profilo/amicizie/chat del `TRAVELER` e in più:

- Crea, modifica ed elimina **viaggi** e **attività** (incluse quelle collegate a un viaggio), con upload immagini.
- Gestisce le **partenze/sessioni**: aggiunta, modifica, cancellazione e conferma delle date disponibili, con relativa capienza posti.
- Consulta l'elenco dei propri viaggi/attività
- **Risponde alle recensioni** ricevute sui propri viaggi/attività.
- Chatta con i viaggiatori interessati ai propri viaggi.

### ADMIN — amministrazione della piattaforma

- **Modera gli utenti**: blocca/sblocca account.
- **Gestisce le richieste di upgrade a organizzatore**: consulta le richieste pendenti e le approva o rifiuta (con motivazione).
- Consulta l'elenco utenti filtrato per stato e la classifica dei migliori viaggiatori per punti gamification.
- Crea nuovi **tag** di classificazione per viaggi/attività (metadati).
- Può scrivere recensioni come un `TRAVELER` (ruolo abilitato anche su `POST /api/v1/reviews/{travelId}`).

## Database

Ogni servizio business ha un proprio container PostgreSQL isolato, definito in `docker-compose.yml`:

- `keycloak-db` (`keycloak`), `user-db` (`user_db`), `travel-db` (`travel_db`), `booking-db` (`booking_db`), `review-db` (`review_db`), `chat-db` (`chat_db`)

Lo schema di ciascun database è gestito automaticamente da Hibernate (`spring.jpa.hibernate.ddl-auto=update`) all'avvio dei servizi: non sono richieste migrazioni manuali.

## Licenza

Distribuito con licenza **GNU General Public License v3.0** — vedi il file [LICENSE](LICENSE).
