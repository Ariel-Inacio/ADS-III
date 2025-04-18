package indexacao.Lista;

public class FazerArquivo {

    public static String Arquivo(int opcao){

        String nomeArquivo = null;
        switch(opcao){
            case 1: nomeArquivo = "ListaInvertidaMovie&TVShow"; break;
            case 3: nomeArquivo = "ListaInvertidaDiretor"; break;
            case 4: nomeArquivo = "ListaInvertidaPais"; break;
            case 5: nomeArquivo = "ListaInvertidaDataAdi"; break;
            case 6: nomeArquivo = "ListaInvertidaAnoLan"; break;
            case 7: nomeArquivo = "ListaInvertidaClass"; break;
            case 9: nomeArquivo = "ListaInvertidaGenero"; break;
            default: System.out.println("Opcao invalida"); break;
        }
        return nomeArquivo;

    }

    public static int BuscarCriterio(int criterio){

        int index = 0;
        switch(criterio){
            case 1: index = 1; break; // Nome do filme
            case 2: index = 3; break; // Diretor
            case 3: index = 4; break; // País
            case 4: index = 5; break; // Data de Adição
            case 5: index = 6; break; // Ano de Lançamento
            case 6: index = 7; break; // Classificação Indicativa
            case 7: index = 9; break; // Gênero
            default: System.out.println("Opcao invalida"); break;
        }
        return index;

    }
    
}
