
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Csv_reader {
    

    public static List<String[]> lector(String filePath) {
        List<String[]> datos = new ArrayList<>();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            // Iterar sobre cada línea del archivo CSV
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Dividir la línea en campos usando coma como delimitador
                String[] fields = line.split(";");
                datos.add(fields);
            }
            scanner.close(); // Cerrar el scanner al finalizar la lectura

        } catch (FileNotFoundException e) {
            System.err.println("Archivo no encontrado: " + e.getMessage());
        }
        return datos;
    }
}