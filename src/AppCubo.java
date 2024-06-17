import olapcube.Proyeccion;
import olapcube.configuration.ConfigCubo;
import olapcube.configuration.ConfigDimension;
import olapcube.configuration.ConfigHechos;
import olapcube.estructura.Cubo;

public class AppCubo {

    private static final String[] NOMBRES_HECHOS = new String[] {"cantidad", "valor_unitario", "valor_total", "costo"};
    private static final Integer[] COLUMNAS_HECHOS = new Integer[] {3, 4, 5, 6};
    
    private static ConfigCubo crearConfigCubo() {
        return new ConfigCubo(
            "Cubo de ventas",
            ConfigHechos.configCSV(
                "src/olapcube/datasets-olap/ventas.csv", 
                NOMBRES_HECHOS,
                COLUMNAS_HECHOS
            ),
            new ConfigDimension[] {
                ConfigDimension.configCSV("Productos", "src/olapcube/datasets-olap/productos.csv", 0, 3, 0, new int[]{3,2,1}),
                ConfigDimension.configCSV("Fechas", "src/olapcube/datasets-olap/fechas.csv", 0, 5, 2, new int[]{5,4,3,2,1}),
                ConfigDimension.configCSV("POS", "src/olapcube/datasets-olap/puntos_venta.csv", 0, 5, 1, new int[]{5,4,3,2,1})
            }
        );
    }

    public static void main(String[] args) {
        ConfigCubo config = crearConfigCubo();

        Cubo cubo = Cubo.crearFromConfig(config);
        System.out.println("Cubo creado: " + cubo);

        cubo.drillDown("POS");
        cubo.drillDown("POS");
        cubo.drillDown("POS");
        cubo.rollUp("POS");
        cubo.drillDown("Productos");
        cubo.drillDown("Productos");
        //cubo.rollUp("Productos");
        //cubo.drillDown("Fechas");
        //cubo.drillDown("Fechas");
        //cubo.drillDown("Fechas");
        //cubo.rollUp("Fechas");
        // Proyecciones
        Proyeccion proyeccion = cubo.proyectar("valor_total","suma", 10, 4);
        
        // Mostrar Dimension POS (hecho: default)
        //proyeccion.print("POS");

        // Mostrar Dimensiones POS vs Fechas (hecho: cantidad)
        proyeccion.seleccionarHecho("valor_total");
        proyeccion.print("POS", "Productos");

        
        //Cubo cuboSlice = cubo.slice("Fechas", "2017/").slice("POS", "North America/Canada/");
        //cuboSlice.proyectar("valor_total","suma").print("POS","Fechas");
        Cubo cuboDice = cubo.dice("POS", new String[]{"North America/Canada/Alberta/", "North America/Canada/Ontario/"}).slice("Fechas", "2017/");
        cuboDice.proyectar("cantidad","suma", 10, 5).print("POS","Fechas");
        }
}