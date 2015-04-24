/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package crypto;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 *
 * @author stkerr
 */
public class PokerKey implements RSAPublicKey, RSAPrivateKey
{
    private BigInteger e, d, m;
    
    /**
     * Constructs a new mental poker key pair. This implements the RSA key
     * interfaces for programming convenience as well.
     * @param e The e key to use
     * @param d The d key to use
     * @param m The public modulus to use
     */
    public PokerKey(BigInteger e, BigInteger d, BigInteger m)
    {
        this.e = e;
        this.d = d;
        this.m = m;
    }
    
    /**
     * Returns the user's e key for mental poker
     * @return The e key for mental poker
     */
    public BigInteger getPublicExponent()
    {
        return e;
    }
    
    /**
     * Returns the user's d key for mental poker
     * @return The d key for mental poker
     */
    public BigInteger getPrivateExponent()
    {
        return d;
    }
    
    /**
     * Get the encoding
     * @return null
     */
    public byte[] getEncoded()
    {
        return null;
    }
    
    /**
     * Get the format
     * @return null
     */
    public String getFormat()
    {
        return null;
    }
    
    /**
     * Get the algorithm
     * @return "Poker"
     */
    public String getAlgorithm()
    {
        return "Poker";
    }

    /**
     * Return the modulus for this set of poker keys
     * @return The public modulus
     */
    public BigInteger getModulus()
    {
        return m;
    }
}
