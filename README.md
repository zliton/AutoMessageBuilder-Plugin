# Plugin AutoMessageBuilder v1.0.2

## Comandos
  - /automessage list | **Muestra todos los mensajes Automaticos**
  - /automessage delete  <id> | **Borra un mensaje automatico**
  - /automessage add <identifier> <interval> <mensaje> | **Agrega un nuevo mensaje automatico**
  - /automessage | **Muestra los comandos**

## Aliases
  - /automessage | /am | /automb

## Permisos
 ```yaml
  permissions:
    automessagebuilder.automessage:
      description: "/automessage permission for AutoMessageBuilder"
      default: op
  
    automessagebuilder.automessage.list:
      description: "/automessage list permission for AutoMessageBuilder"
      default: op
  
    automessagebuilder.automessage.add:
      description: "/automessage add permission for AutoMessageBuilder"
      default: op
    
    automessagebuilder.automessage.update:
      description: "/automessage set permission for AutoMessageBuilder"
      default: op
  
    automessagebuilder.automessage.delete:
      description: "/automessage delete permission for AutoMessageBuilder"
      default: op
  
  
    automessagebuilder.*:
      description: "All permissions for AutoMessageBuilder"
      default: op
 ```

## Base de datos

El Plugin utiliza una base de datos sql para mayor seguridad y manejo de errores, el plugin utiliza bases de datos SQLite y MySQL

Para instalar una Base de datos MySQL solo necesitas cambiar unas opciones:

 ```yaml

database:

  #
  # For Databases SQLite (Default)
  #
  sqlite:
    
    enabled: false # Por defecto es true, pero ya que utilizaremos una base de datos mysql lo desactivamos

    connection:
      file: automessage.db
  

  #
  # For MySQL databases for multiple servers
  #
  mysql:

    enabled: true # Activa esta opción para poder habilitar el driver MySQL

    connection:
      username: root # Ingresa el usuario de tu base de datos 
      password: password # Establece aca la contraseña de tu base de datos
      database: db_server # Establece aca el nombre de tu base de datos
      hostname: localhost # Ingresa la IP o Hostname de tu Servidor
      port: 3306 


 ```

## Instalación
  
  Necesitas [Maven Java](https://maven.apache.org) y [Java 21](https://www.oracle.com/mx/java/technologies/downloads/) para compilar y generar un nuevo archivo jar del plugin.

  Luego ejecutar el comando:
  ```
    mvn clean 
    mvn package
  ```

  Al terminar la compilacion se generada un archivo automessagebuilder-1.0.2.jar en la carpeta target, puedes copiar el archivo y agregarlo a tu servidor : D
  


##

### Paper Api Version: 1.20
