package crypto;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 *
 * @author stkerr
 */
public class MentalPokerWrapper
{

    private final static SecureRandom random = new SecureRandom();
    
    
    /**
     * The 4096-bit modulus to be used for the Mental Poker stages
     * This number was pregenerated, but a new one can be made with the following call:
     * private final static BigInteger modulus = BigInteger.probablePrime(4096, random); 
     */
    public final static BigInteger modulus = new BigInteger("905514704836430675800761617569269979454323550437336845317887664112195258018199065419744457339938192942270658408641237248268406071710130591695811154553390461950392184521200344723625117080979368260513288204660845526091757172607160064548566603342147003784893199253550317851665803170231774739918374043695869193953772843240482893527649605639678495829284662742374375844253881281545198440138956045496685625573008320540196631904321687977724869353548556009716656870957872097021067002569175907757759961958797805576984560249492954230074470025322911424476657169078576311779874611941383714113485445237228136348539399177823617126754157071066185495335871979865096357941185459815747765520489323361991651379796569949584987167942280346498504061605131038419969930513448499225865573817320273607980775626818081999984265326779940417682443443674070816589322372006231130479669291313966965384883310807286982446739440044648100095052454627019518926183508453145187900010391625221817589731248410165601554660390971161295479758969758188520307199496452991548148179866636729773777464904781349852981520159665263518153184665345019566965891277656452505410652610509342992900756707281787073994266779417195881058655837598182493926893101608750178345996844355066608591785589");

    /**
     * Returns a new set of private keys
     * @return An ArrayList representing the keys. The first element is e and the
     * second element is d
     */
    public static PokerKey generateKeypair()
    {
        PublicKey publicKey;
        PrivateKey privateKey;

        /*
         * Generate e*d = 1 (mod phi(modulus))
         */
        BigInteger e, d, phi;

        phi = modulus.subtract(BigInteger.ONE);

        /* Choose a random value for e */
        while (true)
        {
            /* Make e somewhat small */
            e = new BigInteger(256, random);

            /* If e is not coprime, generate a new one */
            if ((e.gcd(phi)).equals(BigInteger.ONE) == true)
            {
                break;
            }
        }

        /* d = e^-1 (mod modulus) */
        d = e.modInverse(phi);

        ArrayList<BigInteger> pair = new ArrayList<BigInteger>(2);
        pair.add(e);
        pair.add(d);

//        System.out.println(e.multiply(d).mod(phi));
        PokerKey key = new PokerKey(e, d, modulus);
        return key;
    }

    /**
     * Provides a wrapper to the encrypt() function, allowing a user to provide a String
     * @param input The plaintext message
     * @param e The encryption factor
     * @return A BigInteger representing the ciphertext
     */
    public static BigInteger encrypt(String input, BigInteger e)
    {
        return MentalPokerWrapper.encrypt(string2int(input), e);
    }
    
    /**
     * Using the given input and encryption key, enciphers
     * the input and returns the ciphertext.
     * @param input The plaintext message
     * @param e The key to use
     * @return The ciphertext
     */
    public static BigInteger encrypt(BigInteger input, BigInteger e)
    {
        BigInteger ciphertext = input.modPow(e, modulus);
        return ciphertext;
    }

    /**
     * Using the given input and deciphering parameter, decrypts
     * and returns the corresponding plaintext
     * @param input The ciphertext
     * @param d The key to use
     * @return The corresponding plaintext in a BigInteger. Use int2string to convert
     * it to an ASCII message
     */
    public static BigInteger decrypt(BigInteger input, BigInteger d)
    {

        return input.modPow(d, modulus);
    }

    /**
     *  Convert a string into a BigInteger.  The string should consist of
     *  ASCII characters only.  The ASCII codes are simply concatenated to
     *  give the integer.
     * @param str The String to encode
     * @return The corresponding BigInteger
     */
    public static BigInteger string2int(String str)
    {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++)
        {
            b[i] = (byte) str.charAt(i);
        }
        return new BigInteger(1, b);
    }

    /**
     *  Convert a BigInteger into a string of ASCII characters.  Each byte
     *  in the integer is simply converted into the corresponding ASCII code.
     * @param n The integer to encode
     * @return The String that was encoded in the number
     */
    public static String int2string(BigInteger n)
    {
        byte[] b = n.toByteArray();
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < b.length; i++)
        {
            s.append((char) b[i]);
        }
        return s.toString();
    }

    /**
     * A simple test of the mental poker code
     * @param args No arguments used
     */
    public static void main(String[] args)
    {
        PokerKey keys = MentalPokerWrapper.generateKeypair();

        BigInteger e = keys.getPublicExponent();
        BigInteger d = keys.getPrivateExponent();

        PokerKey keys2 = MentalPokerWrapper.generateKeypair();

        BigInteger e2 = keys2.getPublicExponent();
        BigInteger d2 = keys2.getPrivateExponent();

//      System.out.println(e);
//      System.out.println(d);

        BigInteger temp = MentalPokerWrapper.encrypt("HOLA SENOR", e);
        temp = MentalPokerWrapper.encrypt(temp, e2);
        
        temp = MentalPokerWrapper.decrypt(temp, d2);
        temp = MentalPokerWrapper.decrypt(temp, d);
        System.out.println(int2string(temp));

        
    }
}
