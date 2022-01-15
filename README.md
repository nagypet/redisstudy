# redisstudy

A spring caching mechanizmusok működését demonstráló alkalmazás

## Links

- AdminGui: https://localhost:8400/admin-gui/index.html
- Swagger: https://localhost:8400/swagger-ui/index.html
- Swagger API docs: https://localhost:8400/v2/api-docs
- Actuator: https://localhost:8400/actuator
- Health: https://localhost:8400/actuator/health
- Prometheus: https://localhost:8400/actuator/prometheus


## Build parancsok

A build parancsokat ki lehet adni mind a projekt gyökérkönyvtárában, mind akármelyik build target alkönyvtárában.

- `gradlew dist` - elkészíti az install csomagot (no fat jar). Lásd `redisstudy\build\install\redisstudy\`
- `gradlew run` - futtatja az alkalmazást
- `gradlew dockerImage or gradlew doI` - docker Image. Ezt a projekt gyökérkönyvtárában érdemes kiadni, mert akkor az összes szükséges image-et elkészíti


## Futtatás az IDE-ből
- Ha az IDE-ből futtatjuk az alkalmazást, akkor be kell állítani, hogy a build után a következő gradle parancsok lefussanak: `prepareRunInIDEA2021` vagy `prepareRunInEclipse`


## Postgres adatbázis létrehozása

- Menjünk a következő könyvtárba: `docker-compose\redisstudy` és futtassuk le ezt a parancsot `coU --infra`. Ez elindítja az adatbázist és a redist.
- A http://localhost:5400 porton érjük el a pgadmin-t. Lépjünk be, és csatlakozzunk az adatbázisra. Ennek a hostneve: postgres
- Készítsünk egy üres adatbázist redisstudy névvel
- Futtassuk le a `redisstudy\db\postgres\scripts.sql` szkriptet.

## Futtatás dockerben
- Menjünk a projekt gyökérkönyvtárába és adjuk ki: `gradlew doI`
- Ezután nyissunk egy parancssort a `docker-compose\redisstudy` könyvtárban és futtassuk le: `coU --svc`

```
C:\np\github\redisstudy\docker-compose\redisstudy>docker ps
CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS              PORTS                              NAMES
e46f19c0bd17        redisstudy-redisstudy  "sh ./redisstudy"        27 minutes ago      Up 26 minutes       8080/tcp, 0.0.0.0:8400->8400/tcp   redisstudy-webservice
29f9eeec57b8        postgres:10.13-alpine  "docker-entrypoint.s…"   2 hours ago         Up 2 hours          0.0.0.0:5432->5432/tcp             redisstudy-postgres
c8680b173460        thajeztah/pgadmin4     "python ./usr/local/…"   2 hours ago         Up 2 hours          0.0.0.0:5400->5050/tcp             redisstudy-pgadmin
9a9c60447a21        redisstudy-redis       "docker-entrypoint.s…"   2 hours ago         Up 2 hours          0.0.0.0:6379->6379/tcp             redisstudy-redis
```

## Tesztelés

Mivel ez a keretprogram egy teljes értékű alkalmazás, authentikáció szükséges a REST végpontok hívásához. Minden végpont JWT tokent használ, kivéve az /authenticate végpont, amelyik Basic authentikációval működik. Bejelentkezéshez használjuk az admin/admin felhasználónév és jelszó párost.

Az /authenticate végpont segítségével generáljunk egy tokent. 
```
{
  "sub": "admin",
  "jwt": "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0MjI1MTc2NiwiZXhwIjoxNjQyMjU1MzY2LCJ1aWQiOi0xLCJybHMiOlsiUk9MRV9FTVBUWSJdfQ.DdiHT0Uxn7WdU7Fhj2Ifj-CYB_NBXi1ockDubto8DSW65bioFekLt4KkIfG4Ucj9WrdLUmMDz3Fu2ReX3Wglsg5D9ZFcmAcutIoTpuZDx8-4Nd0F5RdWsR088TUYAvBCHLvmiKQwCqFN9h289ithOsP2ptY0DfmmZB7BkV2PP7br3Jlo8DcBT56iRvjB-1J83gdm1cXe2I04EFLZs-TRtWZ0ADuamUkGuWMMeHH9R6ptJcPj2o-Eb3c5bej1sntcfvUQvM_jSk1uNW564w2Pk0DLkSM9lkgLnKlUyKbKji5ZZJxc2n92VZMlpoaR5jgZkvGlcEB3ZjR_cNHSUdoHRQ",
  "iat": "2022-01-15 14:02:46.537",
  "exp": "2022-01-15 15:02:46.537"
}
```
Ezt a tokent használhatjuk a többi végpontnál belépésre. A swagger-ben így adjuk meg a tokent: `Bearer eyJhbGciOiJSUzUxMiJ9.eyJz...`

A book-controller segítségével hozzunk létre néhány könyvet. (`POST /books` végpont) Például:
```
{
  "authors": [
    {
      "id": 0,
      "name": "Vámos Miklós"
    }
  ],
  "dateIssued": "2012-01-15",
  "pages": 243,
  "title": "Apák könyve"
}
```

Ha a szerzőnél 0-ás id-t adunk át, akkor az alkalmazás létrehozza mind a szerző, mind a könyv entitásokat, és összerendeli őket egymással.

### Cacheable annotáció

Ezután a `GET /books/{id}` végpont segítségével lekérdezhetjük azonosító alapján a létrehozott könyvet. Erre a végpontra be van állítva a cache-elés az alábbi módon:
```
@Cacheable(cacheNames = "book", key = "#id")
public BookDTO getBookById(Long id) throws ResourceNotFoundException
{
    Optional<BookEntity> bookEntity = this.bookRepo.findById(id);
    if (bookEntity.isPresent())
    {
        return mapBookEntity2DTO(bookEntity.get());
    }

    throw new ResourceNotFoundException(String.format("Book with id %d cannot be found!", id));
}

```
A `redisstudy-webservice` konténer logjában ellenőrizhetjük a cache működését.

### Redis CLI

Lépjünk be a redis konténerbe: `docker exec -it redisstudy-redis bash`.

```
C:\np\github\redisstudy\docker-compose\redisstudy>docker exec -it redisstudy-redis bash
root@9a9c60447a21:/data# redis-cli
127.0.0.1:6379> auth apfel
OK
127.0.0.1:6379> ping
PONG
```

A Redis CLI parancsai itt találhatók: [cli](https://redis.io/commands#generic)

Pl. a kulcsokat kilistázhatjuk az alábbi paranccsal: 
```
127.0.0.1:6379> keys *
1) "book::2"
2) "book::1"
```

Az alkalmazás konfigjában úgy állítottuk be, hogy a Redis 4 óráig tárolja az adatokat. Ellenőrizhetjük, hogy mennyi idő múlva törli ki a cache az adatot:

```
127.0.0.1:6379> ttl book::1
(integer) 8712
```
Ez a kulcs 8712 másodpercig marad még a cache-ben.

### A cache invalidálása

Természetesen ha megváltozik az adat az adatbázisban, akkor a cache-t is üríteni kell. Erre szolgál a `@CacheEvict` annotáció:

```
@CacheEvict(cacheNames = "book", key = "#id")
public void updateBook(Long id, BookParams bookParams) throws ResourceNotFoundException
{
    Optional<BookEntity> byId = this.bookRepo.findById(id);
    if (!byId.isPresent())
    {
        throw new ResourceNotFoundException(String.format("Book with id %d cannot be found!", id));
    }

    createOrUpdateBookEntity(bookParams, byId.get());
}
```
