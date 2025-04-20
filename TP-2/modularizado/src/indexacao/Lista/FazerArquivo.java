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
    
}
