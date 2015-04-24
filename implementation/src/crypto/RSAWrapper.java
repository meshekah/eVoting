/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

/**
 *
 * @author stkerr
 */
public class RSAWrapper
{   
    /**
     * Encrypts the given String using the stored key pair. Tt will use 
     * hybrid encryption and return the RSA
     * encrypted symmetric key that was applied to the bytes in messageBuffer
     *
     * @param input The plaintext message
     * @param publickey The public key to use
     * @param messageBuffer The ArrayList to store the symmetric encrypted bytes
     * @param IV The initialization vector to use
     * @return A byte[] of ciphertext
     */
    public static byte[] encrypt(String input, RSAPublicKey publickey, 
            ArrayList<Byte> messageBuffer, byte[] IV)
    {
        return RSAWrapper.encrypt(input.getBytes(), publickey, messageBuffer, IV);
    }

    /**
     * Given an array of encrypted bytes, will decrypt them
     * using the stored key pair and return a String of the results.
     *
     * @param input The RSA encrypted symmetric AES key
     * @param privatekey The private key to be used
     * @param message The AES encrypted byte array
     * @param IV The Initialization vector to use
     * @return A String of the corresponding plaintext
     */
    public static String decryptToString(byte[] input, RSAPrivateKey privatekey, 
            ArrayList<Byte> message, byte[] IV)
    {
        
        return new String(RSAWrapper.decrypt(input, privatekey, message, IV));
    }

    /**
     * Using the supplied plaintext and key pair, encrypts the plaintexts
     * with RSA and returns the RSA encrypted AES key that was used on the
     * message itself
     *
     * @param input The plaintext
     * @param publickey The public key to use
     * @param messageBuffer An ArrayList to contain the AES message (i.e. ciphertext)
     * @param ivBytes The initialization vector to be used (16 bytes)
     * @return The symmetric key under RSA (i.e. RSA(K_Aes))
     */
    public static byte[] encrypt(byte[] input, RSAPublicKey publickey, 
            ArrayList<Byte> messageBuffer, byte[] ivBytes)
    {

        try
        {
            /* Use hybrid encryption */
//            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            byte[] keyBytes = new byte[16]; /* 128 bit */
            new SecureRandom().nextBytes(keyBytes);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher aescipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
            aescipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] cipherText = new byte[aescipher.getOutputSize(input.length)];

            int ctLength = aescipher.update(input, 0, input.length, cipherText, 0);

            ctLength += aescipher.doFinal(cipherText, ctLength);
            
            /* Copy the symmetric key message into the message buffer */
            for(int i = 0; i < cipherText.length; i++)
            {
                messageBuffer.add(new Byte(cipherText[i]));
            }
            
            /* Compute the RSA signature of the symmetric key */
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publickey);
            byte[] rsaResults = cipher.doFinal(keyBytes);
            
            
            
