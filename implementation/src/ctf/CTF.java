/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ctf;

import Database.DB;
import crypto.MentalPokerWrapper;
import crypto.PokerKey;
import crypto.RSAMachine;
import crypto.RSAWrapper;
import ctf.networking.ListenServer;
import ctf.GUI.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.StringTokenizer;
import networking.Message;
import org.apache.commons.lang.ArrayUtils;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author Mohammed
 */
public class CTF extends Interface
{
    private ListenServer server;

    //This is the local database object that links to the CTF database.
    private DB database;

    //This is the path to the IDs file where all the possible IDs will be stored in an encrypted form using the CTF mental Poker Encryption Key.
    /**
     *
     */
    public String IDsFilePath;

    //This holds the CTF's Mental Poker key pair.
    private PokerKey pKey;

    //This heolds the CTF's RSA key pair.
    private KeyPair RSAKeyPair;

    //This holds the CTF global dates for the start of the registeration period, end of the registration period and the end of the voting period.
    private Date RegStart, RegDeadline, VotingDeadline;
    
    //This holds the list of seconday IDs that are saved at the CTF side to resolve the ID conflicts.
    private ArrayList<BigInteger> secondaryIDs;
    
    // The list of possible candidates
    private ArrayList<String> candidates;

    //Objects used throughout the code.
    private static FileInputStream fstream = null;
    private static DataInputStream in = null;
    private static BufferedReader br = null;
    
    /**
     * The CTF constructor when initializes all the internal variables at the CTF side.
     * @param EVotersPath - the path to the eligible voters file to be added to the database.
     * @param secondaryPath - the path of the secondary list of the ID numbers that will be used to resolve the ID conflict.
     * @param RegStart - the date of the start of the registration period.
     * @param RegDeadline - the date of the deadline of the registration period.
     * @param VotingDeadline - the voting period deadline
     * @throws Exception - if any errors occur.
     */
    public CTF(String EVotersPath, String secondaryPath,
            Date RegStart, Date RegDeadline, Date VotingDeadline) throws Exception {
        
        database = new DB();
        database.ClearDB();
        database.AddEligVotersToDB(EVotersPath);

        secondaryIDs = new ArrayList<BigInteger>();
        secondaryIDs = CTF.readIDNumbers(secondaryPath);
            
        //TODO:do we have to type the keys manually here.
        pKey = new PokerKey(new BigInteger("102305074691832500652868793325658345977645070217392683577866422209638437167937"),
                new BigInteger("657230828025924251939170402521181477778124729833912333737194912341322993126033131716575155573271494800001916866611392755648201660337633525124416103130691699314248799955107245886734851482085425756373047123225817705842382673059168351868980786552835511033270072100163682878432820431999605205070784377005630057969010199349831583669324255011032037731020352913308668012653847473022511645727628759574432475425979019749404467964110913346513961737715204184343568429629077598860731014859366868735882949083933540402183731138110075818697231318272099075879670493446590331132141760600809988684633060519344978436953909425551837422650477586185931784050133532503796787275707573253328429197063408653101509399219982631569627549684428689703194703349585383110021985524901504752688553670622551716568862529822612847969527018346621497417840377985212995373309828405908983329238867051241354316041468116646009783218986598490808178146513136109076407190821494279057364258049131632715134546874446331855024756840182609954241737819896958529362403358431044396199894485480806536440669339742235382357541306153121029682811720442277175672606542623551893744092835677910833079488018149721407291009588195801816373653445964783300082610316788160680087910938266991425983779133"),
                MentalPokerWrapper.modulus);
        
        
        this.RegStart = RegStart;
        this.RegDeadline = RegDeadline;
        this.VotingDeadline = VotingDeadline;
        
        //TODO:I think we should read these fromt he CTF GUI.
        candidates = new ArrayList<String>();
        candidates.add("John Smith");
        candidates.add("Gordon Freeman");
        candidates.add("Jackie Burkhart");
        candidates.add("Michael Bluth");
        candidates.add("Angela Martin");
    }

    /**
     * Initializes the RSA key pair for the CTF.
     * Putting this in a separate function allows GUI to load faster
     * @throws Exception
     */
    public void init() throws Exception
    { RSAKeyPair = RSAWrapper.genKeyPair(); }
        
    /**
     * Adds a candidate to the candidate list.
     * @param candidate - The candidate to be added.
     */
    public void addCandidate(String candidate)
    { candidates.add(candidate); }
    
    /**
     * This is called to get the list of secondary IDs that are used to resolve the ID conflict.
     * @return - an ArrayList of the list of secondary IDs.
     */
    public ArrayList<BigInteger> getSecondaryIDs()
    { return secondaryIDs; }
    
