package progetto_2;

public class NotEnoughSharesException extends RuntimeException {
    public NotEnoughSharesException(String key, int minshares) {
        super("La chiave " + key + " necessita di almeno " + minshares + " partecipanti.");
    }
}
