package classes;

import indexacao.Arvore.RegistroArvoreBMais;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistroID implements RegistroArvoreBMais<RegistroID> {
    private int id; // ID do objeto
    private long offset; // Posição no arquivo de dados
    private static final short TAMANHO = 12; // 4 bytes para ID + 8 bytes para offset

    public RegistroID() {
        this(-1, -1);
    }

    public RegistroID(int id, long offset) {
        this.id = id;
        this.offset = offset;
    }

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeLong(offset);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.offset = dis.readLong();
    }

    @Override
    public int compareTo(RegistroID obj) {
        return Integer.compare(this.id, obj.id);
    }

    @Override
    public RegistroID clone() {
        return new RegistroID(this.id, this.offset);
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Offset: " + offset;
    }
}

