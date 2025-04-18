package z;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ano {
    
    public static void main(String[] args) {
        
        String arquivo = "netflix1.csv";
        String genero = "ano.csv";

        try(BufferedReader leitura = new BufferedReader(new FileReader(arquivo));
        RandomAccessFile raf = new RandomAccessFile(genero, "rw")){

            String line = leitura.readLine(); // Lê o cabeçalho do arquivo CSV

            while((line = leitura.readLine()) != null){

                // Limpeza da linha para remover caracteres indesejados
                line = line.replaceAll(";", "").trim();
                line = line.replaceAll("^\"|\"$", "").trim();
                line = line.replaceAll("\"\"", "\"").trim();

                List<String> dadosFilme = extrairDadosLinha(line);

                line = dadosFilme.get(6); // Pega o gênero do filme


                raf.seek(0); // Volta para o início do arquivo
                boolean existe = false; // Variável para verificar se o gênero já existe

                while(raf.getFilePointer() < raf.length()){

                    String tipo = raf.readLine(); // Lê a linha do arquivo de gêneros

                    if(tipo.equals(line)){ // Se o gênero já existe, não adiciona novamente
                        existe = true;
                        break;
                    }

                }

                if(!existe){ // Se o gênero não existe, adiciona ao arquivo
                    raf.writeBytes(line + "\n");
                }

                

            }

        }catch (Exception e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    //Função para tratar melhor as string presentes no arquivo CSV
    private static List<String> extrairDadosLinha(String line) {

        List<String> lista = new ArrayList<>(); // Lista para armazenar os dados extraídos da linha
    
        // Expressão regular para capturar campos entre aspas ou separados por vírgula
        Matcher m = Pattern.compile("\"([^\"]*)\"|([^,]+)").matcher(line);
    
        while (m.find()) {
            // Se o primeiro grupo da regex (dentro de aspas) for encontrado, usa ele; caso contrário, usa o segundo
            String resultado = m.group(1) != null ? m.group(1) : m.group(2);
            if (resultado != null) {
                lista.add(resultado.trim()); // Adiciona o valor à lista, removendo espaços extras
            }
        }
        
        return lista; // Retorna a lista de valores extraídos
    }

}