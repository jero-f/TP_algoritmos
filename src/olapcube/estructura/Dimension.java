package olapcube.estructura;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import olapcube.configuration.ConfigDimension;

/**
 * Clase que representa una dimension de un cubo OLAP.
 */
public class Dimension {
    private String nombre;                              // Nombre de la dimension
    private List<Map<String, Set<Integer>>> valoresToCeldas;  // Mapeo de valores de la dimensión a celdas en el cubo
    private List<Map<Integer, String>> idToValores;           // Mapeo de ids (pk) de la dimensión a valores
    private int columnaFkHechos;                    // Columna que contiene la clave foránea en la tabla de los hechos
    private int nivelActual = 0;
    
    /**
     * Constructor de la clase
     * 
     * @param nombre Nombre de la dimension
     */
    private Dimension(String nombre) {
        this.nombre = nombre;
        valoresToCeldas = new ArrayList<>();
        idToValores = new ArrayList<>();
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

        for (int j = 0; j < configDimension.getColumnaValor(); j++){ //pongo en cada posic un nuevo map
            dim.idToValores.add(new HashMap<>());
            dim.valoresToCeldas.add(new HashMap<>());
        }

        for (String[] datos : configDimension.getDatasetReader().read()) {
            int pkDimension = Integer.parseInt(datos[configDimension.getColumnaKey()]);
            String valor = "";

            for (int i = 0; i < configDimension.getColumnaValor(); i++){
                valor += datos[configDimension.getColumnaValor() - i] + "/";
                dim.idToValores.get(i).put(pkDimension, valor);

            //TODO: CREO que con un for aca sobre los niveles de las dimensiones podemos jerarquizar bien
            // crear los diccionarios de los niveles para valoreToCeldas
            // armar bien idToValores con los nombres bien hechos
                dim.valoresToCeldas.get(i).put(valor, new HashSet<>());
            }
        }
        return dim;
    }


    public Dimension copiar() {
        Dimension nueva = new Dimension(this.nombre);
        nueva.valoresToCeldas = new ArrayList<>();
        for (int i = 0; i < valoresToCeldas.size(); i++) {
            Map<String, Set<Integer>> nuevoMapa = new HashMap<>();
            for (Map.Entry<String, Set<Integer>> entry : this.valoresToCeldas.get(i).entrySet()) {
                nuevoMapa.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            nueva.valoresToCeldas.add(nuevoMapa);
        }
       /* nueva.idToValores = new ArrayList<>(this.idToValores.size());
        for (int i = 0; i < this.idToValores.size(); i++) {
            nueva.idToValores.add(new HashMap<>(this.idToValores.get(i)));
        }
        */
        nueva.idToValores = this.idToValores;
        nueva.columnaFkHechos = this.columnaFkHechos;
        nueva.nivelActual = this.nivelActual;
        return nueva;
    }
    

    public void filtrar(String valor) {
        filtrar(new String[]{valor});
    }
    
    public void filtrar(String[] valores) {
        List<Map<String, Set<Integer>>> nuevosValoresToCeldas = new ArrayList<>();
        for (int i = 0; i < valoresToCeldas.size(); i++) {
            nuevosValoresToCeldas.add(new HashMap<>());
        }
        for (String valor : valores) {
            if (valoresToCeldas.get(nivelActual).containsKey(valor)) {
                nuevosValoresToCeldas.get(nivelActual).put(valor, valoresToCeldas.get(nivelActual).get(valor));
            }
        }
        
        valoresToCeldas = nuevosValoresToCeldas;
    }
    


    @Override
    public String toString() {
        return "Dimension [nombre=" + nombre + "]";
    }

    public String[] getValores() {
        return valoresToCeldas.get(nivelActual).keySet().toArray(new String[0]);
    }

    public Set<Integer> getIndicesCeldas(String valor) {
        return valoresToCeldas.get(nivelActual).get(valor);
    }

    public String getNombre() {
        return nombre;
    }

    public String getValorFromId(Integer id) {
        return idToValores.get(nivelActual).get(id);
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

/*
    public void prueba(){
        //for (Integer i: valoresToCeldas.get(2).get("North America/Canada/Alberta/"))
        //    System.out.println(i);
        System.out.println(valoresToCeldas.get(0).get("North America/").size());
    } */
}


