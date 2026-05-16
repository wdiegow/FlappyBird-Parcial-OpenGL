<div align="center">
  <h1>🐦 Flappy Bird - OpenGL (LWJGL)</h1>
  <p><b>Proyecto Práctico - Primer Examen Parcial de Programación Gráfica</b></p>
</div>

---

## 👤 Autor y Desarrollador

| Rol | Nombre |
| :--- | :--- |
| **Estudiante** | **Diego Andres Astete Paz** |

---

## 🎮 Controles del Juego

El juego soporta un modo de dos jugadores simultáneos e independientes:

* **Jugador 1 (Pájaro Amarillo):** Presionar la tecla `SPACE` para iniciar y saltar.
* **Jugador 2 (Pájaro Rojo):** Presionar la tecla `W` para iniciar y saltar.
* **Reinicio:** Presionar la tecla `R` (Solo en *Game Over*).
* **Salir:** Presionar la tecla `ESC`.

> **Nota:** La partida termina únicamente cuando **ambos** jugadores han chocado. Mientras uno siga con vida, el juego continúa.

---

## 🚀 Compilación y Ejecución

El proyecto está estructurado utilizando **Maven**. Para compilar y ejecutar el juego sin depender de un IDE específico, abre una terminal (CMD, PowerShell o Bash) en la raíz del proyecto (donde se ubica el archivo `pom.xml`) y ejecuta el siguiente comando:

```bash
mvn clean compile exec:java "-Dexec.mainClass=com.graphics.AppFlappyBird"