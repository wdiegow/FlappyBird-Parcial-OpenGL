package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * CLASE GAME (EL MODELO)
 * Actúa como el cerebro matemático del patrón MVC. Aquí no se dibuja nada de OpenGL,
 * solo reside la lógica pura: físicas, colisiones, puntuación y dificultad progresiva.
 */
public class Game {
    
    // --- CONSTANTES FÍSICAS Y DE ENTORNO ---
    // Definen las reglas inmutables del mundo: la gravedad, los límites aerodinámicos,
    // y la configuración estructural de las tuberías (velocidad, tiempo y altura).
    private static final float GRAVEDAD = -1.9f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;
    private static final float VEL_TUBERIAS_BASE = 0.62f;
    private static final float TIEMPO_TUBERIAS_BASE = 1.5f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;

    // --- ESTADO DINÁMICO DEL JUEGO ---
    // Variables que cambian frame a frame. Son públicas para que el Renderer (Vista)
    // sepa qué dibujar y el InputManager (Controlador) sepa cuándo inyectar acciones.
    public Bird jugador1;
    public Bird jugador2;
    public final List<Pipe> tuberias = new ArrayList<>();
    
    public float velocidadActual;
    public float tiempoSpawnActual;
    public int nivel;
    public float timerSpawn;
    public boolean started;
    public boolean gameOver;

    private final Random random = new Random();

    /**
     * Constructor: Inicializa las entidades principales al abrir el juego.
     * Instancia a los jugadores con distintas coordenadas X para separarlos visualmente
     * y llama a reset() para preparar el Nivel 1.
     */
    public Game() {
        jugador1 = new Bird(-0.50f);
        jugador2 = new Bird(-0.35f);
        reset();
    }

    /**
     * Reinicia el estado del mundo a sus valores de fábrica.
     * Es fundamental para poder jugar una nueva partida tras el Game Over sin cerrar la ventana.
     */
    public void reset() {
        jugador1.reset();
        jugador2.reset();
        velocidadActual = VEL_TUBERIAS_BASE;
        tiempoSpawnActual = TIEMPO_TUBERIAS_BASE;
        nivel = 1;
        timerSpawn = 0.0f;
        started = false;
        gameOver = false;
        tuberias.clear();
    }

