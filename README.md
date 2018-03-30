# spring-webflux-demo
A Spring WebFlux demo application.

## Start the application

Mongodb and Redis are needed to start the application.
`src/resourcs/applicaitn.yml` specifies the configuration of *dev* and *prod* environment.

### *dev* environment
 
Make sure Mongodb and Redis are accessible.
`./gradlew bootRun` to start the application.

### *prod* environment

Docker is used to deploy the application.
Make sure docker is installed.

```bash
./gradlew build # build a jar file
docker-compose up --build -d # start the application in docker
```
