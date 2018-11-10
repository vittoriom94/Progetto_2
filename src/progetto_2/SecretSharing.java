package progetto_2;

import java.math.*;
import java.util.*;
import java.util.Map.Entry;

public class SecretSharing {
    private int bitLength;
    private static final int C = 100;
    private BigInteger p;

    public SecretSharing(int bitLength) {
        this.bitLength = bitLength;
        p = generatePrime(bitLength);
    }

    public SecretSharing(BigInteger p ){
        this.p = p;
        bitLength = p.bitLength();
    }
    public BigInteger getP(){
        return p;
    }

    public static BigInteger generatePrime(int bl) {
        boolean value = false;
        BigInteger pr;
        do {
            pr = BigInteger.probablePrime(bl, new Random());
            if (pr.isProbablePrime(C)) {
                value = true;
            }
        } while (value == false);
        return pr;
    }


    public BigInteger randomZp() {
        BigInteger r;
        do {
            r = new BigInteger(bitLength, new Random());
        }
        while (r.compareTo(BigInteger.ZERO) < 0 || r.compareTo(this.p) >= 0);
        return r;
    }


    public Map<BigInteger, BigInteger> genShares(BigInteger s, int k, int n) {
        if (p.compareTo(s) <= 0 || k > n) {
            return null;
        }

        BigInteger[] a = new BigInteger[k]; // vettore dei coeefficienti
        Map<BigInteger, BigInteger> shares = new HashMap<>(); //mappa che associa l'identità del partecipante i alla share Si

        a[0] = s;
        for (int i = 1; i < k; i++) {
            a[i] = randomZp();
        }

        BigInteger pi, share;
        for (Integer i = 1; i < n + 1; i++) //genero le share da inserire nella mappa
        {
            pi = new BigInteger(i.toString());
            share = BigInteger.ZERO;
            for (int j = 0; j < k; j++) {
                share = share.add(a[j].multiply(pi.pow(j))); //i-esima share Si
            }
            shares.put(pi, share.mod(p)); //la inserisco nella mappa
        }
        return shares;
    }

    public BigInteger getSecret(Map<BigInteger, BigInteger> shares) {

        BigInteger s = BigInteger.ZERO;
        BigInteger pi, vi, prod_ies;

        for (Entry<BigInteger, BigInteger> entry : shares.entrySet()) {
            pi = entry.getKey();
            vi = entry.getValue();

            prod_ies = BigInteger.ONE;
            for (BigInteger pj : shares.keySet()) {
                if (pj.compareTo(pi) != 0) {
                    prod_ies = prod_ies.multiply(pj.subtract(pi).modInverse(p).multiply(pj)); //prodotto i-esimo
                }
            }
            s = s.add(vi.multiply(prod_ies));
        }

        return s.mod(p);
    }
}
	