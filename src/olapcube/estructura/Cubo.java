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
    
        if (hechos_celda.size() != nombresHechos.size()){
            throw new IllegalArgumentException("Celda con cantidad de hechos diferente a las demas");
        }

        int cant = celda.getValores(nombresHechos.get(0)).size();
        for (String hecho : nombresHechos){
            if (cant != celda.getValores(hecho).size()){
                throw new IllegalArgumentException("Celda con distinta cantidad de valores para cada hecho");
            }
        }
        celdas.add(celda);
    }


    /**
     * Método que permite aumentar el nivel de jerarquia de una dimensión
     * @param dimension Dimensión que se va a ver afectada
     * @throws IllegalArgumentException si la dimension pasada no se encuentra en el cubo
     */
    public void rollUp(String dimension){
        if (!dimensiones.containsKey(dimension)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + dimension );
        }
        dimensiones.get(dimension).aumentarJerarquia();
    }

    /**
     * Método que permite disminuir el nivel de jerarquia de una dimensión
     * @param dimension Dimensión que se va a ver afectada
     * @throws IllegalArgumentException si la dimension pasada no se encuentra en el cubo
     */
    public void drillDown(String dimension){
        if (!dimensiones.containsKey(dimension)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + dimension );
        }
        dimensiones.get(dimension).disminuirJerarquia();
    }

    @Override
    public String toString() {
        String out = "*** CUBO ***";
        out += "\n - Celdas: " + celdas.size();
        out += "\n - Dimensiones:";
        for (Dimension dimension : dimensiones.values()) {
            out += "\n  . " + dimension;
        }
        return out;
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

    /**
     * @param nombre_hecho hecho a proyectar
     * @param medida medida a proyectar
     * @param maxFilas máximo de filas a proyectar
     * @param maxColumnas máximo de columnas a proyectar
     * @return Proyeccion del cubo
     */
    public Proyeccion proyectar(String nombreHecho, String medida, int maxFilas, int maxColumnas) {
        return new Proyeccion(this, nombreHecho, medida, maxFilas, maxColumnas);
    }

    private Cubo copiar(){
        Cubo cubo = new Cubo();
        cubo.dimensiones = new HashMap<>();
        for (Dimension dimension : this.dimensiones.values()) {
            cubo.dimensiones.put(dimension.getNombre(), dimension.copiar());
        }
        cubo.medidas = this.medidas;
        cubo.celdas = this.celdas;
        cubo.nombresHechos = this.nombresHechos;
        return cubo;
    }

    /**
     * Método que genera un nuevo cubo que unicamente tengo los hechos que cumplan que en la dimensión pasada tengan ese valor
     * @param nombreDim dimensión en la que se aplica
     * @param valor valor que deben tener los hechos del nuevo cubo
     * @return cubo que en esa dimension solo tiene hechos con ese valor
     * @throws IllegalArgumentException si nombreDim no es una dimensión del cubo
     */
    public Cubo slice(String nombreDim, String valor) {
        if (!dimensiones.containsKey(nombreDim)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + nombreDim );
        }
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim).filtrar(valor);
        return cubo;
    }


        /**
     * Método que genera un nuevo cubo que unicamente tengo los hechos que cumplan que en la dimensión pasada tengan alguno de esos valores
     * @param nombreDim dimensión en la que se aplica
     * @param valor valor que deben tener los hechos del nuevo cubo
     * @return cubo que en esa dimension solo tiene hechos con esos valores
     * @throws IllegalArgumentException si nombreDim no es una dimensión del cubo
     */
    public Cubo dice(String nombreDim, String[] valores) {
        if (!dimensiones.containsKey(nombreDim)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + nombreDim );
        }
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim).filtrar(valores);
        return cubo;
    }

    /**
    * Método que genera un nuevo cubo que unicamente tengo los hechos que cumplan que en la dimensiones pasadas tengan alguno de esos valores
    * @param nombreDim dimensión 1
    * @param valores1 valores que deben tener los hechos del nuevo cubo en la dimension 1
    * @param nombreDim dimensión 2
    * @param valores2 valores que deben tener los hechos del nuevo cubo en la dimension 2
    * @return cubo que en esas dimensiones solo tiene hechos con esos valores
    * @throws IllegalArgumentException si algun nombreDim no es una dimensión del cubo
    */
    public Cubo dice(String nombreDim1, String[] valores1, String nombreDim2, String[] valores2) {
        if (!dimensiones.containsKey(nombreDim1)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + nombreDim1 );
        }
        if (!dimensiones.containsKey(nombreDim2)){
            throw new IllegalArgumentException("Dimensión no hallada en el cubo: " + nombreDim2 );
        }

        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim1).filtrar(valores1);
        cubo.dimensiones.get(nombreDim2).filtrar(valores2);
        return cubo;
    }
    /**
    * @param nombreDim dimensión 1
    * @param valores1 valores que deben tener los hechos del nuevo cubo en la dimension 1
    * @param nombreDim dimensión 2
    * @param valores2 valores que deben tener los hechos del nuevo cubo en la dimension 2
    * @param nombreDim dimensión 3
    * @param valores3 valores que deben tener los hechos del nuevo cubo en la dimension 3
    * @return cubo que en esas dimensiones solo tiene hechos con esos valores
    * @throws IllegalArgumentException si algun nombreDim no es una dimensión del cubo
    */
    public Cubo dice(String nombreDim1, String[] valores1, String nombreDim2, String[] valores2, String nombreDim3, String[] valores3) {
        Cubo cubo = this.copiar();
        cubo.dimensiones.get(nombreDim1).filtrar(valores1);
        cubo.dimensiones.get(nombreDim2).filtrar(valores2);
        cubo.dimensiones.get(nombreDim3).filtrar(valores3);
        return cubo;
    }
}