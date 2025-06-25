package indexacao.Lista;

public class ElementoLista implements Comparable<ElementoLista>, Cloneable {
    
    private long posicao;
    private int id;

    public ElementoLista(Long l, int i) {
        this.posicao = l;
        this.id = i;
    }

    public long getLocalizacao() {
        return posicao;
    }

    public void setLocalizacao(long posicao) {
        this.posicao = posicao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "("+this.posicao+";"+this.id+")";
    }

    @Override
    public ElementoLista clone() {
        try {
            return (ElementoLista) super.clone();
        } catch (CloneNotSupportedException e) {
            // Tratamento de exceção se a clonagem falhar
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int compareTo(ElementoLista outro) {
        return Integer.compare(this.id, outro.id);
    }
}