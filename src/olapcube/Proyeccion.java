package olapcube;

import java.util.Arrays;

import olapcube.estructura.Celda;
import olapcube.estructura.Cubo;
import olapcube.estructura.Dimension;

/**
 * Clase que representa una proyeccion de un cubo OLAP
 */
public class Proyeccion {
    private Cubo cubo;              // Cubo sobre el que se realiza la proyeccion
    private int maxFilas = 10;      // Maximo de filas a mostrar
    private int maxColumnas = 4;   // Maximo de columnas a mostrar
    private String hecho;           // Hecho a proyectar
    private String medida;          // Medida a proyectar
    
    // Atributos para mostrar en consola
    //private int tamanoCelda = 20;
    private String formatoCelda = "%8.8s";
    private String separador = " | ";

    /**
     * Constructor de la clase
     * 
     * @param cubo Cubo sobre el que se realiza la proyeccion
     */
    public Proyeccion(Cubo cubo, String nombre_hecho, String nombre_medida) {
        this.cubo = cubo;
    
        boolean hecho_esta = false;
        for (String hecho : cubo.getNombresHechos()){
            if (hecho.equals(nombre_hecho)){
                hecho_esta = true;
            }
        }
        if (hecho_esta == false){
            throw new IllegalArgumentException("nombre hecho no encontrado: " + nombre_hecho);
        }
        this.hecho = nombre_hecho;    // Selecciona el primer hecho por defecto, modificado
        
        
        boolean medida_esta = false;
        for (String medida : cubo.getMedidas()){
            if (medida.equals(nombre_medida)){
                medida_esta = true;
            }
        }
        if (medida_esta == false){
            throw new IllegalArgumentException("nombre medida no encontrado: " + nombre_medida);
        }
        this.medida = nombre_medida;         // Selecciona la primera medida por defecto, modificado
    }

    public void seleccionarHecho(String hecho) {
        this.hecho = hecho;
    }

    public void seleccionarMedida(String medida) {
        this.medida = medida;
    }

    /**
     * Muestra la proyeccion de una dimension
     * 
     * @param nombreDimension Nombre de la dimension a proyectar
     */
    public void print(String nombreDimension) {
        Dimension dimension = cubo.getDimension(nombreDimension);
        System.out.println("Proyeccion de " + dimension.getNombre());
        
        String[] columnas = new String[] {hecho + " (" + medida + ")"};

        // Genera celdas de la proyeccion
        Double[][] valores = new Double[dimension.getValores().length][1];
        for (int i = 0; i < dimension.getValores().length; i++) {
            String valorDimension = dimension.getValores()[i];
            Celda celdaAgrupada = cubo.getCelda(dimension, valorDimension);
            valores[i][0] = cubo.getMedida(medida).calcular(celdaAgrupada.getValores(hecho));
        }

        // Muestra en consola
        printTablaConsola(dimension.getValores(), columnas, valores);
    }

    /**
     * Muestra la proyeccion de dos dimensiones
     * 
     * @param nombreDim1 Nombre de la primera dimension (filas)
     * @param nombreDim2 Nombre de la segunda dimension (columnas)
     */
    public void print(String nombreDim1, String nombreDim2) {
        Dimension dimension1 = cubo.getDimension(nombreDim1);
        Dimension dimension2 = cubo.getDimension(nombreDim2);
        System.out.println("Proyeccion de " + dimension1.getNombre() + " vs " + dimension2.getNombre() + " - " + hecho + " (" + medida + ")");
        
        // Genera celdas de la proyeccion
        Double[][] valores = new Double[dimension1.getValores().length][dimension2.getValores().length];
        for (int i = 0; i < dimension1.getValores().length; i++) {
            String valorDim1 = dimension1.getValores()[i];
            for (int j = 0; j < dimension2.getValores().length; j++) {
                String valorDim2 = dimension2.getValores()[j];
                Celda celdaAgrupada = cubo.getCelda(dimension1, valorDim1, dimension2, valorDim2);
                valores[i][j] = cubo.getMedida(medida).calcular(celdaAgrupada.getValores(hecho));
            }
        }

        // Muestra en consola
        printTablaConsola(dimension1.getValores(), dimension2.getValores(), valores);
    }

