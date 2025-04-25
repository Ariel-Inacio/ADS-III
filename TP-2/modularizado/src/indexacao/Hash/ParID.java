package indexacao.Hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParID implements indexacao.Hash.RegistroHashExtensivel<ParID> {


  private int id;
  private long endereco;
  private short TAMANHO = 12;

  public ParID() {
    this(-1, -1);
  }

  public ParID(long e, int i) {
  
    this.endereco = e;
    this.id = i;
  
  }

  @Override
  public int hashCode() {
    return Math.abs(this.id);
  }

  public short size() {
    return this.TAMANHO;
  }

  public String toString() {
    return this.endereco + ";" + this.id;
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(id);
    dos.writeLong(endereco);
    byte[] bs = baos.toByteArray();
    byte[] bs2 = new byte[TAMANHO];
    for (int i = 0; i < TAMANHO; i++)
      bs2[i] = ' ';
    for (int i = 0; i < bs.length && i < TAMANHO; i++)
      bs2[i] = bs[i];
    return bs2;
  }

  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.id = dis.readInt();
    this.endereco = dis.readLong();
  }

  public static int hash(int id) {
    return Math.abs(id);
  }
  public static int hash(String endereco) {
    return Math.abs(endereco.hashCode());
  }

  public int getId() {
    return id;
  }
  public long getEndereco() {
    return endereco;
  }

}