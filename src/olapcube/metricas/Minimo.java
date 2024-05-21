package olapcube.metricas;

import java.util.List;

/**
 * Clase que representa una medida de suma
 */
public class Minimo extends Medida {

    public Minimo() {
        super("Minimo");
    }

    @Override
    public double calcular(List<Double> valores) {
        double min = Double.POSITIVE_INFINITY;
        for (Double valor : valores) {
            if (valor < min){
                min = valor;
            }
        }
        
        return min;
    }
}