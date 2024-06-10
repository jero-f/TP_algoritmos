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
                ConfigDimension.configCSV("Productos", "src/olapcube/datasets-olap/productos.csv", 0, 3, 0),
                ConfigDimension.configCSV("Fechas", "src/olapcube/datasets-olap/fechas.csv", 0, 5, 2),
                ConfigDimension.configCSV("POS", "src/olapcube/datasets-olap/puntos_venta.csv", 0, 4, 1)
            }
        );
    }

    public static void main(String[] args) {
        ConfigCubo config = crearConfigCubo();

        Cubo cubo = Cubo.crearFromConfig(config);
        System.out.println("Cubo creado: " + cubo);

        //cubo.drillDown("POS");
        //cubo.drillDown("POS");
        //cubo.drillDown("Productos");
        //cubo.drillDown("Fechas");
        // Proyecciones
        //Proyeccion proyeccion = cubo.proyectar("cantidad","count");
        
        // Mostrar Dimension POS (hecho: default)
        //proyeccion.print("POS");

        // Mostrar Dimensiones POS vs Fechas (hecho: cantidad)
        //proyeccion.seleccionarHecho("cantidad");
        //proyeccion.print("POS", "Fechas");

        
        Cubo cuboDice = cubo.dice("Fechas", new String[]{" | 2017"," | 2018"});
        cuboDice.proyectar("valor_total","suma").print("POS","Fechas");
        //cuboDice.dice("POS", new String[]{"Canada", "France"}).proyectar("valor_total","suma").print("POS","Fechas");
    
        }
    
}
