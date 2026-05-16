package com.graphics;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * CLASE RENDERER (LA VISTA)
 * Encargada de toda la comunicación con OpenGL. Recibe el estado del juego (Modelo)
 * y lo traduce a instrucciones gráficas (dibujar rectángulos).
 * Implementa un Pipeline Gráfico basado en Core Profile 3.3.
 */
public class Renderer {
    
    // --- VARIABLES DE OPENGL ---
    // Identificadores (Handles) que nos da la tarjeta gráfica para referenciar 
    // nuestros programas y memorias almacenadas en la VRAM.
    private int programa; // El Shader Program compilado.
    private int vao;      // Vertex Array Object: Describe cómo leer los vértices.
    private int vbo;      // Vertex Buffer Object: Almacena los datos de los vértices.
    
    // --- BUZONES DEL SHADER (UNIFORMS) ---
    // Direcciones de memoria para enviarle datos dinámicos al Shader en cada frame.
    private int uOffsetLocation; // Para mover el rectángulo (Traslación X, Y).
    private int uScaleLocation;  // Para cambiar el tamaño del rectángulo (Ancho, Alto).
    private int uColorLocation;  // Para pintar el rectángulo (R, G, B).

    /**
     * Inicializa el Pipeline Gráfico preparando los Shaders y subiendo la 
     * geometría base (el Quad) a la memoria de video.
     */
    public void init() {
        crearShaders();
        crearQuadBase();
    }

