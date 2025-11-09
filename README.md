# Prueba Técnica

Este repositorio contiene la solución completa al reto técnico, implementando una arquitectura de microservicios (Tarjetas y Transacciones) desplegada en AWS.

## 1. Arquitectura de la Solución

La solución se compone de dos microservicios independientes que se comunican vía REST, cada uno con su propia base de datos (separados en la instancia RDS), desplegados en Elastic Beanstalk.



## 2. Modelo de Base de Datos

Se utilizó un modelo relacional simple en PostgreSQL (en AWS) y H2 (para pruebas locales).


## 3. Instrucciones de Ejecución (¡Importante!)

La solución está 100% desplegada y funcional en la nube de AWS.

### Opción A: Probar en la Nube (AWS) - Recomendado

Puedes probar la aplicación en vivo usando la colección de Postman incluida.

* **Servicio de Tarjetas (`card-service`):**
    `http://card-service-env1.eba-3ywqmghm.us-east-2.elasticbeanstalk.com/`
* **Servicio de Transacciones (`transaction-service`):**
    `http://transaction-service-env.eba-3ywqmghm.us-east-2.elasticbeanstalk.com/`

**Colección de Postman:**
1.  Importa el archivo `PruebaTecnica.postman_collection.json` (incluido en este repositorio).
2.  Ejecuta los 7 pasos del "Flujo de Prueba Completo" (Crear, Activar, Recargar, Comprar, Verificar, Anular, Verificar).

### Opción B: Ejecutar en Local

El proyecto está configurado con **Perfiles de Spring** para ejecutarse localmente sin necesidad de AWS.

1.  **Requisitos:**
    * Java 17
    * Maven
2.  **Iniciar `card-service`:**
    * Navega a la carpeta `/card-service`.
    * Ejecuta: `mvn spring-boot:run` (Correrá en el puerto `8081`).
3.  **Iniciar `transaction-service`:**
    * Navega a la carpeta `/transaction-service`.
    * Ejecuta: `mvn spring-boot:run` (Correrá en el puerto `8080`).

La aplicación se ejecutará usando una base de datos **H2 en memoria**.

---

## 4. Buenas Prácticas Implementadas:

* **Arquitectura de Microservicios:** Servicios desacoplados (`card-service`, `transaction-service`).
* **Principios SOLID:** Se aplicó **Inversión de Dependencias (DIP)** usando interfaces de Servicio (`CardService`, `TransactionService`) e **Inyección de Dependencias (DI)** vía constructor.
* **Manejo de Dinero:** Se utilizó `BigDecimal` en toda la aplicación para garantizar la precisión financiera, evitando `double` o `float`.
* **Pruebas (Nivel 2):** Se crearon **Pruebas Unitarias** (`...Test.java`) usando Mockito para simular (mockear) el Repositorio y el `RestTemplate`, logrando probar la lógica de negocio de forma aislada.
* **Manejo de Errores:** Se implementó manejo de excepciones centralizado (`@ControllerAdvice`) y excepciones personalizadas (`CardException`, `TransactionException`).
* **Perfiles de Entorno:** Se usó **Spring Profiles** (`application-local.properties`, `application-cloud.properties`) para permitir que una única base de código funcione en entornos locales (H2) y de nube (PostgreSQL) sin cambios.
* **Despliegue (Nivel 3):** La solución está "dockerizada" y desplegada en **AWS** usando **RDS** (para BBDD), **ECR** (para imágenes Docker) y **Elastic Beanstalk** (para los servicios).