    /**
     * Using the given file, parse and return all the ID numbers
     * @param filename The file containing all a new ID number on each line
     * @return The list of ID numbers
     */
    public static ArrayList<BigInteger> readIDNumbers(String filename) throws Exception
    {
        try {
            if (filename == null)
                throw new Exception("The file name has not been supplied");

            ArrayList<BigInteger> results = new ArrayList<BigInteger>();
            fstream = new FileInputStream(filename);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

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
        catch (Exception e) {
            if (br != null)
                br.close();
            if (in != null)
                in.close();
            if (fstream != null)
                fstream.close();
            throw new Exception(e.getMessage());
        }
    }
    
    /**
     * Changes the start of the registration period.
     */
    public void startRegistrationPeriod()
    {
        timeLabel.setText("Current Period: Registration");
        System.out.println("Registration period started.");
        Calendar cal = Calendar.getInstance();
        
        RegStart = new Date();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        RegDeadline = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        VotingDeadline = cal.getTime();
    }
    
    /**
     * Starts the voting period
     */
    public void startVotingPeriod()
    {
        timeLabel.setText("Current Period: Voting");
//        System.out.println("Voting period started.");
        Calendar cal = Calendar.getInstance();
        
        RegDeadline = new Date();
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        RegStart = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 5);
        VotingDeadline = cal.getTime();
        
        try
        {
            byte[] sig = OnPubRegVotersFile("/Users/stkerr/NetBeansProjects/regusers.txt");
//            System.out.println("Registered voters file signature: " + new String(sig));
        }
        catch(Exception e)
        { e.printStackTrace(); }
        
    }


    /**
     * Starts the reveal period
     */
    public void startRevealPeriod()
    {
//        System.out.println("Reveal period started.");
        timeLabel.setText("Current Period: Reveal");
        
        Calendar cal = Calendar.getInstance();
        
        VotingDeadline = new Date();
        
        cal.add(Calendar.DAY_OF_MONTH, -5);
        RegStart = cal.getTime();
        RegDeadline = cal.getTime();
    }
    
    /**
     * @return Returns the Registration deadline.
     */
    public Date getRegDeadline() { return RegDeadline; }

    /**
     * Gets the start of the registration period.
     * @return Date - the start of the registration period.
     */
    public Date getRegStart() { return RegStart; }

    /**
     * Gets the deadline of the voting period.
     * @return Date - deadline of the voting period.
     */
    public Date getVotingDeadline() { return VotingDeadline; }
    
