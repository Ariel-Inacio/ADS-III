package classes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Filmes implements Externalizable, Comparable<Filmes>{

    private boolean lapide;
    private int id;
    private String tipo;
    private String nome;
    private String diretor;
    private String pais;
    private LocalDate ano_adi;
    private Year ano_lan;
    private String classificacao;
    private String duracao;
    private String Genero;

    public Filmes(){}

    public Filmes(List<String> lista, int tmp, Boolean formasFormatacao) {
        // Define o formato da data com base na opção foramasFormatacao que e fornecia de acordo com a funcao que que instancia esta
        DateTimeFormatter format;
        if (formasFormatacao) {
            format = DateTimeFormatter.ofPattern("yyyy/M/d");
            LocalDate data = LocalDate.parse(lista.get(5), format);
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
            lista.set(5, data.format(format));
        } else {
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
        }
        
        // Inicializa os atributos do objeto Filmes
        this.lapide = false;
        this.id = tmp;
        this.tipo = lista.get(1);
        this.nome = lista.get(2);
        this.diretor = lista.get(3);
        this.pais = lista.get(4);
        
        // Converte a data para LocalDate
        LocalDate data = LocalDate.parse(lista.get(5), format);
        this.ano_adi = data;
        
        // Converte o ano de lançamento para Year
        Year anoLan = Year.parse(lista.get(6));
        this.ano_lan = anoLan;
        
        this.classificacao = lista.get(7);
        this.duracao = lista.get(8);
        this.Genero = lista.get(9);
    }
    
    @Override
    public void writeExternal(ObjectOutput Out) throws IOException {
        // Escreve os atributos no fluxo de saída
        Out.writeBoolean(lapide);
        Out.writeInt(id);
        
        byte[] tipoBytes = tipo.getBytes("UTF-8");
        Out.writeShort(tipoBytes.length);
        Out.write(tipoBytes);
        
        byte[] nomeBytes = nome.getBytes("UTF-8");
        Out.writeShort(nomeBytes.length);
        Out.write(nomeBytes);
        
        byte[] diretorBytes = diretor.getBytes("UTF-8");
        Out.writeShort(diretorBytes.length);
        Out.write(diretorBytes);
        
        byte[] paisBytes = pais.getBytes("UTF-8");
        Out.writeShort(paisBytes.length);
        Out.write(paisBytes);
        
        // Escreve a data de adição
        Out.writeByte(ano_adi.getMonthValue());
        Out.writeByte(ano_adi.getDayOfMonth());
        Out.writeShort(ano_adi.getYear());
        
        // Escreve o ano de lançamento
        Out.writeShort(ano_lan.getValue());
        
        byte[] classificacaoBytes = classificacao.getBytes("UTF-8");
        Out.writeShort(classificacaoBytes.length);
        Out.write(classificacaoBytes);
        
        byte[] duracaoBytes = duracao.getBytes("UTF-8");
        Out.writeShort(duracaoBytes.length);
        Out.write(duracaoBytes);
        
        byte[] GeneroBytes = Genero.getBytes("UTF-8");
        Out.writeShort(GeneroBytes.length);
        Out.write(GeneroBytes);
    }
    
    @Override
    public void readExternal(ObjectInput dataIn) throws IOException {
        try {
            // Lê os dados do fluxo de entrada
            lapide = dataIn.readBoolean();
            id = dataIn.readInt();
            
            byte[] tipoBytes = new byte[dataIn.readShort()];
            dataIn.readFully(tipoBytes);
            tipo = new String(tipoBytes, "UTF-8");
            
            byte[] nomeBytes = new byte[dataIn.readShort()];
            dataIn.readFully(nomeBytes);
            nome = new String(nomeBytes, "UTF-8");
            
            byte[] diretorBytes = new byte[dataIn.readShort()];
            dataIn.readFully(diretorBytes);
            diretor = new String(diretorBytes, "UTF-8");
            
            byte[] paisBytes = new byte[dataIn.readShort()];
            dataIn.readFully(paisBytes);
            pais = new String(paisBytes, "UTF-8");
            
            int mes = dataIn.readByte();
            int dia = dataIn.readByte();
            int ano = dataIn.readShort();
            ano_adi = LocalDate.of(ano, mes, dia);
            
            int anoLan = dataIn.readShort();
            ano_lan = Year.of(anoLan);
            
            byte[] classificacaoBytes = new byte[dataIn.readShort()];
            dataIn.readFully(classificacaoBytes);
            classificacao = new String(classificacaoBytes, "UTF-8");
            
            byte[] duracaoBytes = new byte[dataIn.readShort()];
            dataIn.readFully(duracaoBytes);
            duracao = new String(duracaoBytes, "UTF-8");
            
            byte[] GeneroBytes = new byte[dataIn.readShort()];
            dataIn.readFully(GeneroBytes);
            Genero = new String(GeneroBytes, "UTF-8");
        } catch (IOException e) {
            throw new IOException("Erro ao ler objeto Filmes", e);
        }
    }

    public String CriterioLista(int criterios){
        // Retorna o valor do atributo correspondente ao critério fornecido
        switch(criterios){
            case 1: return tipo;
            case 3: return diretor;
            case 4: return pais;
            case 5:{
                LocalDate dataTmp = ano_adi;
                // Formata a data de adição para o formato "M/d/yyyy"
                DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
                return dataTmp.format(format);
            }
            case 6: return ano_lan.toString();
            case 7: return classificacao;
            case 9: return Genero;
            default: return null;
        }
    }
    
    @Override
    public int compareTo(Filmes f) {
        // Compara os filmes pelo ID
        return Integer.compare(this.id, f.id);
    }
    
    public void Ler(){
        // Exibe os dados do filme
        System.out.println("----------------------------------------");
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome.trim());
        System.out.println("Ano de Lancamento: " + ano_lan);
        System.out.println("Data de Adicao: " + ano_adi);
        System.out.println("Duração: " + duracao.trim());
        System.out.println("Diretor: " + diretor.trim());
        System.out.println("Pais: " + pais.trim());
        System.out.println("Gênero: " + Genero.trim());
        System.out.println("Tipo: " + tipo.trim());
        System.out.println("Faixa Etaria: " + classificacao.trim());
        System.out.println("----------------------------------------");
    }
    public boolean getLAPIDE(){
        return lapide;
    }
    public void setLAPIDE(boolean lapide){
        this.lapide = lapide;
    }

    public int getID(){
        return id;
    }
    public void setID (int id){
        this.id = id;
    }

    public String getTIPO(){
        return tipo;
    }
    public void setTIPO (String tipo){
        this.tipo = tipo;
    }

    public String getNOME(){
        return nome;
    }
    public void setNOME (String nome){
        this.nome = nome;
    }

    public String getDIRETOR(){
        return diretor;
    }
    public void setDIRETOR (String diretor){
        this.diretor = diretor;
    }

    public String getPAIS(){
        return pais;
    }
    public void setPAIS (String pais){
        this.pais = pais;
    }

    public LocalDate getANO_ADI(){
        return ano_adi;
    }
    public void setANO_ADI (LocalDate ano_adi){
        this.ano_adi = ano_adi;
    }

    public Year getANO_LAN(){
        return ano_lan;
    }
    public void setANO_LAN (Year ano_lan){
        this.ano_lan = ano_lan;
    }

    public String getCLSSIFICACAO(){
        return classificacao;
    }
    public void setCLASSIFICACAO (String classificacao){
        this.classificacao = classificacao;
    }

    public String getDURACAO(){
        return duracao;
    }
    public void setDURACAO (String duracao){
        this.duracao = duracao;
    }
    
    public String getGENERO(){
        return Genero;
    }
    public void setGENERO (String genero){
        this.Genero = genero;
    }

}