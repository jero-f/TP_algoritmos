package olapcube.metricas;

import java.util.List;

/**
 * Clase que representa una medida de suma
 */
public class Maximo extends Medida {

    public Maximo() {
        super("Maximo");
    }

    @Override
    public double calcular(List<Double> valores) {
        double max = Double.NEGATIVE_INFINITY;
        for (Double valor : valores) {
            if (valor > max){
                max = valor;
            }
        }
        
        return max;
    }
}