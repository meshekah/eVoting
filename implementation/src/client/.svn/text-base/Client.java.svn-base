package client;

import client.GUI.Interface;
import client.networking.ListenClient;
import crypto.MentalPokerWrapper;
import crypto.PokerKey;
import crypto.RSAMachine;
import crypto.RSAWrapper;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;

import networking.Message;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.bouncycastle.util.BigIntegers;

/**
 * The client class for the voting scenario
 */
public class Client extends Interface
{
    /**
     * The RSA machine for this client
     */
    private RSAMachine rsa;

    /**
     * The hostname of the server we are using
     */
    private String globalHost;
    
    /**
     * The port number of the server we are using
     */
    private int globalPort;
    
    /**
     * The RSA keypair we are using for encryption
     */
    private KeyPair rsaKeyPair;
    
    /**
     * An instance of the ListenClient to talk to the server
     */
    private ListenClient listenClient;
    
    /**
     * Our keypair for mental poker
     */
    private PokerKey pokerKeyPair;
    
    /**
     * Our ID number we are using for indentification
     */
    private BigInteger myID;// = new BigInteger("65863996489775819341020548724156370838813703885341144014534655941904355204313");
    
    /**
     * The user's name
     */
    private String username;
    
    /**
     * The user's SSN
     */
    private int userSSN;
    
    /**
     * The filepath of the ID number file
     */
    private String idFile;
    
    /**
     * The signed receipt of our vote from CTF
     */
    private BigInteger voteReceipt;
    
    /**
     * The signed receipt for the user's registration
     */
    private BigInteger regReceipt;
    
    /**
     * The last cipherblock sent
     */
    private String lastCipherblock;
    
    /**
     * Default constructor. Creates a set of keypairs for the client
     * @param files The file storing the list of ID numbers
     */
    public Client(String files)
    {
        rsa = new RSAMachine();
        idFile = files;
        genKeyPair();
    }

    /**
     * Checks the validity of a voter and if he is eligible to vote
     * @param name The voter's name
     * @param SSN The voter's SSN
     * @return If the user is eligible or not
     */
    public boolean checkEligibility(String name, int SSN)
    {
        /* Send the payload out */
        String payload = "CHECKVALID?NAME=" + name + "&SSN=" + SSN;
        send(payload);

        /* Receive the response */
        String response = receive();

        /* Parse the response */
        String status = response.substring(response.indexOf('?'));

        if (status.contains("FAIL"))
        {
            System.out.println(status);
            return false;
        }

        return true;
    }

    /**
     * Using the given encrypted ID number, communicate with CTF and obtain
     * an ID number
     * @param ciphertext The ID encrypted using the CTFs mental poker key
     */
    public void enrollID(BigInteger ciphertext)
    {
//        System.out.println("Attempting with " + ciphertext);
        
        /* Make the payload */
        String payload = "IDENROLL?ID=" + ciphertext.toString();

        /* Send the payload */
        send(payload);

        /* Receive the servers response */
        String response = receive();

        /* Parse out the server's response */
        String commandName = response.substring(0, response.indexOf('?'));
        if (!commandName.equals("IDRESPONSE"))
        {
            System.out.println("Did not get a proper ID response!");
            return;
        }


        String status;
        if (response.indexOf('&') < 0 || response.contains("FAIL"))
        {
            /* Invalid response */
            status = response.substring(response.indexOf('?') + 1);
            System.out.println(status);
            return;
        }

        /* Parse out the ID number */
        String ID = response.substring(response.indexOf('&') + 1 + "ID=".length());

//        System.out.println(ID);
        BigInteger results = new BigInteger(ID);
        myID = MentalPokerWrapper.decrypt(results, pokerKeyPair.getPrivateExponent());
    }

    /**
     * Registers a new client
     * @param name The user's name
     * @param SSN The user's SSN
     */
    public void registerClient(String name, int SSN)
    {
        username = name;
        userSSN = SSN;
        /* Generate a mental poker keypair */

        /* Send the server a register request with our name, SSN, and protocol string */
        String payload = "REGISTERREQ?NAME=" + name + "&SSN=" + SSN;
//        System.out.println("Sending: " + payload);
        send(payload);

        /* Receive the server's response */
        String response = receive();
        String status = response.substring(response.indexOf('?') + 1);
        if (status.contains("FAIL"))
        {
            System.out.println(status);
            System.out.println("Registration failed");
            return;
        }
        else
        {
            /* Store the CTF register receipt */
            String value = response.substring(response.indexOf('&')+"RECEIPT=".length()+1);
            regReceipt = new BigInteger(value);
//            System.out.println("Registration receipt" + value);
        }
        
        /* Read in the list of valid ID numbers now */
        ArrayList<BigInteger> idList = readIDNumbers(idFile);

        /* Randomly select an ID number to use */
        SecureRandom rand = new SecureRandom();
        BigInteger myId = idList.get(rand.nextInt(idList.size()));

        /* Encrypt the randomly selected ID => E_cli(E_ctf(ID)) */
        BigInteger results = MentalPokerWrapper.encrypt(myId, pokerKeyPair.getPublicExponent());
        
        /* Send out the ID enrollment now */
        enrollID(results);

//        System.out.println("Using ID number: " + myID);

        /* Disconnect the session to get a new session ID */
        resetSession();

    }

