package com.graphics;

import org.lwjgl.glfw.GLFW;

public class InputManager {
    // Referencia a la ventana de GLFW, necesaria para poder consultar el hardware del teclado.
    private long window;
    // Variables que guardan el estado anterior de las teclas para detectar la presión inicial (flanco).
    private boolean prevSpace;
    private boolean prevW;
    private boolean prevR;

    // Constructor que aplica inyección de dependencias: recibe la ventana creada en la clase principal.
    public InputManager(long window) {
        this.window = window;
    }

    // Actúa como el Controlador del patrón MVC: lee el teclado y modifica el Modelo (Game).
    public void procesarInput(Game game) {
        // Verifica si la tecla ESC está presionada y marca la ventana con la señal de cierre seguro.
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // Controles J1
        // Lee directamente del hardware si la tecla ESPACIO está presionada en este frame exacto.
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;

        // Detección de flanco de subida: solo entra si la tecla se acaba de presionar (no si se mantiene apretada).
        if (spaceAhora && !prevSpace) {
            // Lógica de reinicio rápido: si estábamos en Game Over, resetea el juego e inicia saltando.
            if (game.gameOver) {
                game.reset();
                game.started = true;
                game.jugador1.saltar();
                //game.jugador2.saltar();
            } else {
                // Flujo normal: activa la gravedad general (started = true) y aplica el impulso al J1.
                game.started = true;
                game.jugador1.saltar();
            }
        }
        // Actualiza la memoria de la tecla guardando el estado actual para la evaluación del próximo frame.
        prevSpace = spaceAhora;

        // Controles J2
        // Consulta el hardware para verificar si la tecla 'W' del Jugador 2 está presionada en este instante.
        boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        // Aplica la detección de flanco de subida para evitar múltiples saltos si se mantiene la tecla presionada.
        if (wAhora && !prevW) {
            // Verifica que la partida no haya terminado para bloquear inputs durante la pantalla de Game Over.
            if (!game.gameOver) {
                // Inicia el motor de físicas globales y aplica la fuerza de salto exclusivamente al Jugador 2.
                game.started = true;
                game.jugador2.saltar();
            }
        }
        // Almacena el estado actual de la tecla 'W' para poder evaluar el cambio (flanco) en el próximo frame.
        prevW = wAhora;

        // Reinicio manual
        // Verifica la pulsación de la tecla 'R', destinada explícitamente para reiniciar el juego tras perder.
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        // Exige un flanco de subida válido y que el estado global sea estrictamente Game Over para proceder.
        if (rAhora && !prevR && game.gameOver) {
            // Invoca el método del Modelo que devuelve las variables de dificultad y jugadores a su estado original.
            game.reset();
        }
        // Actualiza la memoria de la tecla 'R', finalizando el ciclo de procesamiento de entrada de este frame.
        prevR = rAhora;
    }
}