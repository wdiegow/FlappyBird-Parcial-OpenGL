package com.graphics;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

/**
 * CLASE PRINCIPAL (EL ORQUESTADOR)
 * Contiene el punto de entrada (main) de la aplicación.
 * Su única responsabilidad es inicializar la ventana mediante GLFW, preparar el contexto 
 * de OpenGL y coordinar la comunicación entre los tres pilares de la arquitectura (MVC).
 */
public class AppFlappyBird {
    
    // --- CONFIGURACIÓN DE LA VENTANA ---
    // Dimensiones estáticas iniciales de la ventana en píxeles.
    private static final int ANCHO = 900;
    private static final int ALTO = 700;
    
    // Identificador (Handle) de la ventana del sistema operativo administrada por GLFW.
    private long window;
    
    // --- COMPONENTES DE LA ARQUITECTURA (MVC) ---
    private Game game;                 // El Modelo: Contiene las matemáticas y reglas.
    private Renderer renderer;         // La Vista: Dibuja los gráficos usando OpenGL.
    private InputManager inputManager; // El Controlador: Escucha las teclas del jugador.

    /**
     * Flujo de vida de la aplicación.
     * Garantiza que el juego arranque, se ejecute en bucle y se limpie correctamente al salir.
     */
    public void run() {
        init();
        loop();
        cleanup();
    }

    /**
     * Fase de Inicialización.
     * 1. Arranca el subsistema de ventanas (GLFW).
     * 2. Configura el perfil de OpenGL estricto (Core Profile 3.3) exigido por el examen.
     * 3. Crea la ventana y establece el contexto gráfico.
     * 4. Ensambla y conecta las clases de nuestra arquitectura MVC.
     */
    private void init() {
        if (!GLFW.glfwInit()) throw new IllegalStateException("No se pudo iniciar GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        // Exigencia del enunciado: Perfil Core 3.3 para usar Shaders modernos sin funciones obsoletas.
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) throw new RuntimeException("No se pudo crear la ventana");

        // VSync activado (SwapInterval 1) para sincronizar los frames con el monitor y evitar "Screen Tearing".
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        // Ensamblamos las piezas de la arquitectura
        game = new Game();
        renderer = new Renderer();
        renderer.init(); // Prepara Shaders y VAO en la tarjeta de video
        inputManager = new InputManager(window);
    }

    /**
     * Actualiza dinámicamente la barra de título de la ventana.
     * Sirve como un HUD (Heads-Up Display) básico para cumplir el Requerimiento 2.3,
     * mostrando el nivel actual, los controles y los puntajes independientes de cada jugador.
     */
    private void actualizarTitulo() {
        String tituloBase = "Flappy Bird | Nivel: " + game.nivel + 
                            " | P1(Ama): " + game.jugador1.puntaje + 
                            " | P2(Roj): " + game.jugador2.puntaje +
                            " | P3(Ver): " + game.jugador3.puntaje;
        // El texto se adapta según el estado del Modelo (Game)
        if (!game.started) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | SPACE y W para empezar");
        } else if (game.gameOver) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | GAME OVER - R para reiniciar");
        } else {
            GLFW.glfwSetWindowTitle(window, tituloBase);
        }
    }

    /**
     * EL BUCLE PRINCIPAL (Game Loop).
     * Mantiene la aplicación viva e iterando a la máxima velocidad posible (limitada por VSync).
     * Calcula el Delta Time (dt) para asegurar que el juego corra a la misma velocidad en cualquier PC.
     */
    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Cálculo del tiempo transcurrido desde el último frame
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            
            // Límite de seguridad: Si la PC se congela (ej. al mover la ventana), 
            // evita que el 'dt' sea enorme y los pájaros atraviesen las paredes.
            if (dt > 0.033f) dt = 0.033f;

            // 1. Escuchar al jugador
            inputManager.procesarInput(game);
            // 2. Calcular las matemáticas y la física
            game.actualizar(dt);
            // 3. Actualizar los textos
            actualizarTitulo(); 
            // 4. Dibujar el resultado en pantalla
            renderer.render(game);

            // Intercambio de buffers (Double Buffering) y lectura de eventos del sistema (teclas/mouse)
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    /**
     * Fase de Limpieza (Destructor).
     * Se ejecuta al salir del bucle principal (cuando el usuario cierra la ventana o presiona ESC).
     * Delega la limpieza de la VRAM al Renderer y destruye la ventana de GLFW liberando la RAM.
     */
    private void cleanup() {
        renderer.cleanup(); 
        GLFW.glfwDestroyWindow(window); 
        GLFW.glfwTerminate();
    }

    /**
     * Punto de entrada estándar de Java.
     * Crea una instancia anónima de la aplicación y ejecuta su ciclo de vida.
     */
    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}