    /**
     * Sends the necessary information to the CTF to allow votes to be
     * decrypted
     */
    public void revealVote()
    {
        /* Parse the private key out */
        RSAPrivateKey key = (RSAPrivateKey) rsaKeyPair.getPrivate();
        String modulus = key.getModulus().toString();
        String exponent = key.getPrivateExponent().toString();
        
        /* Construct the payload */
        String payload = "REVEALVOTE?ID=" + myID + "&EXPONENT=" + exponent + "&MODULUS=" + modulus;

        /* Send the payload */
        send(payload);
        
        /* Receive the server's response */
        String response = receive();
        String status = response.substring(response.indexOf('?') + 1);
        if (status.contains("FAIL"))
        {
            System.out.println(status);
            System.out.println("Reveal failed");
            return;
        }
    }

    
    /**
     * Actually submits the vote to the CTF as the tuple I, E_k(I,v)
     * @param vote The value for v
     */
    public void submitVote(String vote)
    {
        /* Encrypt the user's choice */
        String plaintext = myID + ":" + vote;
        RSAMachine rsa = new RSAMachine();
        rsa.encrypt(plaintext, (RSAPublicKey) rsaKeyPair.getPublic());
        
        lastCipherblock = rsa.toString();
        
        /* Construct the tuple as a string */
        String payload = "SUBMITVOTE?ID=" + myID + "&" + rsa.toString();
//        System.out.println(payload);

        /* Send the String out */
        send(payload);

        /* Wait for a signed confirmation */
        String response = receive();
        
        try
        {
            StringTokenizer tokens = new StringTokenizer(response, "?&");
            String OTP = "", receipt = "", status = "FAIL";
            
            tokens.nextToken(); // ID:SUBMITRESPONSE
            
            if(response.contains("STATUS") && tokens.hasMoreTokens())
                status = tokens.nextToken(); // STATUS={...}

            /* Is there a failure status encoded? */
            if (status.contains("FAIL"))
            {
                if(status.contains("Sorry, but the ID you're using has been used by another user."))
                {
                    if(response.contains("OTP") && tokens.hasMoreTokens())
                    {
                        String temp = tokens.nextToken("&="); // OTP
                        OTP = tokens.nextToken("&="); // OTP #
//                        System.out.println(OTP);
                    }
                    myID = new BigInteger(OTP); 
                    /* Use the OTP value we received */
                    submitVote(vote);
                }
                else
                {
                    System.out.println(status);
                    System.out.println("Submit failed for unknown reasons");
                    return;
                }
            }
            else
            {
                if(response.contains("RECEIPT") && tokens.hasMoreTokens())
                {
                    receipt = response.substring(response.indexOf("RECEIPT=")+"RECEIPT=".length());
//                    System.out.println("Receipt: " + receipt);
                }
                
                /* No failure, so record the response */
                voteReceipt = new BigInteger(receipt);
            }
        }
        catch(Exception e){e.printStackTrace();}
        
        

        
    }

    /**
     * If the CTF had counted the user's vote incorrectly or the user wants
     * to protest for some other reason, he will call this to send the CTF
     * information proving his real vote.
     * @param vote The vote that the user wants to make.
     */
    public void protestVote(String vote)
    {
        /* Parse out the user's private key */
        RSAPrivateKey key = (RSAPrivateKey) rsaKeyPair.getPrivate();
        String modulus = key.getModulus().toString();
        String exponent = key.getPrivateExponent().toString();

        /* Construct the payload */
        String payload = "PROTEST?"
                + "ID=" + myID
                + "&EXPONENT=" + exponent
                + "&MODULUS=" + modulus
                + "&" + lastCipherblock
                + "&VOTERCPT=" + voteReceipt;

//        System.out.println("Sending " + payload);

        /* Send the payload */
        send(payload);

        /* Wait for a confirmation */
        String response = receive();
//        System.out.println("Protest response: " + response);
    }

