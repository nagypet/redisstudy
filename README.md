# redisstudy

[Magyar verzió](https://github.com/nagypet/redisstudy/blob/development/README_hu.md)

An application that demonstrates the operation of spring caching mechanisms.

## Links

- AdminGui: https://localhost:8400/admin-gui/index.html
- Swagger: https://localhost:8400/swagger-ui/index.html
- Swagger API docs: https://localhost:8400/v2/api-docs
- Actuator: https://localhost:8400/actuator
- Health: https://localhost:8400/actuator/health
- Prometheus: https://localhost:8400/actuator/prometheus


## Build commands

Build commands can be issued both in the root directory of the project and in any subdirectory.

- `gradlew dist` - creates the install package (no fat jar). See in: `redisstudy\build\install\redisstudy\`
- `gradlew run` - runs the application
- `gradlew dockerImage or gradlew doI` - docker Image. It is worth executing this in the root directory of the project, because then it will create all the necessary images.


## Running from the IDE
- If you are running the application from the IDE, you need to configure the following gradle commands to run after build: `prepareRunInIDEA2021` or `prepareRunInEclipse`.


## Creating the Postgres database

- Let's go to the following directory: `docker-compose\redisstudy` and run this command `coU --infra`. This will start up the database and the redis service.
- pgadmin can be accessed through: http://localhost:5400. Log in and connect to the database. Its hostname is postgres
- Create an empty database named redisstudy
- Run this script: `redisstudy\db\postgres\scripts.sql`

## Running in docker
- Go to the root directory of the project and execute this command: `gradlew doI`
- Then open a command prompt at `docker-compose\redisstudy` and run: `coU --svc`

```
C:\np\github\redisstudy\docker-compose\redisstudy>docker ps
CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS              PORTS                              NAMES
e46f19c0bd17        redisstudy-redisstudy  "sh ./redisstudy"        27 minutes ago      Up 26 minutes       8080/tcp, 0.0.0.0:8400->8400/tcp   redisstudy-webservice
29f9eeec57b8        postgres:10.13-alpine  "docker-entrypoint.s…"   2 hours ago         Up 2 hours          0.0.0.0:5432->5432/tcp             redisstudy-postgres
c8680b173460        thajeztah/pgadmin4     "python ./usr/local/…"   2 hours ago         Up 2 hours          0.0.0.0:5400->5050/tcp             redisstudy-pgadmin
9a9c60447a21        redisstudy-redis       "docker-entrypoint.s…"   2 hours ago         Up 2 hours          0.0.0.0:6379->6379/tcp             redisstudy-redis
```

## Testing

Because this framework is a full-featured application, authentication is required to call REST endpoints. All endpoints use a JWT token, except for the / authenticate endpoint, which works with Basic authentication. Use the admin / admin username and password pair to log in.

Use the /authenticate endpoint to generate a token.
```
{
  "sub": "admin",
  "jwt": "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0MjI1MTc2NiwiZXhwIjoxNjQyMjU1MzY2LCJ1aWQiOi0xLCJybHMiOlsiUk9MRV9FTVBUWSJdfQ.DdiHT0Uxn7WdU7Fhj2Ifj-CYB_NBXi1ockDubto8DSW65bioFekLt4KkIfG4Ucj9WrdLUmMDz3Fu2ReX3Wglsg5D9ZFcmAcutIoTpuZDx8-4Nd0F5RdWsR088TUYAvBCHLvmiKQwCqFN9h289ithOsP2ptY0DfmmZB7BkV2PP7br3Jlo8DcBT56iRvjB-1J83gdm1cXe2I04EFLZs-TRtWZ0ADuamUkGuWMMeHH9R6ptJcPj2o-Eb3c5bej1sntcfvUQvM_jSk1uNW564w2Pk0DLkSM9lkgLnKlUyKbKji5ZZJxc2n92VZMlpoaR5jgZkvGlcEB3ZjR_cNHSUdoHRQ",
  "iat": "2022-01-15 14:02:46.537",
  "exp": "2022-01-15 15:02:46.537"
}
```
This token can be used to log in to other endpoints. In the swagger, enter the token as follows: `Bearer eyJhbGciOiJSUzUxMiJ9.eyJz ...`

Use the book-controller to create some books. (`POST /books` endpoint) For example:
```
{
  "authors": [
    {
      "id": 0,
      "name": "Ernest Hamingway"
    }
  ],
  "dateIssued": "2012-01-15",
  "pages": 243,
  "title": "The Old Man And The Sea"
}
```

If we pass an id of 0 to the author, the application creates both the author and book entities and associates them with each other.

### Cacheable annotation

The `GET /books/{id}` endpoint can then be used to query the created book by ID. This endpoint is cached as follows:
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
You can check the cache operation in the `redisstudy-webservice` container log.

### Redis CLI

Enter the redis container: `docker exec -it redisstudy-redis bash`.

```
C:\np\github\redisstudy\docker-compose\redisstudy>docker exec -it redisstudy-redis bash
root@9a9c60447a21:/data# redis-cli
127.0.0.1:6379> auth apfel
OK
127.0.0.1:6379> ping
PONG
```

Redis CLI commands can be found here: [cli](https://redis.io/commands#generic)

For example, you can list the keys with the following command:
```
127.0.0.1:6379> keys *
1) "book::2"
2) "book::1"
```

In the application configuration, Redis is set to store data for up to 4 hours. You can check how long a key will reside in the cache before beeing cleared:

```
127.0.0.1:6379> ttl book::1
(integer) 8712
```
This key remains in the cache for 8712 seconds.

### Invalidating the cache

Of course, if the data in the database changes, the cache must also be emptied. This is what the `@CacheEvict` annotation is for:

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