            return rsaResults;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Given the ciphertext and key, decrypts using RSA and returns the
     * corresponding plaintext
     *
     * @param input The AES key under RSA, (i.e. RSA(K_A))
     * @param privatekey The private key to be used
     * @param messageBuffer The array of AES encrypted message i.e. AES(M)
     * @param ivBytes The IV for AES
     * @return The plaintext
     */
    public static byte[] decrypt(byte[] input, RSAPrivateKey privatekey, 
            ArrayList<Byte> messageBuffer, byte[] ivBytes)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privatekey);
            byte[] aesKey = cipher.doFinal(input);
            
            
            Object[] tempmessage = messageBuffer.toArray();
            byte[] message = new byte[tempmessage.length];
            for(int i = 0; i < message.length; i++)
                message[i] = ((Byte)tempmessage[i]).byteValue();
            
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher aescipher = Cipher.getInstance("AES/CTR/NoPadding","BC");
            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            aescipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            
            byte[] results = new byte[aescipher.getOutputSize(message.length)];
            
            int ptLength = aescipher.update(message, 0, messageBuffer.size(), results, 0);
            ptLength += aescipher.doFinal(results, ptLength);
            
            return results;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Using the given input and private key, signs and returns the
     * RSA signed message
     *
     * @param input The message to sign
     * @param privatekey The privatekey used to sign the message
     * @return The message signature
     */
    public static byte[] sign(byte[] input, RSAPrivateKey privatekey)
    {
        try
        {
            Signature signature = Signature.getInstance("SHA1withRSA", "BC");
            signature.initSign(privatekey, new SecureRandom());
            signature.update(input);
            byte[] sigBytes = signature.sign();
            return sigBytes;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Fell through sign()");
        return null;
    }

    /**
     * Using the given signature and public key, tells if the signature
     * is valid
     * @param message The message to check
     * @param signature The signature to check
     * @param publickey The public key being used
     * @return
     */
    public static boolean checkSignature(byte[] message, byte[] signature, RSAPublicKey publickey)
    {
        try
        {
            Signature sig = Signature.getInstance("SHA1withRSA", "BC");
            sig.initVerify(publickey);
            sig.update(message);

            boolean result = sig.verify(signature);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Fell through check()");
        return false;
    }
    
    /**
     * Generates and returns an RSA 2048 bit key pair
     * @return The KeyPair object representing the new key pair
     */
    public static KeyPair genKeyPair()
    {
        try
        {
            KeyFactory       keyFactory = KeyFactory.getInstance("RSA", "BC");
            
            /* Construct a 2048 bit RSA key */
            RSAKeyGenerationParameters params = new RSAKeyGenerationParameters(new BigInteger("7"), new SecureRandom(), 2048, 20);
            RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
            gen.init(params);
            AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();
            
            RSAKeyParameters pub = (RSAKeyParameters)keyPair.getPublic();
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(pub.getModulus(), pub.getExponent());
            
            RSAKeyParameters priv = (RSAKeyParameters)keyPair.getPrivate();
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(priv.getModulus(), priv.getExponent());

            RSAPublicKey pubKey = (RSAPublicKey)keyFactory.generatePublic(pubKeySpec);
            RSAPrivateKey privKey = (RSAPrivateKey)keyFactory.generatePrivate(privKeySpec);
        
            return new KeyPair(pubKey, privKey);
            
        }
        catch(Exception e)
        {
            System.err.println("RSA Key Generation failed!");
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Returns a String ready to be sent over the network of the
     * given data
     * @param ciphertext The AES encrypted ciphertext
     * @param symmetricalKey The AES key encrypted with RSA
     * @param IV The initialization vector in use
     * @return 
     */
    public static String getEncryptionString(byte[] ciphertext, 
        byte[] symmetricalKey, byte[] IV)
    {
        String message = "CIPHERTEXT=";
        message += new BigInteger(ciphertext).toString();
        
        message += "&SYMKEY=";
        message += new BigInteger(symmetricalKey).toString();
        
        message += "&IV=";
        message += new BigInteger(IV).toString();
        
        return message;
    }
    
    /**
     * A basic test of the RSAWrapper API
     * @param args Should not be used
     */
    public static void main(String[] args)
    {
        KeyPair keys = RSAWrapper.genKeyPair();
                
        byte[] sig = RSAWrapper.sign("HELLO WORLD".getBytes(), 
                (RSAPrivateKey)keys.getPrivate());
        
        System.out.println(RSAWrapper.checkSignature("HELLO WORLD".getBytes(), 
                sig, (RSAPublicKey)keys.getPublic()));
//        byte[] IV = new byte[16];
//        new SecureRandom().nextBytes(IV);
//        

//        
//        ArrayList<Byte> message = new ArrayList<Byte>();
//        
//        byte[] results = encrypt("HASDKLHSJKNDSV:CKN:UDH:ASDH:HOIJASHD:LJKASDHLUI", (RSAPublicKey)keys.getPublic(), message, IV);
//        
//        Object[] temp = message.toArray();
//        byte[] symkey = new byte[message.size()];
//        for(int i = 0; i < symkey.length; i++)
//            symkey[i] = ((Byte)temp[i]).byteValue();
//        
////        results = decrypt(results, (RSAPrivateKey)keys.getPrivate(), message, IV);
//        
//        String output = decryptToString(results, (RSAPrivateKey)keys.getPrivate(), message, IV);
//        System.out.println(output);
//        BigInteger temp2 = new BigInteger(results);
//        System.out.println(RSAWrapper.getEncryptionString(results, symkey, IV));
    }
}