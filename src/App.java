

public class App {
    public static void main(String[] args) {
        String filePath = "./recursos/productos.csv";
        Csv_reader df = new Csv_reader(filePath);
        for (String[] fields : df.datos) {
            for (String field : fields) {
                System.out.print(field + ", ");
            }
            System.out.println(); // Agrega un salto de línea después de imprimir cada línea
        }
    }
}