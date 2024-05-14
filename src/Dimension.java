import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dimension {
    Map<String, List<Integer>> dicc_id;
    
    public Dimension( List<String[]> df_significados , List<String[]> df_ventas) {
        Map<String, List<Integer>> dicc_id = new HashMap<>();

        String id_dimension = df_significados.get(0)[0]; // que id es el que va a ser usado
        Integer indice_en_df_ventas = -1;

        for(int i = 0; i < df_ventas.get(0).length; i++){ // busco en que columna está ese id en df_ventas
            if(id_dimension.equals(df_ventas.get(0)[i])){
                indice_en_df_ventas = i;
                break;
            }
        }

        for( int j = 1; j < df_ventas.size(); j++ ){
            for( String[] fila_significados : df_significados){
                if( df_ventas.get(j)[indice_en_df_ventas].equals(fila_significados[0])){
                    Agregar.agregarValor(dicc_id, fila_significados[fila_significados.length - 1], j-1);
                }
            }
        }

        this.dicc_id = dicc_id;
    }
}