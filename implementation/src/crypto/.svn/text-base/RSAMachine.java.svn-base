
package crypto;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An abstraction layer to utilize RSA
 */
public class RSAMachine 
{
    private BigInteger ciphertext, // AES(M)
            symkey, // RSA(K_aes)
            iv; // IV

    private ArrayList<Byte> messageBuffer; // AES(M)
           
    /**
     * Construct a new RSA machine and initialize it
     */
    public RSAMachine()
    {
        /* Get a random IV */
        byte[] IV = new byte[16];
        new SecureRandom().nextBytes(IV);
        
        iv = new BigInteger(IV);
        
        messageBuffer = new ArrayList<Byte>();
    }
    
    /**
     * Given a string, loads the RSA machine with the values
     * @param contents A string of the form CIPHERTEXT=...&SYMKEY=...&IV=..
     * @return A boolean representing if the load succeeded
     */
    public boolean loadMachine(String contents)
    {
        try
        {
            StringTokenizer tokenizer = new StringTokenizer(contents, "=&");

            /* CIPHERTEXT */
            tokenizer.nextToken();

            /* CIPHERTEXT DATA */
            ciphertext = new BigInteger(tokenizer.nextToken());

            /* SYMKEY */
            tokenizer.nextToken();

            /* SYMKEY DATA */
            symkey = new BigInteger(tokenizer.nextToken());

            /* IV */
            tokenizer.nextToken();

            /* IV DATA */
            iv = new BigInteger((tokenizer.nextToken()));

            return true;
        }
        catch(Exception e)
        {
            return false;
        }
        
    }
    
    /**
     * Loads the machine with the given IV
     * @param IV The IV to use
     */
    public void loadIV(BigInteger IV)
    {
        iv = IV;
    }
    
    /**
     * Prints out the contents of the RSA machine
     * @return 
     */
    @Override
    public String toString()
    {
        return RSAWrapper.getEncryptionString(ciphertext.toByteArray(), 
                symkey.toByteArray(),
                iv.toByteArray());
    }
    
    /**
     * Encrypts a string using RSA. Leverages RSAWrapper
     * @param plaintext The plaintext to encrypt
     * @param publickey The RSA public key
     * @return The ciphertext
     */
    public BigInteger encrypt(String plaintext, RSAPublicKey publickey)
    {
        byte[] array = RSAWrapper.encrypt(plaintext.getBytes(), publickey, 
                messageBuffer, iv.toByteArray());
        symkey = new BigInteger(array);
        
        Object[] temp = messageBuffer.toArray();
        byte[] bytes = new byte[temp.length];
        for(int i = 0; i < temp.length; i++)
        {
            bytes[i] = ((Byte)(temp[i])).byteValue();
        }
        
        ciphertext = new BigInteger(bytes);
        return ciphertext;
    }
    
    /**
     * Decrypts the given ciphertext
     * @param ciphertext The ciphertext to decrypt
     * @param privatekey The RSA private key
     * @return The plaintext
     */
    public BigInteger decrypt(BigInteger ciphertext, RSAPrivateKey privatekey)
    {
        byte[] temp = ciphertext.toByteArray();
        for(int i = 0; i < temp.length;i++)
        {
            messageBuffer.add(temp[i]);
        }
        byte[] array = RSAWrapper.decrypt(symkey.toByteArray(), privatekey, 
                messageBuffer, iv.toByteArray());
        return new BigInteger(array);
    }
    
    /**
     * A basic test of the RSA Machine API
     * @param args Should not be used
     */
    public static void main(String[] args)
    {
        KeyPair keys = RSAWrapper.genKeyPair();
        
        String contents = "CIPHERTEXT=7919291552096823432517663710851008256626741865202216835186729317988824024408529669462185039746575725121266076719100936521857964510650549939858581735328894316207007348043252292074867426610135803171418393860714783017801128190906900909931173144565706718761644489198028673576699877996494016777912029377367322894723999685930309566820855505417812461333742463794712553798242071087105460577850688657699584232690455313699327359005859785823319813334752027578962555737232759455258323102520743046386543543080297564468104707519793446645248831573684470875384685988643502168130049920077616898303630830831000433786910002059631924422&SYMKEY=70064439642346864456982956244497365214691643931462485651224310309612371326962175186596778252182978700605756500601&IV=55996177264106600398263686063157115469";
        RSAMachine rsa = new RSAMachine();
        rsa.loadMachine(contents);
        System.out.println(rsa.toString());
        
        BigInteger c = rsa.encrypt("HI", (RSAPublicKey)keys.getPublic());
        System.out.println(rsa.decrypt(c, (RSAPrivateKey)keys.getPrivate()));
        
        
    }
}
