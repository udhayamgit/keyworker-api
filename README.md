# keyworker-service

OMIC Keyworker Service


#### Env Variables:

```properties
      SERVER_PORT=8081
      SPRING_PROFILES_ACTIVE=dev
      API_GATEWAY_TOKEN=***
      API_GATEWAY_PRIVATE_KEY=***
      USE_API_GATEWAY_AUTH=false
      JWT_PUBLIC_KEY=secret
      ELITE2_API_URI_ROOT=http://localhost:8080/api
```

### Setting secrets

The token is sent to you after submitting the `client.pub` file to the `https://nomis-api-access.service.justice.gov.uk` site

```bash
openssl ecparam -name prime256v1 -genkey -noout -out client.key
openssl ec -in client.key -pubout -out client.pub
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in client.key -out client.pkcs8.key
cat client.pkcs8.key | base64 > API_GATEWAY_PRIVATE_KEY.txt
```

`API_GATEWAY_PRIVATE_KEY.txt` is the `API_GATEWAY_PRIVATE_KEY`

`JWT_PUBLIC_KEY` is generated from:-

```bash
keytool -list -rfc --keystore elite2api.jks | openssl x509 -inform pem -pubkey -noout | base64
```

`elite2api.jks` is the pub/private key pair that elite2-api holds.


### To build:

```bash
./gradlew build
```

### To Run:
```bash
docker-compose up
```

#### Running against local postgres docker:
Run the postgres docker image:
```bash
docker run --name=keyworker-postgres -e POSTGRES_PASSWORD=password -p5432:5432 -d postgres
```
Run spring boot with the the postgres spring profile


