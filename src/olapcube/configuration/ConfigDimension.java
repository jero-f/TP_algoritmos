package olapcube.configuration;

import olapcube.readers.CSVReader;
import olapcube.readers.DatasetReader;

/**
 * Clase que representa la configuración de una dimensión
 */
public class ConfigDimension {
    private String nombre;                  // Nombre de la dimensión
    private DatasetReader datasetReader;    // DatasetReader de la dimensión
    private int columnaKey;                 // Columna que contiene la clave primaria en el dataset de la dimensión
    private int columnaValor;               // Columna que contiene el valor en el dataset de la dimensión
    private int columnaFkHechos;            // Columna que contiene la clave foránea en el dataset de los hechos
    private int[] columnasJerarquias;       // primer numero = jerarquia mas abstracta, ultimo numero = jerarquia menos abstracta, 
                                            // columnas en las que están las jerarquias en el dataset de la dimensión
    /**
     * Constructor de la clase
     * 
     * @param nombre Nombre de la dimensión
     * @param datasetReader DatasetReader de la dimensión
     * @param columnaKey Columna que contiene la clave primaria en el dataset de la dimensión
     * @param columnaValor Columna que contiene el valor en el dataset de la dimensión
     * @param columnaFkHechos Columna que contiene la clave foránea en el dataset de los hechos
     * @param columnasJerarquias Columnas que contienen cada nivel de jerarquia en la tabla de la dimensión
     */
    private ConfigDimension(String nombre, DatasetReader datasetReader, int columnaKey, int columnaValor, int columnaFkHechos, int[] columnasJerarquias) {
        this.nombre = nombre;
        this.datasetReader = datasetReader;
        this.columnaKey = columnaKey;
        this.columnaValor = columnaValor;
        this.columnaFkHechos = columnaFkHechos;
        this.columnasJerarquias = columnasJerarquias;
    }

    /**
     * Método que permite crear una configuración de dimensión a partir de un archivo CSV
     * 
     * @param nombre Nombre de la dimensión
     * @param filePath Ruta del archivo CSV
     * @param columnaKey Columna que contiene la clave primaria en la tabla de la dimensión
     * @param columnaValor Columna que contiene el valor en la tabla de la dimensión
     * @param columnaFkHechos Columna que contiene la clave foránea en la tabla de los hechos
     * @param columnasJerarquias Columnas que contienen cada nivel de jerarquia en la tabla de la dimensión
     * @return
     */
    public static ConfigDimension configCSV(String nombre, String filePath, int columnaKey, int columnaValor, int columnaFkHechos, int[] columnasJerarquias) {
        return new ConfigDimension(nombre, new CSVReader(filePath), columnaKey, columnaValor, columnaFkHechos, columnasJerarquias);
    }

    public String getNombre() {
        return nombre;
    }

    public DatasetReader getDatasetReader() {
        return datasetReader;
    }

    public int getColumnaKey() {
        return columnaKey;
    }

    public int getColumnaValor() {
        return columnaValor;
    }

    public int getColumnaFkHechos() {
        return columnaFkHechos;
    }

    public int[] getColumnasJerarquias(){
        return columnasJerarquias;
    }
}