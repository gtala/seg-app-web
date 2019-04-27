# TACS-1c2017-G1
Deployado en [Openshift](http://tmdbapi-tmdb1.1d35.starter-us-east-1.openshiftapps.com/)
## Help
### Clonar Proyecto:
  En la carpeta donde se desea clonar:
  ```
  git clone https://github.com/TACS-1c2017-G1/TACS-1c2017-G1
  ```
  
### Para importarlo a Eclipse:
  Ir a la perspectiva Git y agregar el proyecto existente.
  Luego en la perspectiva Java, importar el proyecto como un proyecto Maven.
  
### Requisitos para Deployar Localmente, tener instalado:
  * Tomcat instalado y corriendo como servicio;
  
  * MongoDB instalado, corriendo como servicio, ademas es necesario modificar el archivo /src/main/java/app/MongoConfig.java para que quede así: 
    ```
    return new MongoClient("127.0.0.1", 27017);
    //return new MongoClient(new MongoClientURI("mongodb://facundo:grupo1@mongodb/tmdb-g1"));
    ```
  * NodeJS y NPM, solo necesarios si haces algun cambio en javascript y necesitas rebuildear el bundle.js;
  
  * Maven 3 instalado;
  
### Para levantarlo: 
  ```
  chmod +x deploy.sh
  ./deploy.sh
  ```

### Probarlo:
  Seguir la [documentación de la API](https://drive.google.com/open?id=1Yd18SKcWrIAl3KMnLE5CXw_hf2PsOcV18FiCCuUE2fw)

## Estructura de Archivos:

```
package.json - Archivo de proyecto para npm.
gulpfile.js - Se definen las tareas a correr con gulp.

index.html
style.css
bundle.js
templates/
../header.html
../footer.html
../home.html
../listas/
../../list.html, new.html, edit.html
../favoritos/
../../list.html, new.html, edit.html
src/
../router.js - Se definen todos los states.
../controllers/ - Se define un archivo js para cada "angular controller".
../../listasController.js
../../favoritosController.js
styles/
../mainStyle.less - Estilo principal, importa a otros estilos que queramos definir.
../otherStyle.less
```
                                                                                                                             
## Instalación de mongoDB:


### Ubuntu

* Paso 1 — Agregar el Repositorio MongoDB

MongoDB está actualmente incluido en el repositorio de paquetes de Ubuntu, pero el repositorio oficial de MongoDB proporciona la versión más actualizada y es el camino recomendado para instalar este software. En este paso, agregaremos este repositorio oficial al servidor.

Ubuntu se asegura de autenticar los paquetes de software verificando que han sido firmados con llaves GPG, así que primero importaremos la llave para el repositorio oficial de MongoDB.

```
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927
```

Después de importar la llave satisfactoriamente, verá algo como esto:

Output
```
gpg: Total number processed: 1
gpg:               imported: 1  (RSA: 1)
```

A continuación, debemos agregar los detalles del repositorio de Mongo de tal manera que apt pueda saber de donde descargar los paquetes.

Corriendo el siguiente comando crearemos la lista para MongoDB.

```
echo "deb http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
```

Después de agregar los detalles del repositorio, debemos actualizar la lista de paquetes.

```
sudo apt-get update 
```

* Paso 2 — Instalando y Verificando MongoDB

Ahora podemos instalar el propio paquete de MongoDB.

```
sudo apt-get install -y mongodb-org
```

Este comando instalará diversos paquetes incluyendo la versión estable más reciente de MongoDB seguido de herramientas administrativas para el servidor MongoDB.

Para lanzar apropiadamente MongoDB como un servicio de Ubuntu 16.04, debemos crear un archivo unitario que describa el servicio. Un archivo unitario le dice al systemd como manejar el recurso. El tipo más común de unidad es un servicio, el cual determina como iniciar o detener el servicio, cuando debería de iniciar automáticamente al arrancar, y cuando debería depender de otro software para su ejecución.

Vamos a crear un archivo de unidad para administrar el servicio de MongoDB. Crearemos un archivo de configuración llamado mongodb.service en el directorio /etc/systemd/system utilizando nano o su editor de texto favorito.
```
sudo nano /etc/systemd/system/mongodb.service
```

Pegue el siguiente contenido, después guarde y cierre el archivo.

/etc/systemd/system/mongodb.service
```
[Unit]
Description=High-performance, schema-free document-oriented database
After=network.target

[Service]
User=mongodb
ExecStart=/usr/bin/mongod --quiet --config /etc/mongod.conf

[Install]
WantedBy=multi-user.target
```

Este archivo tiene una estructura simple:

La sección Unit contiene un resumen (por ejemplo una descripción legible para el humano que describe el servicio MongoDB) así como las dependencias que deberán existir antes de que el servicio inicie. En nuestro caso, MongoDB depende de que la red esté disponible, por lo tanto agregamos network.tarket aquí.
La sección Service indica como deberá iniciar el servicio. La directiva User especifica que el servicio deberá correr bajo el usuario mongodb, y la directiva ExecStart inicia el comando para arrancar el servidor MongoDB.
La última sección, Install, le dice a systemd cuando el servicio debe iniciar automáticamente. multi-user.target es un sistema de secuencias de arranque estándar , que significa que el servicio correrá automáticamente al arrancar.
Lo siguiente, será iniciar el servicio recién creado con systemctl.

```
sudo systemctl start mongodb
```

Aún cuando este comando no responde con un mensaje, puede utilizar systemctl para revisar que el servicio ha arrancado apropiadamente.
```
sudo systemctl status mongodb
Output
● mongodb.service - High-performance, schema-free document-oriented database
   Loaded: loaded (/etc/systemd/system/mongodb.service; enabled; vendor preset: enabled)
   Active: <span class="highlight">active</span> (running) since Mon 2016-04-25 14:57:20 EDT; 1min 30s ago
 Main PID: 4093 (mongod)
    Tasks: 16 (limit: 512)
   Memory: 47.1M
      CPU: 1.224s
   CGroup: /system.slice/mongodb.service
           └─4093 /usr/bin/mongod --quiet --config /etc/mongod.conf
```
El último paso es habilitar automáticamente el arranque de MongoDB cuando el sistema inicie.

```
sudo systemctl enable mongodb
```

El servidor MongoDB ahora está configurado y corriendo, y usted puede administrar el servicio MongoDB utilizando el comando systemctl (por ejemplo: sudo systemctl mongodb stop, sudo systemctl mongodb start).


