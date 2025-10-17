# LINKTIC-TEST


## Instrucciones de instalación y ejecución
### Clonar el repositorio:

    ```
    git clone https://github.com/danielvargass97/LINKTIC-TEST
    ```
### Requisitos previos
  1. Docker Desktop
  2. Postman (opcional, para probar los endpoints)

### Consutrucción y ejecución del proyecto

  Desde la raíz del proyecto, simplemente ejecuta:
    ```
    docker-compose up --build
    ```
  Los servicios quedarán disponibles en:

    1. Product Service: http://localhost:8081
    2. Inventory Service: http://localhost:8082
### Verificación de contenedores

  Para verificar que los microservicios están corriendo:
  ```
  docker ps
  ```
También puedes ver los logs en tiempo real con:
```
  docker-compose logs -f
  ```
### Probar los endpoints
1. La aplicación expone una interfaz interactiva de documentación y prueba de endpoints con **Swagger**
    ```
   products-service: http://localhost:8081/swagger-ui/index.html
   inventory-service: http://localhost:8082/swagger-ui/index.html
   ```
2. Importar la colección de Postman incluida para probar los endpoints fácilmente.

    **Ruta:** [`./Linktic Backend Dev Test.postman_collection.json`](Linktic Backend Dev Test.postman_collection.json)

    Para usarla:
   1. Abre Postman.
   2. Ve a **Importar** → selecciona el archivo `.json` o arrástralo al área de importación.
   3. Verifica las variables de entorno si es necesario (por ejemplo, `{{inventory_service_url}}`).

### Detener los servicios

Para detener y eliminar los contenedores:
```
  docker-compose down
  ```

## Descripción de la arquitectura.

La solución está basada en una arquitectura de microservicios, donde cada servicio se implementa, ejecuta y despliega de forma independiente.
El objetivo es garantizar la escalabilidad, aislamiento de fallos y facilidad de mantenimiento.

### Componentes principales

1. **Product Service (product-service)**

    - Expone endpoints para gestionar productos: creación, consulta y actualización.

    - Provee los datos de los productos al Inventory Service mediante **peticiones HTTP.**

   - Utiliza una base de datos en ```memoria H2``` para simplificar la ejecución local.

   - Expone su API en el ```puerto 8081```.

   - Variables configuradas en application.properties incluyen:

     - api.key para autenticación.

     - Configuración de la base de datos H2.

2. **Inventory Service (inventory-service)**

   - Gestiona el inventario de los productos y permite realizar compras (disminuyendo las existencias).

   - Se comunica con el Product Service a través de un cliente HTTP (implementado con RestTemplate).

   - Cada vez que se consulta o actualiza el inventario, el servicio valida la existencia del producto consultando al Product Service.

   - También utiliza una base de datos ```H2 en memoria```.

   - Expone su API en el ```puerto 8082```.

3. **Comunicación entre servicios**

   - Los servicios se comunican mediante peticiones REST en formato ```JSON:API```.

   - La comunicación incluye validación por API Key.

   - Ejemplo:
   El Inventory Service consulta el Product Service en:
    ```
    GET http://product-service:8081/api/products/{id}
    ```
4. **Contenedores Docker**

   - Cada microservicio se ejecuta dentro de su propio contenedor Docker.

   - Se utiliza Docker Compose para levantar el ecosistema completo con un solo comando.

   - Cada contenedor incluye:

     - Imagen basada en ```openjdk:17-jdk-alpine```

     - Construcción del proyecto usando Gradle

     - Exposición del puerto correspondiente

     - Dependencias aisladas entre servicios

5. **Orquestación con Docker Compose**

   - El archivo docker-compose.yml define y levanta los dos servicios:
    
     - product-service → ```puerto 8081```

     - inventory-service →``` puerto 8082```

   - El servicio de inventario depende del producto para inicializar correctamente.
   
### Decisiones técnicas y justificaciones

1. **Elección de arquitectura basada en microservicios**

   Se optó por una **arquitectura de microservicios** para aislar responsabilidades y permitir la escalabilidad independiente de cada componente.

   - Cada servicio tiene su propia base de datos y lógica de negocio.

   - La comunicación se realiza mediante **peticiones HTTP REST** en formato **JSON API**, asegurando un bajo acoplamiento.

   - Esto facilita la evolución o despliegue independiente de cada microservicio sin afectar al resto.
2. **Separación de responsabilidades**

    Cada microservicio cumple una función específica:

   - **Product Service** → Gestiona la información del producto (nombre, precio, descripción).
   - **Inventory Service** → Controla el inventario, actualiza las cantidades y gestiona las compras.

    Esta separación permite mantener una clara división de responsabilidades siguiendo el principio **SRP (Single Responsibility Principle)**.

