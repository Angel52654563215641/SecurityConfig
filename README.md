# HU-SEC-001 - Backend Authentication & TLS/SSL (Spring Boot 3 + Spring Security + JWT)

## Requisitos
- JDK 17+ y Maven 3.9+ (o usar la extension "Extension Pack for Java" de VS Code)

## Ejecutar
```bash
# (solo si no existe src/main/resources/keystore/keystore.p12)
./scripts/generate-keystore.sh        # Windows: scripts\generate-keystore.bat

mvn spring-boot:run
```
- HTTPS: https://localhost:8443
- HTTP (solo redirige a HTTPS): http://localhost:8080

Usuarios de prueba: `user / user123` (ROLE_USER) y `admin / admin123` (ROLE_ADMIN).

## Pruebas con curl (`-k` porque el certificado es autofirmado)
```bash
# 1. Handshake TLS + endpoint publico
curl -vk https://localhost:8443/api/public/ping

# 2. Redireccion HTTP -> HTTPS (espera 302)
curl -i http://localhost:8080/api/public/ping

# 3. Forzar versiones TLS
curl -vk --tlsv1.2 --tls-max 1.2 https://localhost:8443/api/public/ping   # OK
curl -vk --tlsv1.3 https://localhost:8443/api/public/ping                  # OK
curl -vk --tls-max 1.1 https://localhost:8443/api/public/ping              # FALLA (handshake rechazado)

# 4. Sin token -> 401
curl -ik https://localhost:8443/api/user/profile

# 5. Login y token
TOKEN=$(curl -sk -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# 6. Con token -> 200
curl -ik https://localhost:8443/api/user/profile -H "Authorization: Bearer $TOKEN"

# 7. Rol insuficiente -> 403
curl -ik https://localhost:8443/api/admin/dashboard -H "Authorization: Bearer $TOKEN"

# 8. Credenciales malas -> 401 personalizado
curl -ik -X POST https://localhost:8443/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"user","password":"mala"}'
```
En Postman: desactivar *Settings > SSL certificate verification* para el certificado autofirmado.

## Errores de handshake TLS
Un handshake fallido ocurre antes de HTTP, por lo que no se puede responder un JSON al cliente.
Se registra en el servidor (logger `org.apache.tomcat.util.net` en DEBUG). Si el cliente habla HTTP en el puerto
HTTPS, Tomcat responde 400. Los eventos de autenticacion/autorizacion se registran en el logger `SECURITY_AUDIT`.

## Produccion
- Usar certificado emitido por una CA (p. ej. Let's Encrypt) y NO subir el keystore al repositorio.
- Definir `SSL_KEYSTORE_PASSWORD` y `JWT_SECRET` (Base64, >= 256 bits) como variables de entorno.
- Reemplazar los usuarios en memoria por BD.
