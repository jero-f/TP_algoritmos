import java.util.List;

public class App {
    public static void main(String[] args) {
        String filePath = "./recursos/productos.csv";
         List<String[]> datos =  Csv_reader.lector(filePath);
        for (String[] fields : datos) {
            for (String field : fields) {
                System.out.print(field + ", ");
            }
            System.out.println(); // Agrega un salto de línea después de imprimir cada línea
        }
    }
}