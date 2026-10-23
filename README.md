# API-E-Commerce

## Authentication and CORS configuration

Set `SPRING_DATA_MONGODB_URI` in the runtime environment to the MongoDB Atlas
connection URI. Do not store database credentials in this repository.

Set `JWT_SECRET` in the runtime environment to a Base64-encoded random value
containing at least 32 bytes. The application fails at startup if the value is
missing, malformed, or too short; do not commit the secret to this repository.
Signup returns a signed JWT, which protected API requests must send as
`Authorization: Bearer <token>`.

Set `CORS_ALLOWED_ORIGINS` to a comma-separated list of full frontend origins
for deployed environments (for example, `https://shop.example.com`). The local
defaults are `http://localhost:3001` and `http://localhost:5173`.

## Run locally

With Java 21, Maven, and a local MongoDB server running on port 27017:

```sh
export SPRING_DATA_MONGODB_URI='mongodb://127.0.0.1:27017/ecommerce_local'
export JWT_SECRET="$(openssl rand -base64 32)"
mvn spring-boot:run
```

This uses a local database rather than Atlas and a temporary signing key.
Tokens issued with this temporary key stop working when the process restarts.
For persistent local sessions, set `JWT_SECRET` to a securely stored key that
you keep outside the repository. To use Atlas, set `SPRING_DATA_MONGODB_URI` to
your Atlas URI instead; do not commit credentials.