    /**
     * Muestra una tabla en consola
     * 
     * @param indice Labels o valores de las filas
     * @param header Labels o valores de las columnas
     * @param valores Valores de la tabla
     */

    /*
    private void printTablaConsola(String[] indice, String[] header, Double[][] valores) {
        if (indice.length > maxFilas) {
            indice = Arrays.copyOfRange(indice, 0, maxFilas);
        }
        if (header.length > maxColumnas) {
            header = Arrays.copyOfRange(header, 0, maxColumnas);
        }

        // Print del header
        System.out.printf(formatoCelda, separador);
        for (String columna : header) {
            System.out.printf(formatoCelda, columna);
            System.out.print(separador);
        }
        System.out.println();
        System.out.println("---------------------------------");

        for (int i = 0; i < indice.length; i++) {
            System.out.printf(formatoCelda, indice[i]);
            System.out.print(separador);
            for (int j = 0; j < header.length; j++) {
                // TODO: Formatear bien el valor de la celda
                System.out.printf(formatoCelda, valores[i][j]);
                System.out.print(separador);
            }
            System.out.println();
        }
    }  */

    private void printTablaConsola(String[] indice, String[] header, Double[][] valores) {
        int cellPadding = 1;  // Espacio entre el contenido y el borde de la celda
        String separator = " | ";
    
        // Ajuste de índice y header según maxFilas y maxColumnas
        if (indice.length > maxFilas) {
            indice = Arrays.copyOfRange(indice, 0, maxFilas);
        }
        if (header.length > maxColumnas) {
            header = Arrays.copyOfRange(header, 0, maxColumnas);
        }
    
        // Crear una lista para los anchos de cada columna
        int[] columnWidths = new int[header.length + 1];
    
        // Calcular el ancho máximo de la primera columna (índice)
        columnWidths[0] = cellPadding;
        for (String s : indice) {
            if (s.length() + cellPadding > columnWidths[0]) {
                columnWidths[0] = s.length() + cellPadding;
            }
        }
    
        // Calcular el ancho máximo de cada columna del header y valores
        for (int j = 0; j < header.length; j++) {
            columnWidths[j + 1] = header[j].length() + cellPadding;
            for (int i = 0; i < indice.length; i++) {
                if (valores[i][j] != null) {
                    String valueStr = String.format("%.2f", valores[i][j]);
                    if (valueStr.length() + cellPadding > columnWidths[j + 1]) {
                        columnWidths[j + 1] = valueStr.length() + cellPadding;
                    }
                } else {
                    String naStr = "N/A";
                    if (naStr.length() + cellPadding > columnWidths[j + 1]) {
                        columnWidths[j + 1] = naStr.length() + cellPadding;
                    }
                }
            }
        }
    
        // Construir los formatos para cada columna
        String[] formats = new String[columnWidths.length];
        for (int i = 0; i < formats.length; i++) {
            formats[i] = "%-" + columnWidths[i] + "s";
        }
        String[] numberFormats = new String[columnWidths.length];
        for (int i = 1; i < numberFormats.length; i++) {
            numberFormats[i] = "%" + columnWidths[i] + ".2f";
        }
    
        // Print del header
        System.out.printf(formats[0], "");
        System.out.print(separator);
        for (int j = 0; j < header.length; j++) {
            System.out.printf(formats[j + 1], header[j]);
            System.out.print(separator);
        }
        System.out.println();
    
        // Ajustar la línea de separación para que cubra hasta donde terminan los headers
        int totalWidth = 0;
        for (int j = 0; j < header.length + 1; j++) {
            totalWidth += columnWidths[j] + separator.length();
        }
        totalWidth -= separator.length(); // No necesitamos un separador al final
    
        // Imprimir la línea de separación
        System.out.println(new String(new char[totalWidth]).replace('\0', '-'));
    
        // Mostrar los valores
        for (int i = 0; i < indice.length; i++) {
            System.out.printf(formats[0], indice[i]);
            System.out.print(separator);
            for (int j = 0; j < header.length; j++) {
                if (valores[i][j] != null) {
                    System.out.printf(numberFormats[j + 1], valores[i][j]);
                } else {
                    System.out.printf(formats[j + 1], "N/A");
                }
                System.out.print(separator);
            }
            System.out.println();
        }
    }
    
    
}