3. **Seguridad y comunicación entre servicios**

   Para validar la comunicación entre microservicios, se emplea una **API Key**, configurada en los application.properties de cada servicio.
   Esto asegura que solo servicios autorizados puedan intercambiar información, evitando llamadas externas no controladas.

4. **Bases de datos y persistencia**

   Durante el desarrollo y pruebas se utilizó una base de datos H2 en memoria, por su facilidad de configuración y compatibilidad con entornos de integración continua.
   Esto permite ejecutar los microservicios sin necesidad de instalar bases de datos externas.
5.  **Contenedorización con Docker**

    Cada microservicio cuenta con su propio Dockerfile, basado en ```openjdk:17-jdk-alpine``` para garantizar ligereza y compatibilidad.
    Ambos se orquestan mediante Docker Compose, lo cual simplifica su despliegue y asegura la correcta comunicación entre ellos mediante nombres de servicio.
6.  **Decisión sobre el endpoint de compra**

    El endpoint de compra (```POST /api/inventory/purchase```) fue implementado dentro del Inventory Service.

    #### Justificación:
     - La compra **afecta directamente al inventario**, ya que implica reducir la cantidad disponible de un producto.

     - El Product Service solo debe encargarse de mantener la información descriptiva del producto, no de su disponibilidad o stock.

     - Centralizar la lógica de compra en el Inventory Service evita duplicar responsabilidades y reduce el acoplamiento entre servicios.

     - Además, este servicio puede:

       - Validar si el producto existe mediante una consulta al Product Service.

       - Verificar la cantidad disponible.

       - Actualizar el inventario y registrar el resultado de la transacción (por ejemplo, cantidad comprada y cantidad restante).

    #### Pruebas y validación
    - Se implementaron tests unitarios y de integración con MockMvc y Mockito.

    - Las pruebas verifican:

      - La correcta comunicación entre servicios.

      - El manejo de errores en casos de inventario insuficiente o producto inexistente.

      - El cumplimiento del formato JSON API en las respuestas.
### Explicación del flujo de compra implementado.

-  El cliente realiza una petición al Inventory Service con el ID del producto y la cantidad deseada.

- El Inventory Service consulta al Product Service para validar que el producto exista.

- Si el producto existe y hay cantidad suficiente:

  - Actualiza el inventario.

  - Devuelve una respuesta con los detalles de la compra.

- Si no hay inventario suficiente o el producto no existe:

  - Retorna un error claro con el estado HTTP correspondiente (404 o 400).

### Documentación sobre el uso de herramientas de IA en el desarrollo

Durante el desarrollo de este proyecto se utilizó **ChatGPT (modelo GPT-5 de OpenAI)** como herramienta de apoyo técnico y de documentación.
El uso de la IA se limitó a tareas de **asistencia y optimización**, manteniendo siempre la **verificación humana del código final.**

#### Herramientas utilizadas

- **ChatGPT (OpenAI, modelo GPT-5)**
    Utilizado para acelerar el desarrollo y documentación del proyecto.

#### Tareas en las que se empleó IA

1. **Diseño y estructuración de microservicios:**

    Se consultó a la IA para definir la arquitectura basada en dos servicios independientes (Product Service e Inventory Service) siguiendo principios de separación de responsabilidades y comunicación vía REST.

2. **Generación de controladores y servicios base:**

    Se solicitó a la IA ejemplos de controladores, servicios y DTOs siguiendo las convenciones de Spring Boot y Gradle.
El código fue revisado manualmente, adaptado a las reglas de negocio y probado localmente.

3. **Manejo de errores y validaciones:**

    Se obtuvo apoyo para implementar respuestas JSON-API y manejo uniforme de errores HTTP (404, 400, 500).
Todas las excepciones y flujos se probaron manualmente con Postman y test unitarios.

4. **Creación de pruebas unitarias (JUnit + Mockito):**

    Se generaron plantillas iniciales de pruebas, que luego fueron ajustadas según la lógica del proyecto y los casos reales de uso.

5. **Documentación y README del proyecto:**

    Se usó IA para redactar secciones descriptivas del sistema, instrucciones de despliegue en Docker y justificación técnica de las decisiones arquitectónicas.

#### Verificación de la calidad del código generado

- Cada fragmento de código generado por la IA fue **inspeccionado manualmente** y ajustado para cumplir con buenas prácticas (SOLID, Clean Code).

- Se realizaron **pruebas locales con Postman** para verificar la correcta integración entre microservicios.

- Se ejecutaron **pruebas unitarias** para validar los comportamientos esperados y evitar regresiones.

- Se revisaron los contenedores Docker y el ```docker-compose.yml``` para garantizar la portabilidad y correcta comunicación entre servicios.