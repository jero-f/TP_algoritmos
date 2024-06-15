package olapcube.estructura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import olapcube.Proyeccion;
import olapcube.configuration.ConfigCubo;
import olapcube.configuration.ConfigDimension;
import olapcube.metricas.Count;
import olapcube.metricas.Maximo;
import olapcube.metricas.Medida;
import olapcube.metricas.Minimo;
import olapcube.metricas.Suma;

/**
 * Representa un cubo OLAP.
 */
public class Cubo {
    private Map<String, Dimension> dimensiones; // Mapeo de nombres de dimensión al objeto de la dimensión
    private Map<String, Medida> medidas;        // Mapeo de nombres de medida al objeto de la medida
    private List<Celda> celdas;                 // Lista de celdas del cubo
    private List<String> nombresHechos;         // Nombres de los hechos (columnas con valores del dataset de hechos)

    private Cubo() {
        dimensiones = new HashMap<>();
        celdas = new ArrayList<>();
        nombresHechos = new ArrayList<>();

        // TODO: Externalizar esta configuracion
        medidas = new HashMap<>();
        medidas.put("suma", new Suma());
        medidas.put("count", new Count());
        medidas.put("maximo", new Maximo());
        medidas.put("minimo", new Minimo());
    }

    /**
     * Método constructor que permite crear un cubo a partir de una configuración
     * 
     * @param config Configuración del cubo
     * @return Cubo
     */
    public static Cubo crearFromConfig(ConfigCubo config) {
        Cubo cubo = new Cubo();

        // Creacion de dimensiones
        for (ConfigDimension configDimension : config.getDimensiones()) {
            cubo.agregarDimension(Dimension.crear(configDimension));
        }

        // Creacion de hechos
        cubo.nombresHechos = List.of(config.getHechos().getNombresHechos());

        int indiceCelda = 0;
        for (String[] datos : config.getHechos().getDatasetReader().read()) {            
            Celda celda = new Celda();
            for (String hecho : cubo.nombresHechos) {
                int columnaHecho = config.getHechos().getColumnaHecho(hecho);
                celda.agregarHecho(hecho, Double.parseDouble(datos[columnaHecho]));
            }
            cubo.agregarCelda(celda);


            // Agrega la celda a las dimensiones
            for (Dimension dimension : cubo.dimensiones.values()) {
            //TODO: CREO que con un for aca sobre los niveles de las dimensiones podemos jerarquizar bien
            //guardar en cada diccionario de la lista de valoresToCeldas el indice celda. modificar agregarHecho para que
            // agregue los hechos a todos los niveles de valoresToCeldas
                int columnaFkHechos = dimension.getColumnaFkHechos();
                int fk = Integer.parseInt(datos[columnaFkHechos]);
                dimension.agregarHecho(fk, indiceCelda);
            }

            indiceCelda++;
        }

        return cubo;
    }

    public List<String> getNombresHechos() {
        return nombresHechos;
    }

    public List<String> getNombresDimensiones() {
        return new ArrayList<>(dimensiones.keySet());
    }

    public List<String> getMedidas() {
        return new ArrayList<>(medidas.keySet());
    }

    public Medida getMedida(String nombre) {
        return medidas.get(nombre);
    }

    public Dimension getDimension(String nombre) {
        if (!dimensiones.containsKey(nombre)) {
            throw new IllegalArgumentException("Dimension no encontrada: " + nombre);
        }
        return dimensiones.get(nombre);
    }

    public void agregarDimension(Dimension dim1) {
        dimensiones.put(dim1.getNombre(), dim1);
    }

    public void agregarCelda(Celda celda) {
        // TODO: Validar que la celda tenga los mismos hechos que las celdas anteriores
        boolean mismos_hechos = true;
        Set<String> hechos_celda =  celda.getHechos();
        for (String hecho : nombresHechos){
            if (!(hechos_celda.contains(hecho))){
                mismos_hechos = false;
            }
        }
        if (mismos_hechos == false){
            throw new IllegalArgumentException("Celda con hechos diferentes a las demas");
        }
    
        // TODO: Validar que la celda tenga la misma cantidad de hechos que los hechos del cubo
        if (hechos_celda.size() != nombresHechos.size()){
            throw new IllegalArgumentException("Celda con cantidad de hechos diferente a las demas");
        }

        // TODO: Validar que la celda tenga la misma cantidad de valores para cada hecho
        int cant = celda.getValores(nombresHechos.get(0)).size();
        for (String hecho : nombresHechos){
            if (cant != celda.getValores(hecho).size()){
                throw new IllegalArgumentException("Celda con distinta cantidad de valores para cada hecho");
            }
        }
        celdas.add(celda);
    }