    /**
     * EL MOTOR PRINCIPAL (Game Loop Lógico).
     * Recibe 'dt' (Delta Time) para asegurar que la gravedad y velocidad se apliquen
     * de manera uniforme sin importar a cuántos FPS corra la computadora del docente.
     */
    public void actualizar(float dt) {
        if (!started || gameOver) return;

        // 1. FÍSICAS INDEPENDIENTES: Aplica gravedad y detecta colisiones de techo/suelo
        // para cada jugador de manera aislada.
        if (!jugador1.muerto) {
            jugador1.velY += GRAVEDAD * dt;
            if (jugador1.velY < VELOCIDAD_MAX_CAIDA) jugador1.velY = VELOCIDAD_MAX_CAIDA;
            jugador1.y += jugador1.velY * dt;
            if (jugador1.y + (Bird.ALTO * 0.5f) >= 1.0f || jugador1.y - (Bird.ALTO * 0.5f) <= -1.0f) {
                jugador1.muerto = true;
            }
        }

        if (!jugador2.muerto) {
            jugador2.velY += GRAVEDAD * dt;
            if (jugador2.velY < VELOCIDAD_MAX_CAIDA) jugador2.velY = VELOCIDAD_MAX_CAIDA;
            jugador2.y += jugador2.velY * dt;
            if (jugador2.y + (Bird.ALTO * 0.5f) >= 1.0f || jugador2.y - (Bird.ALTO * 0.5f) <= -1.0f) {
                jugador2.muerto = true;
            }
        }

        // 2. CONDICIÓN DE DERROTA MULTIJUGADOR: El juego únicamente se detiene si AMBOS mueren.
        if (jugador1.muerto && jugador2.muerto) {
            gameOver = true;
            return;
        }

        // 3. GENERADOR DE OBSTÁCULOS: Usa el acumulador de Delta Time para crear tuberías periódicamente.
        timerSpawn += dt;
        if (timerSpawn >= tiempoSpawnActual) {
            timerSpawn = 0.0f;
            spawnTuberia();
        }

        // 4. GESTIÓN DE TUBERÍAS: Iterador para mover, puntear, chocar y eliminar tuberías.
        Iterator<Pipe> it = tuberias.iterator();
        while (it.hasNext()) {
            Pipe t = it.next();
            t.x -= velocidadActual * dt; // Traslación horizontal

            // Evaluación de puntos y choques para el Jugador 1
            if (!jugador1.muerto) {
                if (t.x + (Pipe.ANCHO * 0.5f) < jugador1.x && !t.puntuadaJ1) {
                    t.puntuadaJ1 = true;
                    jugador1.puntaje++; 
                }
                if (colisionaConTuberia(t, jugador1)) jugador1.muerto = true;
            }

            // Evaluación de puntos y choques para el Jugador 2
            if (!jugador2.muerto) {
                if (t.x + (Pipe.ANCHO * 0.5f) < jugador2.x && !t.puntuadaJ2) {
                    t.puntuadaJ2 = true;
                    jugador2.puntaje++; 
                }
                if (colisionaConTuberia(t, jugador2)) jugador2.muerto = true;
            }

            // 5. CURVA DE DIFICULTAD DINÁMICA: Calcula el nivel basándose en el jugador que va ganando.
            // Aumenta la velocidad y reduce el tiempo de spawn, aplicando topes máximos de seguridad.
            int maxPuntaje = Math.max(jugador1.puntaje, jugador2.puntaje);
            int nuevoNivel = (maxPuntaje / 3) + 1;
            if (nuevoNivel > nivel) {
                nivel = nuevoNivel;
                velocidadActual = VEL_TUBERIAS_BASE + ((nivel - 1) * 0.15f);
                tiempoSpawnActual = TIEMPO_TUBERIAS_BASE - ((nivel - 1) * 0.12f);
                if (tiempoSpawnActual < 0.6f) tiempoSpawnActual = 0.6f;
                if (velocidadActual > 1.8f) velocidadActual = 1.8f;
            }

            // Limpieza de memoria: Se destruye la tubería una vez sale de la pantalla por la izquierda.
            if (t.x + (Pipe.ANCHO * 0.5f) < -1.3f) {
                it.remove();
            }
        }
    }

    /**
     * Fábrica de tuberías: Define una coordenada Y aleatoria para el centro del hueco
     * e inserta la nueva entidad en la lista activa.
     */
    private void spawnTuberia() {
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Pipe(1.2f, gapCentro));
    }

    /**
     * Algoritmo de detección de colisiones AABB (Axis-Aligned Bounding Box).
     * Compara los bordes rectangulares del pájaro especificado contra los de la tubería.
     */
    private boolean colisionaConTuberia(Pipe t, Bird pajaro) { 
        float birdLeft = pajaro.x - (Bird.ANCHO * 0.5f);
        float birdRight = pajaro.x + (Bird.ANCHO * 0.5f);
        float birdBottom = pajaro.y - (Bird.ALTO * 0.5f);
        float birdTop = pajaro.y + (Bird.ALTO * 0.5f);

        float pipeLeft = t.x - (Pipe.ANCHO * 0.5f);
        float pipeRight = t.x + (Pipe.ANCHO * 0.5f);
        
        // Verificación Eje X: Si no están solapados horizontalmente, es imposible que choquen.
        if (!(birdRight > pipeLeft && birdLeft < pipeRight)) return false; 

        // Verificación Eje Y: Evalúa si el pájaro golpeó el marco superior o inferior de la tubería.
        float gapTop = t.gapCentroY + (Pipe.GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (Pipe.GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }
}