    /**
     * Compila y enlaza los programas que se ejecutan directamente en la GPU.
     * - Vertex Shader: Transforma el Quad base utilizando la escala y el offset.
     * - Fragment Shader: Pinta los píxeles resultantes de un color sólido.
     */
    private void crearShaders() {
        String vertexSrc = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            uniform vec2 uOffset;
            uniform vec2 uScale;
            void main() {
                vec2 finalPos = aPos.xy * uScale + uOffset;
                gl_Position = vec4(finalPos, aPos.z, 1.0);
            }
            """;

        String fragmentSrc = """
            #version 330 core
            uniform vec3 uColor;
            out vec4 fragColor;
            void main() {
                fragColor = vec4(uColor, 1.0);
            }
            """;

        // Compilación del Vertex Shader
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Vertex shader error");
        }

        // Compilación del Fragment Shader
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);

        // Enlace (Linking) de ambos Shaders en un solo Programa
        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        // Búsqueda de las direcciones de los Uniforms para usarlos después
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");

        // Limpieza de recursos intermedios en la GPU
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    /**
     * Construye un Cuadrado Unitario (Quad) de 1x1 centrado en el origen (0,0).
     * Este es el ÚNICO objeto 3D que subimos a la tarjeta de video. 
     * Todos los elementos del juego se dibujan reutilizando y escalando este mismo Quad.
     */
    private void crearQuadBase() {
        // Coordenadas de los 2 triángulos (6 vértices) que forman el cuadrado
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,  0.5f, -0.5f, 0.0f,  0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,  0.5f,  0.5f, 0.0f, -0.5f,  0.5f, 0.0f
        };
        
        // Generación y configuración del VAO y VBO
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        
        // Transferencia de datos de la RAM (Java) a la VRAM (Tarjeta Gráfica)
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        
        // Le indicamos al Shader cómo leer estos vértices (3 floats por posición)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        
        // Desvinculamos los buffers por seguridad
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Método principal de dibujo. Se ejecuta en cada ciclo del Game Loop.
     * Lee el estado del Modelo (Game) y ejecuta las llamadas de dibujo (Draw Calls).
     */
    public void render(Game game) {
        // 1. Limpieza de pantalla: Pintamos todo de celeste (Cielo)
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        // 2. Activamos el Pipeline y la geometría a utilizar
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);

        // 3. Dibujamos los elementos estáticos en el fondo (Req. 2.4)
        dibujarFondo();

        // 4. Dibujamos las tuberías calculando la parte superior e inferior del hueco
        for (Pipe t : game.tuberias) {
            float gapTop = t.gapCentroY + (Pipe.GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (Pipe.GAP_ALTO * 0.5f);
            
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                dibujarRect(t.x, gapTop + (altoSuperior * 0.5f), Pipe.ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);
            }
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                dibujarRect(t.x, -1.0f + (altoInferior * 0.5f), Pipe.ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);
            }
        }

        // 5. Dibujamos a los jugadores (si siguen vivos) usando arte compuesto (Req. 2.1)
        if (!game.jugador1.muerto) dibujarPajaroCompuesto(game.jugador1, 0.98f, 0.85f, 0.20f);
        if (!game.jugador2.muerto) dibujarPajaroCompuesto(game.jugador2, 0.98f, 0.35f, 0.20f);
        
        // 6. Si ambos mueren, dibujamos la lápida en primer plano
        if (game.gameOver) dibujarGameOverArte();
    }

    /**
     * Método central de dibujo instanciado (Paramétrico).
     * En lugar de enviar nueva geometría a la tarjeta gráfica, simplemente enviamos
     * nuevas coordenadas, tamaño y color a los "buzones" (Uniforms) del Shader
     * y le ordenamos que dibuje el Quad base otra vez.
     */
    private void dibujarRect(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /**
     * Ensambla el sprite del pájaro mediante primitivas.
     * Calcula la posición relativa de cada parte (Ojo, Pico, etc.) basándose en el centro (x, y).
     * Incluye animación paramétrica del ala según la velocidad vertical del objeto.
     */
    private void dibujarPajaroCompuesto(Bird pajaro, float rBase, float gBase, float bBase) {
        float px = pajaro.x; float py = pajaro.y;
        dibujarRect(px, py, 0.08f, 0.08f, rBase, gBase, bBase); // Cuerpo
        dibujarRect(px - 0.05f, py - 0.01f, 0.03f, 0.04f, rBase * 0.7f, gBase * 0.7f, bBase * 0.7f); // Cola
        dibujarRect(px + 0.05f, py + 0.01f, 0.04f, 0.03f, 1.0f, 0.5f, 0.0f); // Pico
        dibujarRect(px + 0.02f, py + 0.02f, 0.025f, 0.025f, 1.0f, 1.0f, 1.0f); // Ojo (Fondo)
        dibujarRect(px + 0.025f, py + 0.02f, 0.01f, 0.01f, 0.0f, 0.0f, 0.0f); // Pupila
        
        // Animación dinámica del ala (Req. 2.1)
        if (pajaro.velY > 0) dibujarRect(px - 0.01f, py - 0.03f, 0.04f, 0.02f, 1.0f, 1.0f, 1.0f); // Aleteo Abajo
        else dibujarRect(px - 0.01f, py + 0.01f, 0.04f, 0.02f, 1.0f, 1.0f, 1.0f); // Aleteo Arriba
    }

    /**
     * Dibuja un paisaje estático pixel-art en el fondo usando solo primitivas.
     */
    private void dibujarFondo() {
        dibujarRect(-0.6f, 0.6f, 0.25f, 0.25f, 0.98f, 0.90f, 0.20f); 
        dibujarRect(-0.6f, 0.6f, 0.35f, 0.15f, 0.98f, 0.90f, 0.20f); 
        dibujarRect(-0.6f, 0.6f, 0.15f, 0.35f, 0.98f, 0.90f, 0.20f); 
        dibujarRect(0.5f, 0.7f, 0.3f, 0.1f, 1.0f, 1.0f, 1.0f);
        dibujarRect(0.5f, 0.75f, 0.15f, 0.08f, 1.0f, 1.0f, 1.0f);
        dibujarRect(0.0f, -0.9f, 2.0f, 0.2f, 0.45f, 0.30f, 0.15f);
        dibujarRect(0.0f, -0.78f, 2.0f, 0.04f, 0.20f, 0.80f, 0.20f);
    }

    // Dibuja una lápida pixel-art para la pantalla de Game Over
    private void dibujarGameOverArte() {
        dibujarRect(0.0f, 0.0f, 2.0f, 0.7f, 0.15f, 0.18f, 0.22f);
        dibujarRect(0.0f, 0.05f, 0.35f, 0.45f, 0.6f, 0.6f, 0.6f);
        dibujarRect(0.05f, -0.05f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f);
        dibujarRect(0.0f, 0.12f, 0.04f, 0.18f, 0.2f, 0.2f, 0.2f);
        dibujarRect(0.0f, 0.16f, 0.14f, 0.04f, 0.2f, 0.2f, 0.2f);
        dibujarRect(0.0f, -0.18f, 0.55f, 0.06f, 0.20f, 0.80f, 0.20f);
        dibujarRect(-0.2f, -0.15f, 0.04f, 0.04f, 0.15f, 0.70f, 0.15f);
        dibujarRect(0.15f, -0.15f, 0.04f, 0.04f, 0.15f, 0.70f, 0.15f);
    }

    /**
     * Limpia la memoria de la Tarjeta Gráfica (VRAM) antes de cerrar la aplicación.
     * Buena práctica para evitar fugas de memoria (Memory Leaks).
     */
    public void cleanup() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL20.glDeleteProgram(programa);
    }
}