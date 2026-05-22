package com.graphics;

import org.lwjgl.glfw.GLFW;

public class InputManager {
    private long window;
    private boolean prevSpace;
    private boolean prevW;
    private boolean prevE; // para el 3er jugador
    private boolean prevR;

    public InputManager(long window) {
        this.window = window;
    }

    public void procesarInput(Game game) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // Controles J1 (SPACE)
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {
            if (game.gameOver) {
                game.reset();
                game.started = true;
                game.jugador1.saltar();
            } else {
                game.started = true;
                game.jugador1.saltar();
            }
        }
        prevSpace = spaceAhora;

        // Controles J2 (W)
        boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        if (wAhora && !prevW && !game.gameOver) {
            game.started = true;
            game.jugador2.saltar();
        }
        prevW = wAhora;

        // Controles J3 (E) - ¡Nuevo Jugador!
        boolean eAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS;
        if (eAhora && !prevE && !game.gameOver) {
            game.started = true;
            game.jugador3.saltar();
        }
        prevE = eAhora;

        // Reinicio manual (R)
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR && game.gameOver) {
            game.reset();
        }
        prevR = rAhora;
    }
}