# Prueba Técnica

Este repositorio contiene la solución completa al reto técnico, implementando una arquitectura de microservicios (Tarjetas y Transacciones) desplegada en AWS.

## 1. Listado de Requerimientos Obtenidos

La solución cumple con los siguientes requerimientos funcionales y no funcionales:

✅ Requerimientos Funcionales (Lógica de Negocio)

RF-01: Generar un número de tarjeta único de 16 dígitos, asociado a un ID de producto de 6 dígitos.

RF-02: Activar una tarjeta (Enroll) para poder usarla.

RF-03: Recargar saldo (Balance) a una tarjeta.

RF-04: Consultar el saldo (Balance) de una tarjeta.

RF-05: Realizar una transacción de compra.

RF-06: La compra debe validar que la tarjeta exista, esté activa y tenga saldo suficiente.

RF-07: Consultar el detalle de una transacción por su ID.

RF-08: Anular una transacción.

RF-09: La anulación solo se permite si la transacción tiene menos de 24 horas de antigüedad.

RF-10: La anulación debe reversar el monto (devolver el saldo) a la tarjeta.

✅ Requerimientos No Funcionales (Arquitectura y Calidad)
RNF-01: La solución debe estar implementada en una arquitectura de Microservicios (Servicio de Tarjetas y Servicio de Transacciones).

RNF-02: El código debe incluir Pruebas Unitarias para la lógica de negocio (WIP: Cobertura actual ~20%).

RNF-03: La solución debe estar desplegada en la Nube (AWS).

RNF-04: La solución debe seguir buenas prácticas (SOLID, Manejo de Excepciones, etc.).

RNF-05: Se debe proveer un "Manual de Integración" (Swagger) y una colección de Postman.

RNF-06: El proyecto debe estar configurado para ejecutarse tanto en un entorno de Nube (perfil cloud) como Local (perfil local con H2).

## 2. Flujo de Datos (Arquitectura)

La solución sigue una arquitectura de microservicios desplegada en AWS. El flujo es el siguiente:

El cliente (Postman/Swagger) envía peticiones a las URLs públicas de Elastic Beanstalk.

Un servidor Nginx (configurado por Elastic Beanstalk) actúa como proxy inverso y dirige la petición al servicio correspondiente.

El card-service y el transaction-service (contenedores Docker corriendo en EC2) ejecutan la lógica de negocio.

Si el transaction-service necesita validar un saldo (ej. en /purchase), realiza una llamada REST interna (de servidor a servidor) al card-service.

Ambos servicios se conectan a la misma instancia de AWS RDS (PostgreSQL) para persistir los datos.

Las imágenes de Docker están alojadas en AWS ECR (Elastic Container Registry).

## 3. Modelo de Base de Datos

Aunque ambos servicios usan la misma instancia RDS, cada uno gestiona su propia tabla, operando de forma desacoplada.

Tabla cards: (Gestionada por card-service) Almacena el estado de la tarjeta, su saldo, si está activa y su fecha de vencimiento.

Tabla transactions: (Gestionada por transaction-service) Almacena un registro histórico de todas las compras y anulaciones.

La relación entre ellas es lógica, a través del card_id.

## 4. Manual de Integración (Swagger)

La documentación de la API es interactiva y está generada automáticamente con Swagger (SpringDoc). Puedes verla y probarla en vivo en las siguientes URLs:

API Card Service:
http://card-service-env1.eba-3ywqmghm.us-east-2.elasticbeanstalk.com/swagger-ui.html

API Transaction Service:
http://transaction-service-env.eba-3ywqmghm.us-east-2.elasticbeanstalk.com/swagger-ui.html

## 5. Instrucciones de Ejecución

Opción A: Probar en la Nube (AWS) - Recomendado
Puedes probar la aplicación en vivo usando la colección de Postman (Banco_Test_AWS.postman_collection.json) incluida en este repositorio.

Servicio de Tarjetas (card-service):

http://card-service-env1.eba-3ywqmghm.us-east-2.elasticbeanstalk.com
Servicio de Transacciones (transaction-service):

http://transaction-service-env.eba-3ywqmghm.us-east-2.elasticbeanstalk.com
Opción B: Ejecutar en Local
El proyecto está configurado con Perfiles de Spring para ejecutarse localmente sin necesidad de AWS.

Requisitos:

Java 17

Maven

Iniciar card-service:

Navega a la carpeta /card-service.

Ejecuta: mvn spring-boot:run (Correrá en el puerto 8081).

Iniciar transaction-service:

Navega a la carpeta /transaction-service.

Ejecuta: mvn spring-boot:run (Correrá en el puerto 8080).

La aplicación se ejecutará usando una base de datos H2 en memoria.
