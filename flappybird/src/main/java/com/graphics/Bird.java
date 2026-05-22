package com.graphics;

public class Bird {
    // Posición horizontal individual para que los jugadores no se superpongan en la pantalla.
    public float x; 
    // Posición vertical actual, modificada en cada frame por la gravedad del Game Loop.
    public float y;
    // Inercia vertical: valor positivo al recibir el impulso del salto y negativo en caída libre.
    public float velY;
    // Bandera de estado que desactiva los controles y colisiones cuando el jugador choca.
    public boolean muerto;
    public int puntaje;
    
    // Ancho y alto constante de la hitbox (static final porque todos los pájaros miden lo mismo).
    public static final float ANCHO = 0.12f;
    public static final float ALTO = 0.10f;

    // Fuerza física instantánea que contrarresta la gravedad al presionar la tecla de acción.
    public static final float IMPULSO_SALTO = 0.85f;

    // Constructor que recibe una coordenada X específica para separar al J1 del J2 al nacer.
    public Bird(float posicionX) {
        this.x = posicionX;
        reset();
    }

    // Restablece las variables al estado de fábrica (centro de pantalla y cero puntos).
    public void reset() {
        this.y = 0.0f;
        this.velY = 0.0f;
        this.muerto = false;
        this.puntaje = 0;
    }

    // Aplica el impulso vertical con una validación previa para evitar "saltos zombie" post-mortem.
    public void saltar() {
        if (!muerto) { 
            if (this.puntaje >= 3) {
                this.velY = -IMPULSO_SALTO;
            }else {
                this.velY = IMPULSO_SALTO;
            }
        }
    }
}