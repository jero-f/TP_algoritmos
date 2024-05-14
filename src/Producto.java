public class Producto {
    private int id;
    private String categoria;
    private String subcategoria;

    // Constructor, getters y setters
}

public class Tiempo {
    private int id;
    private int dia;
    private int mes;
    private int trimestre;
    private int anio;

    // Constructor, getters y setters
}

public class Local {
    private int id;
    private String nombre;
    private String ciudad;
    private String pais;
    private String region;

    // Constructor, getters y setters
}

public class TablaHechos {
    private Producto producto;
    private Tiempo tiempo;
    private Local local;
    private int id_venta;
    // Otros campos de la tabla de hechos

    // Constructor, getters y setters
}