    /**
     * This methods runs offline and generate the identifiers, encrypts them under the CTF Pohlig-Hellman
     *  encryption key and then finally stores them in a file.
     * The number of identifiers is totally based on the number of registered voters in the system.
     * @param num - The number of registered voters in the system.
     * @param path - The path of the file where the identifiers will be stored.
     * @throws Exception - if an error occurs of the arguments has not been supplied.
     */
    public void generateIDs(int num, String path) throws Exception {
        try {
            //Check if the path has been supplied
            if (path == null) {
                throw new Exception("File Path has not been supplied");
            }
            this.IDsFilePath = path;

            //Calculate the valure of 2^80 and multiply it with num.
            BigInteger I_num = BigInteger.ONE.add(BigInteger.ONE);
            I_num.pow(5);
            I_num.multiply(BigInteger.valueOf(num));

            //Open and prepare the file.
            Formatter writer1 = new Formatter(path);

            String plainIDsPath = "PlainIDs.txt";
            Formatter writer2 = new Formatter(plainIDsPath);
            
            //Generate a random number, encrypt it and then store it in a file.
            BigInteger count = BigInteger.ONE;
            BigInteger temp = null;
            BigInteger tempCipher = null;
            while (count.compareTo(I_num) != 0) {
                //generate the random identifier.
                temp = new BigInteger(256,new SecureRandom());

                //writing it to the decrypted format
                writer2.format("%s\r\n", temp.toString());

                //encrypt it.
                tempCipher = MentalPokerWrapper.encrypt(temp, pKey.getPublicExponent());

                //write it to the file.
                writer1.format("%s\r\n", tempCipher.toString());

                //Increment the counter.
                count.add(BigInteger.ONE);
            }
            writer1.close();
            writer2.close();

            //Save all the IDs in the database.
            database.AddIDsToDB(plainIDsPath);

            File f = new File(plainIDsPath);
            f.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path
     * @return
     * @throws Exception
     */
    public byte [] OnPubRegVotersFile(String path) throws Exception {
        //Extract all the regsitered voters from the data and store their details in the file
        database.CreateRegVotersFile(path);

        //Opening the file.
        if (path == null)
            throw new Exception("File Path has not been supplied");
        File rVoters = new File(path);

        if (!rVoters.exists())
            throw new Exception("File cannot be found");
        if (!rVoters.isFile())
            throw new Exception("The path doean't point to a file");
        if (!rVoters.canRead())
            throw new Exception("The file at " + path + " cannot be read");

        //Reading the contents of the file
        FileInputStream fStream = new FileInputStream(rVoters);
        byte [] fileContent = new byte[(int)rVoters.length()];
        if ( rVoters.length() != fStream.read(fileContent))
            throw new Exception("Problem in Reading the Registered voters file");
        
        //TimeStamping the file.
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm aaa");
        byte [] sig_body;
        byte [] deli = {0,0,0}; //This is the deliminitor detween the ID and the Encrypted Vote.
        sig_body = ArrayUtils.addAll(fileContent,deli);
        sig_body = ArrayUtils.addAll(sig_body, dateFormat.format(new Date().getTime()).getBytes());

        //Signing the content of the file and returning the signature.
        byte [] signature = RSAWrapper.sign(sig_body, (RSAPrivateKey) this.RSAKeyPair.getPrivate());

        //Appending the Signature to the regsitered voters file.
        FileWriter writer = new FileWriter(rVoters,true);
        writer.write("\r\n\r\n"+new String(signature));
        
        //returning the signature to the caller
        return signature;
        
    }

    /**
     * This methods runs offline and generate the identifiers, encrypts them under the CTF Pohlig-Hellman
     *  encryption key and then finally stores them in a file.
     * The number of identifiers is totally based on the number of registered voters in the system.
     *  The probability that and identifier can occur should be roughly 1/2^128, and a collision to
     *  occur is 1/2^80.
     * @param num - The number of registered voters in the system.
     * @param path - The path of the file where the identifiers will be stored.
     * @param secondpath The secondary list of ID numbers
     * @throws Exception
     */
    public void generateIDsTest(int num, String path, String secondpath) throws Exception {
        try {
            //Check if the path has been supplied
            if (path == null) {
                throw new Exception("File Path has not been supplied");
            }
            this.IDsFilePath = path;

            //Calculate the valure of 2^80 and multiply it with num.
            BigInteger I_num = BigInteger.ONE.add(BigInteger.ONE);
            I_num = I_num.pow(6);
            I_num = I_num.multiply(BigInteger.valueOf(num));
            I_num = I_num.multiply(new BigInteger("2")); 

            //Open and prepare the file.
            Formatter writer1 = new Formatter(path);
            Formatter writer3 = new Formatter(secondpath);
            
            String plainIDsPath = "PlainIDs.txt";
            Formatter writer2 = new Formatter(plainIDsPath);

            //Generate a random number, encrypt it and then store it in a file.
            BigInteger count = BigInteger.ONE;
            BigInteger temp = null;
            BigInteger tempCipher = null;
            while (count.compareTo(I_num) < 0) {
                //generate the random identifier.
                temp = new BigInteger(256,new SecureRandom());

                if(count.mod(new BigInteger("2")).equals(BigInteger.ONE))
                {
                    //Increment the counter.
                    count = count.add(BigInteger.ONE);
                
                    writer3.format("%s\r\n", temp.toString());
                    continue;
                }
                
                //writing it to the decrypted format
                writer2.format("%s\r\n", temp.toString());

                //encrypt it.
                tempCipher = MentalPokerWrapper.encrypt(temp, pKey.getPublicExponent());

                //write it to the file.
                writer1.format("%s\r\n", tempCipher.toString());

                //Increment the counter.
                count = count.add(BigInteger.ONE);
            }
            writer1.close();
            writer2.close();
            writer3.close();

            //Save all the IDs in the database.
            database.AddIDsToDB(plainIDsPath);

            File f = new File(plainIDsPath);
            f.delete();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts and configures all the networking for this server
     * and binds it to a hostname and port number.
     *
     * @param host The hostname that this server should bind to
     * @param port The port that this server should listen to
     */
    public void startNetworking(String host, int port)
    {
        server = new ListenServer(host, port);
        server.start();
    }

    /**
     * This removes the first message from the incoming
     * message queue. This call will block and wait if there
     * is no message in the queue.
     *
     * @return The front of the message queue.
     */
    public Message receive()
    {
        return server.receive();
    }

    /**
     * Enqueues a message to the given client. This will
     * not necessarily send it right away, but place it
     * in the queue of messages and returns.
     *
     * @param message The contents of the message to send
     * @param ID The client ID to send this message to
     */
    public void send(String message, int ID)
    {
        server.send(new Message(message, ID));
    }

    /**
     * This method is called by the CTF when it receives a voter registration request.
     * @param SSN - the SSN of the voter requesting to register for voting
     * @return true - if the registration was successful and,
     * @throws Exception
     */
    public byte [] OnRegReq(String SSN) throws Exception {
            if (SSN == null)
                throw new Exception("The voter's SSN has not been supplied");
            if (database.RegVoter(SSN)) {
                //Creating the body of the receipt.
                byte [] receipt_body;
                byte [] deli = {0,0,0}; //This is the deliminitor detween the ID and the Encrypted Vote.
                receipt_body = ArrayUtils.addAll(SSN.getBytes(),deli);
                receipt_body = ArrayUtils.addAll(receipt_body, new Date().toString().getBytes());
                return RSAWrapper.sign(SSN.getBytes(), (RSAPrivateKey) RSAKeyPair.getPrivate());
            }
            else
                return null;
    }

    /**
     * This is called by the CTF when sending the list of identifiers to the client. It first checks that the user has registered.
     * @param SSN - the user's SSN who wants to receive the list of Is.
     * @return The path to the file of identifiers list, if the user is registered.
     */
    public String SendingIs(String SSN) {
        try {
            if (SSN == null)
                throw new Exception("The user's SSN has not been supplied");

            if (!database.IsReg(SSN))
                throw new Exception("The user with SSN = " + SSN + " is not registred");
            return this.IDsFilePath;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This is called when the client sends a request to the CTF to decrypt the double encrypted ID.
     * @param EncID - the double encrypted identifier.
     * @return The decryption of the ID.
     */
    public BigInteger OnIDDecReq(BigInteger EncID) {
        try {
            if (EncID == null)
                throw new Exception("The encrypted ID has not been supplied");
//            System.out.println("Decrypting " + EncID);
            return MentalPokerWrapper.decrypt(EncID, pKey.getPrivateExponent());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    /**
     * This is called by the CTF when it receives a request to record a vote for a given voter.
     * @param id - The ID of the voter.
     * @param cipherpackage
     * @return The receipt of the vote as a signature of id||EncVote
     * @throws Exception
     */
    public byte [] OnReceivingVote(BigInteger id, String cipherpackage) throws Exception {
        //Check if the data has been supplied
        if (id == null || cipherpackage == null)
            throw new Exception("The ID or the vote has not been supplied");

        //Check if the ID is valid
        if (!database.IsValidID(id))
            throw new Exception("The ID is not valid.");

        //Check if there is any conflic in the supplied ID
        if (database.IsIDDupl(id)) {
            System.out.println("The ID has been used by another voter.");
            return null;
        }

        //Adding the vote to the database.
        database.addVote(id, cipherpackage);

//        System.out.println("Signing with " + cipherpackage);

        //Creating the body of the receipt.
        byte [] receipt_body;
        byte [] deli = {0,0,0}; //This is the deliminitor detween the ID and the Encrypted Vote.
        receipt_body = ArrayUtils.addAll(id.toByteArray(),deli);
        receipt_body = ArrayUtils.addAll(receipt_body, cipherpackage.getBytes());

        //Signing the receipt body.
        byte [] receipt;
        receipt = RSAWrapper.sign(receipt_body, (RSAPrivateKey) this.RSAKeyPair.getPrivate());

        //Return the key.
        return receipt;
    }

    /**
     * This is called by the CTF when it receives a request to change the vote a given voter.
     * @param id - The ID of the voter.
     * @param cipherpackage
     * @return The receipt of the vote as a signature of id||EncVote
     * @throws Exception
     */
    public byte [] OnReceivingVoteChangeReq(BigInteger id, String cipherpackage) throws Exception {
        //Check if the data has been supplied
        if (id == null || cipherpackage == null)
            throw new Exception("The ID or the vote has not been supplied");

        //Check if the ID is valid
        if (!database.IsValidID(id))
            throw new Exception("The ID is not valid.");

        //Has Voted
        if (!database.HasVoted(id))
            return null;

        //Adding the vote to the database.
        database.ChangeVote(id, cipherpackage);

        //Creating the body of the receipt.
        byte [] receipt_body;
        byte [] deli = {0,0,0}; //This is the deliminitor detween the ID and the Encrypted Vote.
        receipt_body = ArrayUtils.addAll(id.toByteArray(),deli);
        receipt_body = ArrayUtils.addAll(receipt_body, cipherpackage.getBytes());

        //Signing the receipt body.
        byte [] receipt;
        receipt = RSAWrapper.sign(receipt_body, (RSAPrivateKey) RSAKeyPair.getPrivate());

        //Return the key.
        return receipt;
    }

    /**
     * Tallies the votes and returns a String representation of the results
     * @return The results string that is ready to be put in a text field
     */
    public String calculateResults()
    {
        try
        {
            String results = "";
            
            long max = -1;
            int maxIndex = -1;
            long[] counts = new long[candidates.size()];
            for(int i = 0; i < candidates.size(); i++)
            {
                counts[i] = database.countVotes(candidates.get(i));
                if(counts[i] > max)
                {
                    max = counts[i];
                    maxIndex = i;
                }
                
                results += candidates.get(i)+": " + counts[i] + " votes\n";
            }
            
//            results += candidates.get(maxIndex) + " has won!";
            

            database.retreive_results("/Users/stkerr/NetBeansProjects/voteresults.txt");
            return results;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * This is called when the CTF receives a voter's decryption key.
     * @param id - the voter's ID
     * @param PriKey - The decryption key.
     * @throws Exception
     */
    public void OnReceivingVoteDecKey(BigInteger id, RSAPrivateKey PriKey) throws Exception {
        //Checking that the ID and voter's Private key has been supplied
        if (id == null || PriKey == null)
            throw new Exception("The ID or the Private Key has not been supplied.");

        //Checking that the voter has voted
        if (!database.HasVoted(id))
            throw new Exception("The voter with ID: " + id.toString() + " didn't vote before.");

        //Storing the decryption key in the database.
        database.InsertDecKey(id, PriKey);
    }

    /**
     * This is called by the CTF to check whether a voter is eligible to vote or not
     * @param SSN - the voter's SSN.
     * @return True - if the voter is eligible.
     * @throws Exception if any error occurs.
     */
    public boolean IsEligVoter(String SSN) throws Exception
    { return database.IsElig(SSN); }

    /**
     * This is called by the CTF to check whether a voter has registered.
     * @param SSN - the voter's SSN.
     * @return True - if the voter has registered.
     * @throws Exception if any error occurs.
     */
    public boolean HasReg(String SSN) throws Exception
    { return database.IsReg(SSN); }

    /**
     * This method is called by the CTF to parse the messages received from the voter.
     * @param message - The message to be parsed.
     * @return The list containing the command and its parameter.
     */
    public ArrayList ParseMessage(Message message) {
        ArrayList commandList = new ArrayList();
        StringTokenizer sToken = new StringTokenizer(message.getContents());
        while (sToken.hasMoreTokens())
            commandList.add(sToken.nextToken("=?&"));
        return commandList;
    }

    /**
     * This is called by the CTF internally to add an ID to the list of IDs that are not offered to the voter and which are used to resolve the ID conflicts.
     * @param newID - The ID that need to be added.
     * @throws Exception - If any error occurred or the ID has not been supplied.
     */
    private void addSecondaryID(BigInteger newID) throws Exception
    { if (newID != null) database.AddIDToDB(newID); }

    /**
     * This method deals with the protesting process when a voter thinks that his vote has not been counted properly.
     *  It starts by verifying the signature on the receipt and then checking on of two cases:
     * <br>1) The vote has not been counted properly.
     * <br>2) The vote has not been counted at all.
     * @param ID - The ID of the user who is protesting.
     * @param RSAVotePri - The RSA private key that is used to encrypt the voter's vote.
     * @param cipherpackage - The encrypted vote and all the nessaccary information to decrypt the vote.
     * @param receipt - The CTF signature on the user vote to proof that she voted successfully.
     * @return True - if the protest is valid.<br>False: if the protest is invalid.
     * @throws Exception - If any error occur or any of the arguments has not been supplied.
     */
    public boolean OnProtest(BigInteger ID, RSAPrivateKey RSAVotePri, String cipherpackage, byte[] receipt) throws Exception {
        if (ID == null || RSAVotePri == null || cipherpackage == null || receipt == null)
            throw new Exception("The correct parameter hasn't been sent");

        //Check the signature
        byte [] signed_message;
        byte [] deli = {0,0,0}; //This is the deliminitor detween the ID and the Encrypted Vote.
        signed_message = ArrayUtils.addAll(ID.toByteArray(),deli);
        signed_message = ArrayUtils.addAll(signed_message, cipherpackage.getBytes());
        if (!RSAWrapper.checkSignature(signed_message, receipt, (RSAPublicKey) RSAKeyPair.getPublic()))
            return false;

        //If this voter has voted check that the vote has been properly counted.
        if (database.HasVoted(ID)) {
            String vote = database.getVote(ID);
            
            //Check if the voter has revealed his vote before protesting.
            if (vote == null)
                throw new Exception("You cannot protest before revealing your vote");

            //Decrypting the vote sent by the client as part of the protest.
            RSAMachine rsa = new RSAMachine();
            rsa.loadMachine(cipherpackage);
            StringTokenizer tokens = new StringTokenizer(cipherpackage,"=&");
            tokens.nextToken("=&");
            BigInteger ciphertext = new BigInteger(tokens.nextToken("=&"));
            BigInteger ID_vote = rsa.decrypt(ciphertext, RSAVotePri);
            String storedVote = MentalPokerWrapper.int2string(ID_vote);
            String [] voteArr = storedVote.split(":");
            
            if (!voteArr[1].equalsIgnoreCase(vote)) {
                //Vote has been inproperly stored in the database.
                database.ChangeVote(ID, cipherpackage);
                database.InsertDecKey(ID, RSAVotePri);
                return true;
            }
            else //The vote stored in the database is the same one the user is protesting for.
                return false;
        }

        //The voter's vote has not been added to the database and the receipt is correct.
        //Add the vote to the database
        this.addProtestedVote(ID, cipherpackage, RSAVotePri);
        return true;
    }

    /**
     * This is called internally by the CTF if the protest was valid and the voter's vote has not been found in the database.
     * @param ID - The ID of the voter protesting.
     * @param cipherpackage - The encrypted version of the vote.
     * @param RSAVotePri - The voter RSA private key used to encrypt the vote.
     */
    private void addProtestedVote(BigInteger ID, String cipherpackage, RSAPrivateKey RSAVotePri) throws Exception {
        if (ID == null || cipherpackage == null || RSAVotePri == null)
            throw new Exception("The correct parameter hasn't been sent");

        //Check if the ID is valid
        if (!database.IsValidID(ID))
            throw new Exception("The ID is not valid.");

        //Adding the vote to the database.
        database.addVote(ID, cipherpackage);

        //Storing the decryption key in the database.
        database.InsertDecKey(ID, RSAVotePri);
    }

    /**
     * This is the main function where the CTF is running and doing its job.
     * @param args - The command line arguments.
     */
    public static void main(String[] args) {
        try {
            //Initializing the CTF
            CTF ctf = new CTF("/Users/stkerr/NetBeansProjects/userlist.txt",
                    "/Users/stkerr/NetBeansProjects/secondids.txt", new Date(), new Date(), new Date());

//            ctf.generateIDsTest(2, "/Users/stkerr/NetBeansProjects/id.txt","/Users/stkerr/NetBeansProjects/secondids.txt");
            
            //Display the GUI
            ctf.configGUI();
            ctf.setVisible(true);

            //Additional intialization of the CTF internal data.
            ctf.init();

            //Strating the networking.
            ctf.startNetworking("127.0.0.1", 4444);

            //varaibles needed inside the main while loop.
            Message message;
            String com, id, exp, mod, name, ssn;
            byte [] cipher, receipt, symkey, iv;
            KeyFactory keyFactory;
            RSAPrivateKeySpec RSASpec;
            RSAPrivateKey RSAVotePri;
            String response = null;
            ArrayList command;
            BigInteger ID, EXP, MOD;
            long otp;
            Date TimeRec;
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm aaa");
            String cipherpackage;

            //Data Structure Storing the voter's session IDs when handling the ID Enroll request.
            ArrayList<Integer> sessionIDs = new ArrayList<Integer>();
            
            while(true) {
                //Receiving a message from the voter.
                message = ctf.receive();

                //TimeRec will hold the time when the message has been received.
                TimeRec = new Date();
                System.out.println("Server Received: " + message + ". At: " + dateFormat.format(TimeRec.getTime()));

                //Parsing the message sent from the voter.
                command = ctf.ParseMessage(message);

                //Check if the command was empty
                if (command.isEmpty())
                    continue;

                //Get the command the voter sent.
                com = command.get(0).toString();

                //Check ing which command has been sent and perform the relevent actions.
                if (com.equalsIgnoreCase("REVEALVOTE")) {
                    /* This is the when the user wants to reveal her vote after the voting deadline where the CTF can
                     *  decrypt the votes and count the results.
                     */
                    try {
                        //make sure that this is after the end of the voting peroid first.
                        if (TimeRec.before(ctf.getVotingDeadline())) {
                            response = "REVEALRESPONSE?STATUS={FAIL:Sorry, you cannot reveal your vote before the voting deadline on: " + dateFormat.format(ctf.getVotingDeadline()) + "}";
                            ctf.send(response, message.getID());
                            continue;
                        }

                        //Extracting the command parameters.
                        id = command.get(2).toString();
                        exp = command.get(4).toString();
                        mod = command.get(6).toString();

                        ID = new BigInteger(id);
                        EXP = new BigInteger(exp);
                        MOD = new BigInteger(mod);

                        //Create the private key from exp and mod
                        keyFactory = KeyFactory.getInstance("RSA", "BC");
                        RSASpec = new RSAPrivateKeySpec(MOD,EXP);
                        RSAVotePri = (RSAPrivateKey) keyFactory.generatePrivate(RSASpec);

                        //Storing the information in the database.
                        ctf.OnReceivingVoteDecKey(ID, RSAVotePri);

                        //Create and send the response
                        response = "REVEALRESPONSE?STATUS={ACCEPT:Your decryption key has been saved}";
                        ctf.send(response, message.getID());
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                        response = "REVEALRESPONSE?STATUS={FAIL:Your decryption key has NOT been saved. " + e.getMessage() + "}" ;
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("IDENROLL")) {
                    /*
                     * Thi is the command where the voter has selected and ID from the list of possible IDs and then
                     *  encrypted that ID using her Pohlig-Hellman encryption key. Then she send the double encrypted ID
                     *  to be decrypted by the CTF. This command must be preceeded by a sucessfull registeration request.
                     *  to limit the ID enrollment to registered users only.
                     */
                    try {
                        //Making sure that this has been done after a successful Registeration command.
                        if (!sessionIDs.contains(new Integer(message.getID()))) {
                            response = "IDRESPONSE?STATUS={FAIL:You must register before getting an ID}";
                            ctf.send(response, message.getID());
                            continue;
                        }

                        //Extracting the parameters
                        id = command.get(2).toString();
                        ID = new BigInteger(id);

                        //Decrypting the ID.
                        ID = ctf.OnIDDecReq(ID);

                        //Create and send the response
                        response = "IDRESPONSE?STATUS={ACCEPT:The ID is decrypted and attached}&ID=" + ID.toString();
                        ctf.send(response, message.getID());

                        //deleting the session ID from the list.
                        sessionIDs.remove(new Integer(message.getID()));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        response = "IDRESPONSE?STATUS={FAIL:The ID cannot be decrypted. Error: " + e.getMessage() + "}&ID=0";
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("CHECKVALID")) {
                    /*
                     * This is a command that is sent by the voter to check whether she is an eligible voter.
                     */
                    try {
                        //Extracting the command parameter.
                        name = command.get(2).toString();
                        ssn = command.get(4).toString();

                        //Checking if the user is valid.
                        if (ctf.IsEligVoter(ssn))
                            response = "CHECKRESPONSE?STATUS={ACCEPT:You are an eligible voter}";
                        else
                            response = "CHECKRESPONSE?STATUS={FAIL:You are NOT an eligible voter}";
                        ctf.send(response, message.getID());
                    }
                    catch (Exception e) {
                        response = "CHECKRESPONSE?STATUS={FAIL:The command cannot be executed. Error: " + e.getMessage() + "}";
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("REGISTERREQ")) {
                    /*
                     * This command is sent by the voter to request regsitering to vote.
                     */
                    try {
                        //Check that this message has been received before the registeration deadline and after the Registeration start.
                        if (TimeRec.before(ctf.getRegStart())) {
                            response = "REGRESPONSE?STATUS={FAIL:Sorry, the registeration has not been opened yet, it starts on: " + dateFormat.format(ctf.getRegStart()) + "}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }
                        if (TimeRec.after(ctf.getRegDeadline())) {
                            response = "REGRESPONSE?STATUS={FAIL:Sorry, you have missed the registeration period. It ended on : " + dateFormat.format(ctf.getRegDeadline()) + "}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }

                        //Extracting the parameters.
                        name = command.get(2).toString();
                        ssn = command.get(4).toString();

                        //Registering the user
                        receipt = ctf.OnRegReq(ssn);
                        if ( receipt != null) {
                            //Signing the receipt body.
                            response = "REGRESPONSE?STATUS={ACCEPT:Congratulations, you have been registered.}&RECEIPT=" + new BigInteger(receipt);
                        }
                        else
                            response = "REGRESPONSE?STATUS={FAIL:Sorry, but you are not eligible to register.}&RECEIPT=0";
                        ctf.send(response, message.getID());

                        //Adding the user session to the Array List
                        sessionIDs.add(new Integer(message.getID()));
                    }
                    catch (Exception e) {
                        response = "CHECKRESPONSE?STATUS={FAIL:Sorry, an error has occured. Please try again. Error: " + e.getMessage() + "}&RECEIPT=0";
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("SUBMITVOTE")) {
                    /*
                     * This command is sent by the voter to cubmit her encrypted vote to the CTF.
                     */
                    try {
                        //Checking that this is after the registeration deadline and before the voting deadline.
                        if (TimeRec.before(ctf.getRegDeadline())) {
                            response = "SUBMITRESPONSE?STATUS={FAIL:Sorry, but the voting period hasn't started yet. It starts at: " + dateFormat.format(ctf.getRegDeadline()) + ".}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }
                        if (TimeRec.after(ctf.getVotingDeadline())) {
                            response = "SUBMITRESPONSE?STATUS={FAIL:Sorry, but you have missed the voting period ended on: " + dateFormat.format(ctf.getVotingDeadline()) + ".}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }

                        //Extracting the paramters.
                        id = command.get(2).toString();
                        cipherpackage = 
                                command.get(3).toString() + "=" + command.get(4).toString() + "&" +
                                command.get(5).toString() + "=" + command.get(6).toString() + "&" + 
                                command.get(7).toString() + "=" + command.get(8).toString();
                        
                        ID = new BigInteger(id);

                        //Storing the vote in the database
                        receipt = ctf.OnReceivingVote(ID, cipherpackage);
                        if (receipt == null) {
                            BigInteger newID = ctf.getSecondaryIDs().get(new SecureRandom().nextInt(ctf.getSecondaryIDs().size()));
                            ctf.addSecondaryID(newID);

                            //Generate the response.
                            response = "SUBMITRESPONSE?STATUS={FAIL:Sorry, but the ID you're using has been used by another user.}&OTP=" + newID.toString();
                            
                            
                        }
                        else
                            response = "SUBMITRESPONSE?STATUS={ACCEPT:Your vote has been saved}&RECEIPT=" + new BigInteger(receipt);
                        ctf.send(response, message.getID());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        response = "SUBMITRESPONSE?STATUS={FAIL:Sorry, but your vote has not been counted. Error:" + e.getMessage() + ".}&RECEIPT=0";
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("PROTEST")) {
                    /*
                     * This command is sent by the voter protesting that their vote has not been counted.
                     */
                    try {
                        //Checking this happening after the voting deadline
                        if (TimeRec.before(ctf.getVotingDeadline())) {
                            response = "PROTESTRESPONSE?STATUS={FAIL:Sorry, but you cannot protest before the voting deadline at: " + dateFormat.format(ctf.getVotingDeadline()) + ".}";
                            ctf.send(response, message.getID());
                            continue;
                        }

                        //Extracting the parameters
                        id = command.get(2).toString();
                        exp = command.get(4).toString();
                        mod = command.get(6).toString();

                        ID = new BigInteger(id);
                        EXP = new BigInteger(exp);
                        MOD = new BigInteger(mod);

                        cipherpackage =
                                command.get(7).toString() + "=" + command.get(8).toString() + "&" +
                                command.get(9).toString() + "=" + command.get(10).toString() + "&" +
                                command.get(11).toString() + "=" + command.get(12).toString();

                        receipt = new BigInteger(command.get(14).toString()).toByteArray();

                        //Create the private key from exp and mod
                        keyFactory = KeyFactory.getInstance("RSA", "BC");
                        RSASpec = new RSAPrivateKeySpec(MOD,EXP);
                        RSAVotePri = (RSAPrivateKey) keyFactory.generatePrivate(RSASpec);

                        //Verify the signature and check that this hasn't been included in the database.
                        if (ctf.OnProtest(ID,RSAVotePri,cipherpackage,receipt))
                            response = "PROTESTRESPONSE?STATUS={ACCEPT:your vote has been updated successfully.}";
                        else
                            response = "PROTESTRESPONSE?STATUS={FAIL:Sorry, but your protest is not valid.}";

                        ctf.send(response, message.getID());

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        response = "PROTESTRESPONSE?STATUS={FAIL:Sorry, but an error has occured, please try again. Error: " + e.getMessage() + ".}";
                        ctf.send(response, message.getID());
                    }
                }
                else if (com.equalsIgnoreCase("CHANGEREQ")) {
                    /*
                     * This is sent by the voter when she want to change her voter where she supplied her ID and her
                     *  new vote in encrypted form.
                     */
                    try {
                        //Checking that this is after the registeration deadline and before the voting deadline.
                        if (TimeRec.before(ctf.getRegDeadline())) {
                            response = "CHANGERESPONSE?STATUS={FAIL:Sorry, but the voting period hasn't started yet. It strats at: " + dateFormat.format(ctf.getRegDeadline()) + ".}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }
                        if (TimeRec.after(ctf.getVotingDeadline())) {
                            response = "CHANGERESPONSE?STATUS={FAIL:Sorry, but you have missed the voting period ended on: " + dateFormat.format(ctf.getVotingDeadline()) + ".}&RECEIPT=0";
                            ctf.send(response, message.getID());
                            continue;
                        }
                        
                        //Extracting the parameters.
                        id = command.get(2).toString();
                        cipherpackage = 
                                command.get(3).toString() + "=" + command.get(4).toString() + "&" +
                                command.get(5).toString() + "=" + command.get(6).toString() + "&" + 
                                command.get(7).toString() + "=" + command.get(8).toString();
                        
                        ID = new BigInteger(id);

                        //Submitting the change request to the CTF.
                        receipt = ctf.OnReceivingVoteChangeReq(ID, cipherpackage);

                        if (receipt == null)//The voter didn't vote before.
                            response = "CHANGERESPONSE?STATUS={FAIL:Sorry, but you haven't voted before.}&RECEIPT=0";
                        else
                            response = "CHANGERESPONSE?STATUS={ACCEPT:Your vote has been changed successfully}&RECEIPT=" + new BigInteger(receipt);
                        ctf.send(response, message.getID());
                    }
                    catch (Exception e) {
                        response = "CHANGERESPONSE?STATUS={FAIL:Sorry, your vote couldn't be changed. Error: " + e.getMessage() + ".}&RECEIPT=0";
                    }
                }
                else {
                    System.out.println("The command received has not been formated properly");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
