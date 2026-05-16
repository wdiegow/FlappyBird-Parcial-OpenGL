package com.graphics;

public class Pipe {
    // Posición horizontal de la tubería, Va disminuyendo en cada frame para simular que el pájaro avanza.
    public float x; 
    
    // Coordenada vertical donde se ubica el centro d gap, Se genera aleatoriamente al instanciar la tubería.
    public float gapCentroY;
    
    // Bandera que indica si el Jugador 1 ya cruzó esta tubería.
    public boolean puntuadaJ1;
    
    // Bandera para el Jugador 2. ya que ambos pueden cruzar en tiempos distintos.
    public boolean puntuadaJ2;

    // Constante para el grosor de las tuberías, Se usa para dibujar los rectángulos y calcular la caja de colisiones.
    public static final float ANCHO = 0.18f;
    
    // Constante global que define la altura del hueco entre la tubería de arriba y la de abajo.
    public static final float GAP_ALTO = 0.48f;

    // Se llama desde el Game.java (spawnTuberia) para una nueva tubería en el borde derecho de la pantalla.
    public Pipe(float x, float gapCentroY) {
        this.x = x; // Asigna la posición inicial (generalmente 1.2f, fuera de pantalla)
        this.gapCentroY = gapCentroY; // Asigna la altura aleatoria del hueco
        
        // Al nacer la tubería, ningún jugador la ha cruzado todavía, por lo que inician en false.
        this.puntuadaJ1 = false;
        this.puntuadaJ2 = false;
    }
}