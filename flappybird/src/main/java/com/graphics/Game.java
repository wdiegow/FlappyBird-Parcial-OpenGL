package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game {
    private static final float GRAVEDAD = -1.9f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;
    private static final float VEL_TUBERIAS_BASE = 0.62f;
    private static final float TIEMPO_TUBERIAS_BASE = 1.5f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;
    
    // para q flote
    private static final int PUNTOS_PARA_FLOTAR = 3;

    public Bird jugador1;
    public Bird jugador2;
    public Bird jugador3; // aca el nuevo
    public final List<Pipe> tuberias = new ArrayList<>();
    
    public float velocidadActual;
    public float tiempoSpawnActual;
    public int nivel;
    public float timerSpawn;
    public boolean started;
    public boolean gameOver;

    private final Random random = new Random();

    public Game() {
        jugador1 = new Bird(-0.50f);
        jugador2 = new Bird(-0.35f);
        jugador3 = new Bird(-0.20f); // Nace un poco más adelante
        reset();
    }

    public void reset() {
        jugador1.reset();
        jugador2.reset();
        jugador3.reset();
        velocidadActual = VEL_TUBERIAS_BASE;
        tiempoSpawnActual = TIEMPO_TUBERIAS_BASE;
        nivel = 1;
        timerSpawn = 0.0f;
        started = false;
        gameOver = false;
        tuberias.clear();
    }

    public void actualizar(float dt) {
        if (!started || gameOver) return;

        // --- Físicas J1 ---
        if (!jugador1.muerto) {
            // Llogica p q flote
            float gravedadAplicada = (jugador1.puntaje >= PUNTOS_PARA_FLOTAR) ? -GRAVEDAD : GRAVEDAD;
            
            jugador1.velY += gravedadAplicada * dt;
            if (jugador1.velY < VELOCIDAD_MAX_CAIDA) jugador1.velY = VELOCIDAD_MAX_CAIDA;
            jugador1.y += jugador1.velY * dt;
            if (jugador1.y + (Bird.ALTO * 0.5f) >= 1.0f || jugador1.y - (Bird.ALTO * 0.5f) <= -1.0f) {
                jugador1.muerto = true;
            }
        }

        // --- Físicas J2 ---
        if (!jugador2.muerto) {
            float gravedadAplicada = (jugador2.puntaje >= PUNTOS_PARA_FLOTAR) ? -GRAVEDAD : GRAVEDAD;
            
            jugador2.velY += gravedadAplicada * dt;
            if (jugador2.velY < VELOCIDAD_MAX_CAIDA) jugador2.velY = VELOCIDAD_MAX_CAIDA;
            jugador2.y += jugador2.velY * dt;
            if (jugador2.y + (Bird.ALTO * 0.5f) >= 1.0f || jugador2.y - (Bird.ALTO * 0.5f) <= -1.0f) {
                jugador2.muerto = true;
            }
        }

        // --- Físicas J3 ---
        if (!jugador3.muerto) {
            float gravedadAplicada = (jugador3.puntaje >= PUNTOS_PARA_FLOTAR) ? -GRAVEDAD : GRAVEDAD;
            
            jugador3.velY += gravedadAplicada * dt;
            if (jugador3.velY < VELOCIDAD_MAX_CAIDA) jugador3.velY = VELOCIDAD_MAX_CAIDA;
            jugador3.y += jugador3.velY * dt;
            if (jugador3.y + (Bird.ALTO * 0.5f) >= 1.0f || jugador3.y - (Bird.ALTO * 0.5f) <= -1.0f) {
                jugador3.muerto = true;
            }
        }

        // El juego termina solo si los 3 mueren
        if (jugador1.muerto && jugador2.muerto && jugador3.muerto) {
            gameOver = true;
            return;
        }

        timerSpawn += dt;
        if (timerSpawn >= tiempoSpawnActual) {
            timerSpawn = 0.0f;
            spawnTuberia();
        }

        Iterator<Pipe> it = tuberias.iterator();
        while (it.hasNext()) {
            Pipe t = it.next();
            t.x -= velocidadActual * dt;

            // Evaluaciones J1
            if (!jugador1.muerto) {
                if (t.x + (Pipe.ANCHO * 0.5f) < jugador1.x && !t.puntuadaJ1) {
                    t.puntuadaJ1 = true;
                    jugador1.puntaje++; 
                }
                if (colisionaConTuberia(t, jugador1)) jugador1.muerto = true;
            }

            // Evaluaciones J2
            if (!jugador2.muerto) {
                if (t.x + (Pipe.ANCHO * 0.5f) < jugador2.x && !t.puntuadaJ2) {
                    t.puntuadaJ2 = true;
                    jugador2.puntaje++; 
                }
                if (colisionaConTuberia(t, jugador2)) jugador2.muerto = true;
            }

            // Evaluaciones J3
            if (!jugador3.muerto) {
                // Para simplificar y no cambiar la clase Pipe, podemos usar un truco:
                // Si la tubería ya fue cruzada por el X de J3, sumamos punto.
                // Como no creamos 'puntuadaJ3' en Pipe, podemos usar la distancia.
                // (Si quieres, puedes añadir puntuadaJ3 en Pipe.java, pero esto evita tocar otro archivo)
                // Usaremos la lógica estándar y requerimos actualizar Pipe.java por seguridad.
            }
            
            // Evaluaciones J3 (Lógica limpia que requiere actualizar Pipe.java)
            if (!jugador3.muerto) {
                if (t.x + (Pipe.ANCHO * 0.5f) < jugador3.x && !t.puntuadaJ3) {
                    t.puntuadaJ3 = true;
                    jugador3.puntaje++; 
                }
                if (colisionaConTuberia(t, jugador3)) jugador3.muerto = true;
            }

            // Dificultad basada en el puntaje más alto de los tres
            int max12 = Math.max(jugador1.puntaje, jugador2.puntaje);
            int maxPuntaje = Math.max(max12, jugador3.puntaje);
            int nuevoNivel = (maxPuntaje / 3) + 1;
            
            if (nuevoNivel > nivel) {
                nivel = nuevoNivel;
                velocidadActual = VEL_TUBERIAS_BASE + ((nivel - 1) * 0.15f);
                tiempoSpawnActual = TIEMPO_TUBERIAS_BASE - ((nivel - 1) * 0.12f);
                if (tiempoSpawnActual < 0.6f) tiempoSpawnActual = 0.6f;
                if (velocidadActual > 1.8f) velocidadActual = 1.8f;
            }

            if (t.x + (Pipe.ANCHO * 0.5f) < -1.3f) {
                it.remove();
            }
        }
    }

    private void spawnTuberia() {
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Pipe(1.2f, gapCentro));
    }

    private boolean colisionaConTuberia(Pipe t, Bird pajaro) { 
        float birdLeft = pajaro.x - (Bird.ANCHO * 0.5f);
        float birdRight = pajaro.x + (Bird.ANCHO * 0.5f);
        float birdBottom = pajaro.y - (Bird.ALTO * 0.5f);
        float birdTop = pajaro.y + (Bird.ALTO * 0.5f);

        float pipeLeft = t.x - (Pipe.ANCHO * 0.5f);
        float pipeRight = t.x + (Pipe.ANCHO * 0.5f);
        if (!(birdRight > pipeLeft && birdLeft < pipeRight)) return false; 

        float gapTop = t.gapCentroY + (Pipe.GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (Pipe.GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }
}