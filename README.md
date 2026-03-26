# matrikkel-wls-auth
Tilpasninger for å få matrikkel på weblogic til å fungere med to-faktor autentisering


## Publisering
Pakkene ble tidligere publisert til Nexus. Gamle versjoner av pakkene er migrert til GitHub Packages.

Nye versjoner av pakker publiseres til [GitHub Packages](https://github.com/orgs/kartverket/packages?repo_name=matrikkel-wls-auth) via [build-push.yml](.github/workflows/build-publish.yml) workflowen.
Ved hver push til `master` så vil det bygges og publiseres en ny versjon av pakkene.

## Releasetesting
Tester kjøres automatisk som en del av [build-push.yml](.github/workflows/build-publish.yml) workflowen ved PR og push til `master`.


## Versjonering
Pakken har versjonsnummer som er av formatet `[Major version].[Date].[SHA]`
Versjonsnummeret oppdateres ved hver publisering.

`[Major version]` oppdateres ved breaking changes og kan endres i [build-push.yml](.github/workflows/build-publish.yml) workflowen.
