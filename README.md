# Velora — a private digital vault for Android

Velora is an original, from-scratch Android password manager: passwords,
passkeys, secure notes, payment cards, personal identity, Wi-Fi credentials,
OTP codes, documents, API keys, and recovery codes, all in one
client-side-encrypted vault. The brand, visual language, icon system,
animations, and terminology are original — nothing here is copied from any
existing product's assets, layouts, or marketing copy.

## Brand

- **Name:** Velora
- **Personality:** private, calm, intelligent, reliable, premium — invisible
  until you need it, powerful when you do.
- **Mark:** an abstract shield/vault monogram (`core/design/icons/VeloraIcons.kt`,
  `res/drawable/ic_velora_mark.xml`) — two rounded arcs suggesting a shield's
  shoulders around a single offset "aperture" that stands in for the vault door.
- **Accent:** "Signal Violet" — a muted electric violet, deliberately not the
  generic cybersecurity blue. Full token set in `core/design/Color.kt`.

## Architecture

```
UI            Jetpack Compose, single-Activity, Navigation-Compose
Pattern       MVVM (Hilt ViewModels) + a thin repository layer
Local data    Room over SQLCipher (whole-DB AES-256 encryption)
Security      Android Keystore (StrongBox when available) + PBKDF2 envelope
              encryption, androidx.biometric, androidx.security-crypto
Sync/backend  Retrofit + OkHttp against a REST API (data/remote) — encrypted-
              blob transport only; the client never sends plaintext secrets
DI            Hilt
Async         Kotlin Coroutines + Flow
Widgets       Jetpack Glance
Autofill      Android Autofill Framework (no Accessibility Service fallback)
```

### Package layout

- `core/design` — the whole design system: color/type/spacing/shape/motion
  tokens, the original icon set, and every reusable component (buttons,
  cards, fields, empty/error states, the radial add menu, the bottom nav).
- `core/security` — Keystore crypto, PBKDF2 key derivation, the envelope-
  encryption scheme, biometric prompts, session/auto-lock, clipboard
  timeout, screenshot protection.
- `core/util` — password strength scoring, the password/passphrase
  generator, TOTP.
- `core/navigation` — the route table and nav graph.
- `data/local` — Room entities/DAOs and the SQLCipher-backed database
  provider, which opens the database the instant the vault unlocks and
  closes it the instant it locks.
- `data/remote` — Retrofit API interfaces for account/session, encrypted
  sync, and the k-anonymity breach-check lookup.
- `data/repository` — the single source of truth each feature reads from.
- `feature/*` — one package per product area (onboarding, auth, home, vault,
  search, itemdetail, passkeys, generator, security, notes, cards, identity,
  documents, otp, settings), each with its own ViewModel(s) and screen(s).
- `autofill/`, `widget/` — the Autofill service and three Glance widgets.

## Security model, in short

- The master password is **never stored**, not even hashed for comparison.
  A random 256-bit "vault key" is generated once and independently wrapped
  (AES-GCM) under a PBKDF2 key derived from the master password, and
  optionally under a PIN-derived key and an Android Keystore key gated by
  biometrics/device credential. Deleting one wrapping revokes that unlock
  method without touching the others.
- The vault key is the SQLCipher passphrase; the database only exists in
  memory-mapped form while unlocked and is closed immediately on lock.
- Documents and OTP secrets get their own Keystore-backed AES-GCM envelope
  independent of the database file, since they live outside SQLite.
- No custom cryptography anywhere — only Android Keystore, standard JCE
  primitives (PBKDF2WithHmacSHA256, AES/GCM/NoPadding), and `SecureRandom`.
- Breach checks use a k-anonymity hash-prefix design (only 5 hex characters
  of a SHA-1 hash ever leave the device) and are explicitly a point-in-time
  check, never described as real-time monitoring.
- FLAG_SECURE is applied on sensitive screens; clipboard copies are marked
  sensitive on API 33+ and cleared after a configurable timeout.

See `core/security/VaultKeyManager.kt` for the full envelope-encryption
implementation and rationale.

## What's implemented

Every screen in the brief has a real, wired-up implementation: cinematic
onboarding, account + master password + PIN + biometric setup, the unlock
flow with a recovery-code path, the Vault Home dashboard, category
browsing, global search, the password detail/add/edit flow, passkeys,
the signature password generator (scramble-in animation, strength ring),
the Security Center with an explainable score and breach monitoring,
secure notes, virtual payment cards, personal identity records, Wi-Fi
credentials, a live TOTP authenticator, encrypted document storage,
API keys, recovery codes, custom items, and the full settings surface
(security, vault, sync, appearance, notifications, privacy).

## What's intentionally out of scope for this build

- **No real backend.** `data/remote` defines the API contracts (auth,
  encrypted sync, breach-check) against a placeholder host
  (`BuildConfig.API_BASE_URL`); every call fails soft so the vault stays
  fully usable offline, exactly as the requirements ask for local-first
  behavior.
- **Passkey creation** records what a completed Credential Manager flow
  would produce; wiring it to a real relying party needs a live WebAuthn
  challenge this project doesn't have a server for.
- **Tablet layout** currently adapts spacing rather than shipping the full
  three-pane list/detail rail described in the brief — the single-pane
  screens are responsive but not yet re-composed into a rail + list +
  detail layout.
- The passphrase generator ships a small original word list; a production
  build should swap in a vetted large list (e.g. EFF's) from a raw resource.

## Building

This was built in a sandbox without the Android SDK installed, so it has
not been compiled here. It's a standard Gradle/AGP/Compose project — open
it in Android Studio (Koala+) or run:

```
./gradlew assembleDebug
```

`minSdk` is 26 (required for the Autofill Framework and modern Keystore
APIs); `compileSdk`/`targetSdk` are 35.