    /**
     * If a user decides to change his vote, this function is called. This will
     * cause keypairs to be regenerated and a new vote to be made
     * @param vote The new vote
     */
    public void changeVote(String vote)
    {
        /* Create a new vote */
        String plaintext = myID + ":" + vote;
        
        rsa = new RSAMachine();
        rsa.encrypt(plaintext, (RSAPublicKey) rsaKeyPair.getPublic());
        lastCipherblock = rsa.toString();
        
        /* Construct the revision payload */
        String payload = "CHANGEREQ?"
                + "ID=" + myID
                + "&"
                + rsa.toString();

        /* Send the payload */
        send(payload);

        /* Wait for a confirmation */
        String response = receive();
        
        try
        {
            StringTokenizer tokens = new StringTokenizer(response, "?&");
            String OTP = "", receipt = "", status = "FAIL";
            
            if(response.contains("STATUS") && tokens.hasMoreTokens())
                status = tokens.nextToken();

            /* Is there a failure status encoded? */
            if (status.contains("FAIL"))
            {
                System.out.println(status);
                System.out.println("Submit failed for unknown reasons");
                return;
            }
            else
            {
                if(response.contains("RECEIPT") && tokens.hasMoreTokens())
                    receipt = tokens.nextToken();
                
                /* No failure, so record the response */
                voteReceipt = MentalPokerWrapper.string2int(receipt);
            }
        }
        catch(Exception e){e.printStackTrace();}

    }

    /**
     * Using the given file, parse and return all the ID numbers
     * @param filename The file containing all a new ID number on each line
     * @return The list of ID numbers
     */
    public ArrayList<BigInteger> readIDNumbers(String filename)
    {
        try
        {
            ArrayList<BigInteger> results = new ArrayList<BigInteger>();
            
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String strLine;
            
            while ((strLine = br.readLine()) != null)
            {
                BigInteger temp = new BigInteger(strLine);
                results.add(temp);
            }
            br.close();
            in.close();
            fstream.close();
            return results;
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets the user's RSA key pair
     *
     * @param pair The new key pair to use
     */
    public void setRSAKeyPair(KeyPair pair)
    {
        rsaKeyPair = pair;
    }

    /**
     * Returns the user's stored RSA key pair
     *
     * @return The stored keypair
     */
    public KeyPair getRSAKeyPair()
    {
        return rsaKeyPair;
    }

    /**
     * Returns the user's stored poker key pair
     * @return The stored poker key pair
     */
    public PokerKey getPokerKey()
    {
        return pokerKeyPair;
    }

    /**
     * Sets the user's poker key pair
     * @param key The new keypair for mental poker
     */
    public void setPokerKey(PokerKey key)
    {
        this.pokerKeyPair = key;
    }

    /**
     * Generates a key pair for this user to use. Note that this function
     * will take some time.
     */
    public void genKeyPair()
    {
        rsaKeyPair = RSAWrapper.genKeyPair();
        pokerKeyPair = MentalPokerWrapper.generateKeypair();
    }

    /**
     * Starts the networking for the given host and port
     *
     * @param host The name of the host to connect to, such as 127.0.0.1 or www.google.com
     * @param port The port of the host to connect to
     */
    public void startNetworking(String host, int port)
    {
        globalHost = host;
        globalPort = port;
        listenClient = new ListenClient();
        listenClient.startNetworking(host, port);
    }

    /**
     * Restarts networking for the client so as to acquire a new session ID
     */
    public void resetSession()
    {
        listenClient.cease();
        listenClient = new ListenClient();
        listenClient.startNetworking(globalHost, globalPort);
    }

    /**
     * Creates a message with the given String
     * and enqueues it into the message queue. The
     * underlying client class will format it with
     * a session ID properly.
     *
     * @param message The contents of the message to send
     */
    public void send(String message)
    {
        listenClient.send(new Message(message, listenClient.getID()));
    }

    /**
     * Removes and returns the contents of the first
     * message in the received message queue. Note that
     * if the message queue is empty, this call will block.
     *
     * @return The contents of the message that was received
     */
    public String receive()
    {
        return listenClient.receive().getContents();
    }

    /**
     * Starts the client for the voting scenario
     * @param args Any command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
     
        Client client = new Client("/Users/stkerr/NetBeansProjects/id.txt");
        client.startGUI();
        client.genKeyPair();
        client.startNetworking("127.0.0.1", 4444);

        //        client.registerClient("John Smith", 123456789);
//        Thread.sleep(3000);
//        client.submitVote("John Smith");
//        Thread.sleep(3000);
//        client.revealVote();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
