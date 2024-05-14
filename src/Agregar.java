import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Agregar {

    public static void agregarValor(Map<String, List<Integer>> mapa, String clave, Integer valor) {
        // Verificar si la clave ya existe en el mapa
        if (mapa.containsKey(clave)) {
            // Si existe, obtener la lista asociada y agregar el nuevo valor
            List<Integer> lista = mapa.get(clave);
            lista.add(valor);
        } else {
            // Si no existe, crear una nueva lista y agregarla al mapa
            List<Integer> lista = new ArrayList<>();
            lista.add(valor);
            mapa.put(clave, lista);
        }
    }
}
