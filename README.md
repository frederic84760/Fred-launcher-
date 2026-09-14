# Fred Launcher — V0.1

Launcher Android minimaliste personnel : noir OLED, accents bleu électrique, horloge, météo, 7 applications favorites, recherche et tiroir d’applications.

## Fonctionnel dans cette V1

- Déclaré comme **launcher HOME Android** : il peut être choisi comme écran d’accueil par défaut.
- Interface Jetpack Compose plein écran OLED.
- Horloge et date en français.
- Météo réelle via Open-Meteo, sans clé API (configurée par défaut sur Gardanne).
- 7 favoris : Téléphone, Messages, WhatsApp, Gmail, Chrome, Appareil photo, Photos.
- Les favoris sont résolus à partir des apps réellement installées quand c’est possible.
- Glissement vers le haut : ouvre toutes les applications.
- Recherche instantanée dans les applications.
- Appui long sur l’accueil : réglages Fred Launcher.
- Bouton vers les réglages Android pour sélectionner Fred comme launcher par défaut.

## Fred Phone / photos des contacts

Le projet contient `phone/ContactPhotoResolver.kt`, qui sait retrouver le nom et la photo d’un contact à partir d’un numéro si l’autorisation Contacts est accordée.

**Le remplacement complet de l’application Téléphone n’est volontairement pas activé dans V0.1.** Android impose qu’une app choisie comme dialer par défaut gère correctement l’interface d’appel entrant, l’appel en cours, les appels sortants et le rôle `ROLE_DIALER`. On l’ajoutera comme étape séparée après validation du launcher.

## Ouvrir le projet

1. Installer une version récente d’Android Studio compatible avec AGP 9.4.
2. Ouvrir le dossier `FredLauncher`.
3. Laisser Gradle synchroniser le projet.
4. Connecter le Honor Magic7 Pro avec le débogage USB activé.
5. Lancer la configuration `app`.
6. Dans Fred Launcher : appui long > **Définir Fred par défaut**.

## Versions de build

- Android Gradle Plugin : 9.4.0
- Gradle : 9.6.0
- Kotlin : 2.3.21
- Jetpack Compose BOM : 2026.08.00
- compileSdk / targetSdk : 37
- minSdk : 29

## Personnalisation rapide

- Couleur principale : `ui/theme/Theme.kt` → `FredBlue`.
- Ville météo : `data/WeatherRepository.kt`.
- Liste des 7 favoris : `data/AppRepository.kt` → `FavoriteKind`.
- Fond d’écran : `res/drawable-nodpi/fred_wallpaper.webp`.

## Notes

C’est un projet V0.1 prêt à ouvrir et à compiler. Il n’a pas été compilé dans l’environnement de génération car le SDK Android n’y est pas installé. La structure Gradle inclut néanmoins le wrapper attendu pour Android Studio.
