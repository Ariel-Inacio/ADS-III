package util;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidadeData {
    
    // Método para verificar se uma data e valida
    public static boolean isDataValida(String dataStr, DateTimeFormatter formato) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalDate data = LocalDate.parse(dataStr, formato);
            return true;
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida, tente novamente...");
            return false;
        }
    }

    // Método para verificar se um ano é valido
    public static boolean isAnoValido(String anoStr) {
        if (anoStr == null) {
            return false;
        }
        
        try {
            Year ano = Year.parse(anoStr);
            
            // Verifica se o ano está em um intervalo aceitável
            return (ano.getValue() >= 1900 && ano.getValue() <= 2100);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de ano inválido, tente novamente...");
            return false;
        }
    }

}
