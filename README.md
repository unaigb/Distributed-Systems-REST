<div align="center">

# 🌐 Sistemas Distribuidos con REST

**Un chat y una multidifusión con ordenación total por acuerdo, ambos sobre servicios web REST en Java**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)]()
[![Jersey](https://img.shields.io/badge/Jersey-JAX--RS_2.1-1F7A8C?style=flat)]()
[![Tomcat](https://img.shields.io/badge/Tomcat-Servlet_4.0-F8DC75?style=flat&logo=apachetomcat&logoColor=black)]()
[![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=flat&logo=eclipseide&logoColor=white)]()

[Descripción](#-sobre-el-proyecto) · [ChatConRest](#-chatconrest) · [IS-ISConRest](#-is-isconrest) · [Cómo ejecutarlo](#-cómo-ejecutarlo) · [Estructura](#-estructura-del-repositorio) · [Stack](#-stack-tecnológico)

</div>

---

## 📖 Sobre el proyecto

Dos prácticas de *Sistemas Distribuidos* en las que **varios equipos hablan entre sí a través de servicios REST**: la primera, un chat sencillo; la segunda, un algoritmo clásico de sincronización de relojes y orden de mensajes. Los servidores se despliegan como servicios web (JAX-RS con **Jersey**) y los clientes, hechos en Java, los consumen desde consola (y, en el chat, con una pequeña ventana).

> 📌 **Proyecto académico** de la asignatura de *Sistemas Distribuidos* (Universidad de Salamanca).

---

## 💬 ChatConRest

Un chat en el que cada máquina publica sus mensajes en el servidor de un grupo y los demás los recuperan.

**API del servidor** (base: `/rest/Servidor`):

| Método | Ruta | Qué hace |
| :--- | :--- | :--- |
| `GET` | `reiniciarChat` | Vacía el chat. |
| `POST` | `enviarChat` | Añade un mensaje (texto plano). |
| `GET` | `pedirChat` | Devuelve el chat completo. |
| `POST` | `modificarChat` | Modifica un mensaje ya enviado. |

El **cliente** pide la IP del servidor y un *nick*, muestra el chat en una **ventana Swing** y ofrece por consola un menú para **enviar**, **modificar** un mensaje o **salir**.

---

## ⏱️ IS-ISConRest

Implementa una **multidifusión con ordenación total por acuerdo** (estilo ISIS): todos los procesos deben entregar los mensajes **en el mismo orden**, aunque cada uno los reciba en momentos distintos.

- Cada `Proceso` (un hilo) mantiene un **reloj lógico de Lamport** (`LC1` al enviar, `LC2` al recibir).
- Al recibir un mensaje, cada proceso **propone un orden** (su reloj más su identificador, para desempatar) y se **acuerda** el orden definitivo a partir de las propuestas.
- El servicio expone, entre otros, `Arrancar` (crea los procesos de la máquina, con un parámetro para activar o no la ordenación y la lista de IP de las demás máquinas), `enviarMensaje` y `enviarPropuesta`.
- El **cliente** permite **ejecutar el algoritmo** en varias máquinas y **comprobar los registros** para verificar que todos entregaron en el mismo orden.

---

## 🚀 Cómo ejecutarlo

**Requisitos:** JDK 8 o superior, Eclipse con las herramientas web (*Dynamic Web Project*) y un contenedor de servlets compatible con **Servlet 4.0** (por ejemplo, **Apache Tomcat 9**). Las librerías no se incluyen; hay que añadirlas a `WebContent/WEB-INF/lib/`.

| Librería | Uso |
| :--- | :--- |
| Jersey 2.x (`jersey-server`, `jersey-client`, `jersey-container-servlet`, `jersey-hk2`, `jersey-media-json-binding`...) | Servicios REST y cliente |
| `jakarta.ws.rs-api` 2.1.x, `hk2-*`, `jakarta.inject`, `javassist` | Dependencias de Jersey |
| `yasson` y `jakarta.json*` | Serialización JSON |
| `jakarta.servlet-api` 4.0.x | API de servlets (la aporta Tomcat) |

1. Importa `ChatConRest` o `IS-ISConRest` en Eclipse como proyecto web y añade las librerías.
2. Despliega en Tomcat; `web.xml` mapea los servicios en `/rest/*`.
3. Ejecuta `client.Cliente` desde cada máquina cliente e introduce la IP del servidor.

---

## 📂 Estructura del repositorio

```
Distributed-Systems-REST/
├── ChatConRest/
│   ├── src/server/Servidor.java      # API REST del chat
│   ├── src/client/Cliente.java       # Cliente (consola + ventana Swing)
│   └── WebContent/WEB-INF/web.xml    # Mapeo de Jersey en /rest/*
└── IS-ISConRest/
    ├── src/services/
    │   ├── Servidor.java             # API REST de la multidifusión
    │   ├── Proceso.java              # Proceso con reloj lógico de Lamport
    │   └── Mensaje.java
    ├── src/client/Cliente.java
    └── WebContent/WEB-INF/web.xml
```

---

## 🖥️ Stack tecnológico

| Categoría | Herramientas |
| :--- | :--- |
| **Lenguaje** | Java |
| **Servicios web** | JAX-RS · Jersey · JSON-B (Yasson) |
| **Despliegue** | Tomcat · Servlet 4.0 · Eclipse Dynamic Web Project |
| **Conceptos** | Relojes lógicos de Lamport · multidifusión · ordenación total por acuerdo |

---

## 🎓 Qué aprendí

- A **exponer y consumir servicios REST** con Jersey.
- Que **el tiempo en un sistema distribuido no es único**: sin un orden acordado, cada máquina ve una historia distinta.
- A depurar un algoritmo distribuido con **registros** de cada proceso.

---

## 👤 Autoría

**Unai García** — Grado en Ingeniería Informática (Universidad de Salamanca)

- 🐙 GitHub: [@unaigb](https://github.com/unaigb)

---

<div align="center">

**⭐ Si este repositorio te resulta útil, considera darle una estrella.**

</div>