    public void rollUp(String dimension){
        if (!dimensiones.containsKey(dimension)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + dimension );
        }
        dimensiones.get(dimension).rollUp();
    }

    public void drillDown(String dimension){
        if (!dimensiones.containsKey(dimension)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + dimension );
        }
        dimensiones.get(dimension).drillDown();
    }

    @Override
    public String toString() {
        return "Cubo [celdas=" + celdas.size() + ", dimensiones=" + dimensiones.keySet() + ", medidas=" + medidas.size() + "]";
    }

    /**
     * Obtiene las celdas a partir de un conjunto de indices
     * @param indices Conjunto de indices
     * @return Lista de celdas
     */
    private List<Celda> celdasFromIndices(Set<Integer> indices) {
        List<Celda> celdas = new ArrayList<>();
        for (Integer indice : indices) {
            celdas.add(this.celdas.get(indice));
        }
        return celdas;
    }

    /**
     * Obtiene una celda a partir de una dimensión y un valor, reduciendo las dos dimensiones restantes.
     * 
     * @param dimension La dimensión a la que pertenece el valor
     * @param valor El valor de la dimensión a buscar
     * @return Celda que agrupa todas las celdas que contienen el valor en esa dimensión
     */
    public Celda getCelda(Dimension dimension, String valor) {
        return Celda.agrupar(celdasFromIndices(dimension.getIndicesCeldas(valor)));
    }

    /**
     * Obtiene una celda a partir de dos dimensiones y dos valores, reduciendo la dimensión restante.
     * 
     * @param dim1 La primera dimensión
     * @param valor1 El valor de la primera dimensión
     * @param dim2 La segunda dimensión
     * @param valor2 El valor de la segunda dimensión
     * @return Celda que agrupa todas las celdas que contienen los valores en esas dos dimensiones
     */
    public Celda getCelda(Dimension dim1, String valor1, Dimension dim2, String valor2) {
        Set<Integer> indicesComunes = celdasComunes(dim1.getIndicesCeldas(valor1), dim2.getIndicesCeldas(valor2));
        return Celda.agrupar(celdasFromIndices(indicesComunes));
    }

    /**
     * Obtiene el conjunto de índices que existen en ambos conjuntos (intersección)
     * 
     * @param set1 El primer conjunto de índices
     * @param set2 El segundo conjunto de índices
     * @return Conjunto de índices que representa la intersección de ambos conjuntos
     */
    private static Set<Integer> celdasComunes(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> nuevo = new HashSet<>(set1);
        nuevo.retainAll(set2);
        return nuevo;
    }

    public Proyeccion proyectar(String nombre_hecho, String medida) {
        return new Proyeccion(this, nombre_hecho, medida);
    }

    private Cubo copiar(){
        Cubo cubo = new Cubo();
        // TODO: mejorar (copia superficial, revisar)
        cubo.dimensiones = new HashMap<>();
        for (Dimension dimension : this.dimensiones.values()) {
            cubo.dimensiones.put(dimension.getNombre(), dimension.copiar());
        }
        cubo.medidas = this.medidas;
        cubo.celdas = this.celdas;
        cubo.nombresHechos = this.nombresHechos;
        return cubo;
    }


    public Cubo slice(String nombreDim, String valor) {
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim).filtrar(valor);
        return cubo;
    }

    public Cubo dice(String nombreDim, String[] valores) {
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim).filtrar(valores);
        return cubo;
    }

    public Cubo dice(String nombreDim1, String[] valores1, String nombreDim2, String[] valores2) {
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim1).filtrar(valores1);
        cubo.dimensiones.get(nombreDim2).filtrar(valores2);
        return cubo;
    }
    
    public Cubo dice(String nombreDim1, String[] valores1, String nombreDim2, String[] valores2, String nombreDim3, String[] valores3) {
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim1).filtrar(valores1);
        cubo.dimensiones.get(nombreDim2).filtrar(valores2);
        cubo.dimensiones.get(nombreDim3).filtrar(valores3);
        return cubo;
    }
}
