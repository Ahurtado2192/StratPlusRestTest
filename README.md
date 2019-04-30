# StratPlusRestTest


Url Servicios Rest 



Descripcion: Registra un nuevo usuario 

Tipo de operacion:Post

Ejemplo de Solicitud:

{
  "userId": "ALEXIS",
  "password": "Alexis1234",
  "confirmPassword": "Alexis1234"
}


http://localhost:8080/camel-rest-sql/register/newUser




Descripcion: Busca un usuario para Logeo

Tipo de operacion:Post

Ejemplo de Solicitud:

{
  "userId": "ALEXIS",
  "password": "Alexis1234",
}

http://localhost:8080/camel-rest-sql/login/findUser


Para Correr el proyecto 

mvn clean spring-boot:run


Propiedades del Proyecto 

backdemo/src/main/resources/application-dev.yml
