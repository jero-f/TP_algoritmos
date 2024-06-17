package olapcube.estructura;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import olapcube.configuration.ConfigDimension;

/**
 * Clase que representa una dimension de un cubo OLAP.
 */
public class Dimension {
    private String nombre;                              // Nombre de la dimension
    private Map<String, Set<Integer>> valoresToCeldas;  // Mapeo de valores de la dimensión a celdas en el cubo
    private Map<Integer, String> idToValores;           // Mapeo de ids (pk) de la dimensión a valores
    private int columnaFkHechos;                        // Columna que contiene la clave foránea en la tabla de los hechos
    
    /**
     * Constructor de la clase
     * 
     * @param nombre Nombre de la dimension
     */
    private Dimension(String nombre) {
        this.nombre = nombre;
        valoresToCeldas = new HashMap<>();
        idToValores = new HashMap<>();
    }

    /**
     * Método constructor que permite crear una dimensión a partir de una configuración
     * 
     * @param configDimension Configuración de la dimensión
     * @return Dimension
     */
    public static Dimension crear(ConfigDimension configDimension) {
        Dimension dim = new Dimension(configDimension.getNombre());
        dim.columnaFkHechos = configDimension.getColumnaFkHechos();
        for (String[] datos : configDimension.getDatasetReader().read()) {
            int pkDimension = Integer.parseInt(datos[configDimension.getColumnaKey()]);
            String valor = "";
            int j = 0;

            for (int i : configDimension.getColumnasJerarquias()){
                valor += datos[i] + "/";
                dim.idToValores.get(j).put(pkDimension, valor);

            //TODO: CREO que con un for aca sobre los niveles de las dimensiones podemos jerarquizar bien
            // crear los diccionarios de los niveles para valoreToCeldas
            // armar bien idToValores con los nombres bien hechos
                dim.valoresToCeldas.get(j).put(valor, new HashSet<>());
                j++;
            }
        }
        return dim;
    }

    public Dimension copiar() {
        Dimension nueva = new Dimension(this.nombre);
        nueva.valoresToCeldas = new HashMap<>();
        for (String valor : this.valoresToCeldas.keySet()) {
            nueva.valoresToCeldas.put(valor, this.valoresToCeldas.get(valor));
        }
        nueva.idToValores = this.idToValores;
        nueva.columnaFkHechos = this.columnaFkHechos;
    
        return nueva;
    }    

    public void filtrar(String valor) {
        filtrar(new String[]{valor});
    }

    public void filtrar(String[] valores){
        HashMap<String, Set<Integer>> nuevosValores = new HashMap<>();
        for (String valor : valores) {
            nuevosValores.put(valor, valoresToCeldas.get(valor));
        }
            valoresToCeldas = nuevosValores;
    }


    @Override
    public String toString() {
        return "Dimension [nombre=" + nombre + "]";
    }

    public String[] getValores() {
        return valoresToCeldas.keySet().toArray(new String[0]);
    }

    public Set<Integer> getIndicesCeldas(String valor) {
        return valoresToCeldas.get(valor);
    }

    public String getNombre() {
        return nombre;
    }

    public String getValorFromId(Integer id) {
        return idToValores.get(id);
    }

    public int getColumnaFkHechos() {
        return columnaFkHechos;
    }

    /**
     * Método que permite agregar un hecho a la dimensión
     * 
     * @param idValor id (pk) de la dimensión
     * @param indiceCelda índice de la celda en el cubo
     */
    public void agregarHecho(int idValor, int indiceCelda) {
        for (int j = 0; j < valoresToCeldas.size(); j++){
            if (!idToValores.get(j).containsKey(idValor)) {
            throw new IllegalArgumentException("El id " + idValor + " del valor no existe en la dimension " + nombre + "en el nivel" + j);
           }
        }
        //TODO: iterar sobre cada nivel de valoresToCeldas haciendo lo mismo
        for (int i = 0; i < valoresToCeldas.size(); i++){
            valoresToCeldas.get(i).get(idToValores.get(i).get(idValor)).add(indiceCelda);
       }
    }

    public void rollUp() {
        if (nivelActual > 0) {
            nivelActual -= 1;
        } else {
            throw new IllegalStateException("No se puede aumentar mas el nivel de jerarquia");
        }
    }

    public void drillDown() {
        if (nivelActual < valoresToCeldas.size() - 1) {
            nivelActual += 1;
        } else {
            throw new IllegalStateException("No se puede disminuir mas el nivel de jerarquia");
        }
    }
}


