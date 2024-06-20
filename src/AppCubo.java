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

    private static void proyeccionesBase(Cubo cubo) {
        // Proyecciones
        Proyeccion proyeccion = cubo.proyectar("valor_total","suma", 10, 3);
        cubo.drillDown("Fechas");
        cubo.drillDown("Fechas");
        cubo.drillDown("POS");
        cubo.drillDown("Productos");
        System.out.println(cubo);
        
        // POS (hecho: default)
        proyeccion.print("POS");

        // POS vs Fechas (hecho: cantidad)
        proyeccion.seleccionarHecho("cantidad");
        proyeccion.print("POS", "Fechas");

        // POS vs Productos (hecho: valor_unitario)
        proyeccion.seleccionarHecho("valor_unitario");
        proyeccion.print("POS", "Productos");

        // Slice France - Europe
        Cubo slicedFrance = cubo.slice("POS", "Europe/France/");
        slicedFrance.proyectar("valor_unitario", "suma", 10, 3).print("POS", "Productos");

        // Slice France - Europe con rollup a Region
        slicedFrance.rollUp("POS");
        slicedFrance.proyectar("valor_unitario", "suma", 20, 3).print("POS", "Productos");

        // Slice France - Europe con drilldown a Provincia
        slicedFrance.drillDown("POS");
        slicedFrance.drillDown("POS");
        slicedFrance.proyectar("valor_unitario", "suma", 20, 3).print("POS", "Productos");

        // Cubo original para probar independencia de estructura
        cubo.rollUp("POS");
        cubo.proyectar("valor_unitario", "suma", 10, 3).print("POS", "Productos");

        // Dice Europe + Pacific
        Cubo dicedEuropePacific = cubo.dice("POS", new String[]{"Europe/","Pacific/"});
        //dicedEuropePacific.rollUp("Fechas");
        dicedEuropePacific.rollUp("Fechas");
        dicedEuropePacific.rollUp("Fechas");
        dicedEuropePacific.proyectar("cantidad", "suma", 10, 3).print("POS", "Fechas");

        System.out.println(cubo);
        cubo.drillDown("POS");
        Cubo cubo2 = cubo.slice("Productos", "Accessories/Bike Racks/").slice("POS", "Europe/Germany/");
        cubo2.proyectar("costo", "suma", 10, 10).print("POS", "Productos");
    }

    public static void main(String[] args) {
        ConfigCubo config = crearConfigCubo();

        Cubo cubo = Cubo.crearFromConfig(config);
        System.out.println(cubo);

        proyeccionesBase(cubo);
    }
}