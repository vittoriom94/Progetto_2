package progetto_2;

import java.io.Serializable;
import java.util.Objects;

public class Identifier implements Serializable {
    private String mittente = "";
    private String destinatario = "";
    private String nome = "";
    private int k;
    private static final long serialVersionUID = 4L;

    public Identifier(String mittente, String destinatario, String nome, int k, String key) {
        this.k = k;
        switch(key){
            case "RSAPublic":
            case "RSAPrivate":
                this.destinatario = destinatario;
                break;
            case "MAC":
                this.destinatario = destinatario;
                this.mittente = mittente;
                this.nome = nome;
                break;
            case "DSAPublic":
            case "DSAPrivate":
                this.mittente = mittente;
                break;
            default:
                this.mittente= mittente;
                this.destinatario=destinatario;
                this.nome=nome;
                break;

        }
    }

    public String getMittente() {
        return mittente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getNome() {
        return nome;
    }

    public int getK() {
        return k;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(mittente, that.mittente) &&
                Objects.equals(destinatario, that.destinatario) &&
                Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mittente, destinatario, nome);
    }
    @Override
    public String toString(){
        return "Nome: "+ nome + " Mittente: " + mittente + " Destinatario: " + destinatario;
    }
}
