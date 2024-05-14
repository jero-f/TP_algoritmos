import java.util.List;

public class App {
    public static void main(String[] args) {
        String filePath1 = "./recursos/ventas.csv";
        String filePath2 = "./recursos/fechas.csv";
        String filePath3 = "./recursos/puntos_venta.csv";
        String filePath4 = "./recursos/productos.csv";

        List<String[]> ventas =  Csv_reader.lector(filePath1);
        List<String[]> fechas = Csv_reader.lector(filePath2);
        List<String[]> puntos_venta = Csv_reader.lector(filePath3);
        List<String[]> productos = Csv_reader.lector(filePath4);

//        for (String[] fields : productos) {
//            for (String field : fields) {
//                System.out.print(field + ", ");
//            }
            //System.out.println(fields[1]);
//            System.out.println();
//        }
        
        Dimension dicc_fechas = new Dimension(fechas, ventas);
        Dimension dicc_puntos_venta = new Dimension(puntos_venta, ventas);
        Dimension dicc_productos = new Dimension(productos, ventas);        

//        for (String clave : dicc_fechas.dicc_id.keySet()) {
//            System.out.println(clave);
//        }
        List<Integer> filas_con_id_fecha_0 = dicc_fechas.dicc_id.get("2020");
        for( int fila : filas_con_id_fecha_0){
            System.out.println(fila);
            //System.out.println(); // Agrega un salto de línea después de imprimir cada línea
        }
    }
}