package olapcube.metricas;

import java.util.List;

/**
 * Clase que representa una medida de cuenta
 */
public class Count extends Medida {

    public Count() {
        super("Count");
    }

    @Override
    public double calcular(List<Double> valores) {
        int cant = valores.size();
        return cant;        
    }
}
