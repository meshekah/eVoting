/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

import crypto.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.interfaces.RSAPrivateKey;


/**
 * This is a class to manage the data at the CTF side with databases and files.
 * @author Mohammed
 */
public class DB {

    //This keeps the connection to the database.
    private Connection connect = null;

    //This is used to store the path name of the Eligible voters file.
    private String EVoterFilePath = null;

    //This is used to store the path name of the regsitered voters file.
    private String RVoterFilePath = null;
    
    //used as a global reader to read the files.
    private Scanner reader;
    
    //used as a global writer to write to files.
    private Formatter writer;

    //Used as a global prepared statamenet to send prepared commands to the database.
    private PreparedStatement pRegStmt = null;

    //Used as a global Result Set Object to store the output of the database.
    private ResultSet rset = null;
    
    /**
     * This is called at the to clear all the database records in tables [voters & votes].
     * @throws Exception - If an error occurred during the connection to the database or clearing the tables.
     */
    public void ClearDB() throws Exception {
        try {
            //Open the database.
            ConnectDB();

            //Clean the 'voter' table.
            connect.createStatement().executeUpdate("DELETE FROM voters");
            connect.createStatement().executeUpdate("DELETE FROM votes");
            
            //Clear the resources
            if (connect != null)
                connect.close();
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Receives the location of the file containing the details all eligible voters to be added the database.
     * @param filePath - this is the path name of the file that contains the eligible voters details.
     * @throws Exception - if any error occurred.
     */
    public void AddEligVotersToDB(String filePath) throws Exception {
        try {
            //Opening the file.
            if (filePath == null)
                throw new Exception("File Path has not been supplied");
            EVoterFilePath = filePath;
            File eVoters = new File(EVoterFilePath);

            if (!eVoters.exists())
                throw new Exception("File cannot be found");
            if (!eVoters.isFile())
                throw new Exception("The path doean't point to a file");
            if (!eVoters.canRead())
                throw new Exception("The file at " + EVoterFilePath + " cannot be read");

            //Connent to the Databse.
            ConnectDB();

            //Prepare the Prepared Statements
            PreparedStatement pstmt = connect.prepareStatement("INSERT INTO voters (SSN, FName, LName) VALUES (?,?,?)");

            //Readng the content of the file and insert it to the database.
            reader = new Scanner(eVoters);
            String SSN = null, fName = null, lName = null;
            while (reader.hasNext()) {
                SSN = reader.next();
                fName = reader.next();
                lName = reader.next();
                pstmt.setString(1, SSN);
                pstmt.setString(2, fName);
                pstmt.setString(3, lName);
                if (pstmt.executeUpdate() != 1) { //if any error ocuurs during adding votes to the databse an
                    //Excption will be thrown and all the voters currenlt in the database will be removed
                    // such that the whole process can be redone again after fixing the file and to make
                    // sure that no voter has been missed.
                    connect.createStatement().executeUpdate("DELETE * FORM voter");
                    throw new Exception("The user" + SSN + " " + fName + " " + lName + "has not beed added.");
                }
            }
            //Close the Scanner object.
            if (reader != null)
                reader.close();
            //Close the connection to the Databse;
            if (connect != null)
                connect.close();

        } catch (Exception e) {
            //Close the Scanner object.
            if (reader != null)
                reader.close();
            //Close the connection to the Databse;
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This is called to check whether a user is eligible to vote or not.
     * @param ssn - The SSN of the user to check for eligibility.
     * @return True - if the user is eligible. <br>False - if the user is not eligible to vote.
     * @throws Exception - if any error occurred.
     */
    public boolean IsElig(String ssn) throws Exception {
        try {
            boolean bool;
            if (ssn == null)
                throw new Exception("The SSN has not been supplied");

            //Connect to the Database
            ConnectDB();

            //Create the prepared ststement
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM voters WHERE SSN=?");
            pRegStmt.setString(1, ssn);

            //Check if the user has registered.
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while checking for the voter");
            if (rset.getInt(1) == 1)
                bool = true;
            else
                bool = false;

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            return bool;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This method is used to check whether a voter has registered or not.
     * @param ssn - the SSN of the voter who we want to check his registration status.
     * @return True - if the user exists and has registered.<br>False: otherwise.
     * @throws Exception - if any error occur.
     */
    public boolean IsReg(String ssn) throws Exception {
        try {
            boolean bool;
            if (ssn == null)
                throw new Exception("The SSN has not been supplied");

            //Connect to the Database
            ConnectDB();

            //Create the prepared ststement
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM voters WHERE SSN=? AND HasReg=1");
            pRegStmt.setString(1, ssn);

            //Check if the user has registered.
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while checking for the voter");
            if (rset.getInt(1) == 1)
                bool = true;
            else
                bool = false;

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            return bool;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            throw new Exception(e.getMessage());
        }

    }

    /**
     * Checks if there is any duplications in the identifier.
     * @param id - This ID that need to be checked for the duplicates.
     * @return - True: if the the ID is already exists in the database, i.e. is a duplicate.<br>False: if this ID doesn't exist in the database. No duplication is found.
     * @throws Exception - if any error occur.
     */
    public boolean IsIDDupl(BigInteger id) throws Exception {
        try {
            boolean bool;
            if (id == null)
                throw new Exception("The SSN has not been supplied");

            //Connect to the Database
            ConnectDB();

            //Create the prepared ststement
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM votes WHERE ID=?");
            pRegStmt.setString(1, id.toString());

            //Check if identifier already exists.
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while checking for the identifier");
            if (rset.getInt(1) == 1)
                bool = true;
            else
                bool = false;

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();
            return bool;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            throw new Exception(e.getMessage());
        }

    }

    /**
     * Checks if a given voter with a given ID has voted.
     * @param id - the voter's ID whom we need to check if she voted before.
     * @return True - if the user has voted before.<br>False - if the user has not voted before.
     * @throws Exception - if any error occurs.
     */
    public boolean HasVoted(BigInteger id) throws Exception {
        try {
            boolean bool;
            if (id == null)
                throw new Exception("The ID has not been supplied");

            //Connect to the Database
            ConnectDB();

            //Create the prepared ststement
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM votes WHERE ID=?");
            pRegStmt.setString(1, id.toString());

            //Check if the voter has voted.
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while checking for the identifier");
            if (rset.getInt(1) == 1)
                bool = true;
            else
                bool = false;

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();
            return bool;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();
            
            throw new Exception(e.getMessage());
        }

    }


    /**
     * This is called when a voter wants to register by supplying her SSN.
     * @param ssn - The SSN of the user to be registered.
     * @return True - if the registration was successful.<br>False - if the registration was not successful.
     * @throws Exception - if any error occurs, of the arguments has not been supplied.
     */
    public boolean RegVoter(String ssn) throws Exception {
        try {
            if (ssn == null)
                throw new Exception("SSN has not been supplied");

            //Check if the voter is eligible to vote
            if (!IsElig(ssn))
                return false;

            //Check if the user already regsitered
            if (IsReg(ssn))
                return false;

            //Connect to the database
            ConnectDB();

            //Prepare the Prepared Statements
            pRegStmt = connect.prepareStatement("UPDATE voters SET HasReg=1 WHERE SSN=?");
            pRegStmt.setString(1, ssn);            

            if (pRegStmt.executeUpdate() == 0) //if the registeration cannot be performed.
                throw new Exception("The voter with SSN = " + ssn + " cannot be registered. Please try again.");

            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();

            return true;
        }
        catch (Exception e) {
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This is called at the end of the registration page to create the registered voters file.
     * @param path - to the file where the registered users details will be saved.
     * @throws Exception - if any error occurred or the arguments has not been supplied correctly.
     */
    public void CreateRegVotersFile(String path) throws Exception {
        try {
            //Checking the file path.
            if (path == null) {
                throw new Exception("File Path has not been supplied");
            }
            RVoterFilePath = path;

            //Connent to the Databse.
            ConnectDB();

            //Reading from the database and writing the list of registered voters to the file.
            rset = connect.createStatement().executeQuery("SELECT SSN, FName, LName FROM voters WHERE HasReg='1'");
            if (!rset.next())
                throw new Exception("There is no registered users");
            ResultSetMetaData rsetm = rset.getMetaData();
            
            writer = new Formatter(RVoterFilePath); //Open the file to write to it.

            //Wrtie the list of registered voters to the file
            writer.format("SSN \t First Name \t LastName \r\n");
            do {
                writer.format("%s\t%s\t%s \r\n", rset.getString(1), rset.getString(2), rset.getString(3));
            } while (rset.next());

        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        finally {
            if (connect != null)
                connect.close();
            if (rset != null)
                rset.close();
            if (writer != null)
                writer.close();
        }
    }

    /**
     * This will be called at the CTF side whenever a new vote has been received from a client.
     * @param id - The voter's ID.
     * @param cipherpackage - The cipher package which contains the encryption of the voter's vote using hybrid encryption.
     * @throws Exception - if any error occurred or the arguments has not been provided.
     */
    public void addVote(BigInteger id, String cipherpackage) throws Exception {
        try {
            if ( id == null)
                throw new Exception("The voter identifier has not been supplied");
            if (cipherpackage == null)
                throw new Exception("The encrypted vote has not been supplied");
            
            //Checks if the ID is valid
            if (!IsValidID(id))
                throw new Exception("The ID is not valid.");

            //Check if the identifier already exists in the database
            if (IsIDDupl(id) == true)
                throw new Exception("Cannot add the vote, the identifier already exists in the database or there might been an error");

            //Connent to the Databse.
            ConnectDB();

            //Prepare the Prepared Statements
            pRegStmt = connect.prepareStatement("INSERT INTO votes (ID, EncVote) VALUES (?,?)");
            pRegStmt.setString(1, id.toString());
            pRegStmt.setString(2, cipherpackage);

            //Adding the vote
            if (pRegStmt.executeUpdate() == 0)
                throw new Exception("The voter with ID = " + id.toString() + " cannot be added. Please try again.");

            //Closing resources
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
        }
        catch (Exception e) {
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This is called by the CTF when it receives a change vote request from the voter.
     * @param id - The voter's ID who need to change her vote.
     * @param cipherpackage - The cipher package which contains the encryption of the voter's vote using hybrid encryption.
     * @throws Exception - if an error occurs or the arguments has not been supplied correctly.
     */
    public void ChangeVote(BigInteger id, String cipherpackage) throws Exception {
        try {
        if (id == null)
            throw new Exception("The voter identifier has not been supplied");
        if (cipherpackage == null)
            throw new Exception("The encrypted vote has not been supplied");

        //Connect to the Database
        ConnectDB();

        //Prepare the prepared statement.
        pRegStmt = connect.prepareStatement("Update votes SET EncVote=? WHERE ID=?");
        pRegStmt.setBytes(1, cipherpackage.getBytes());
        pRegStmt.setString(2, id.toString());

        //Update the database.
        if (pRegStmt.executeUpdate() == 0)
            throw new Exception("The database cannot be updated, check the ID");

        //Cleaning up resources.
        if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
        }
        catch (Exception e) {
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This is called after the end of the voting period where every user will send her identifier
     *  and the decryption key. The method will extract the vote from the database, decrypt it, and
     *  then save it back to the database along with the decryption key.
     * @param id - The voter's ID who is supplying this encryption key.
     * @param PriKey - The voter's private key used to encrypt the vote during the voting period.
     * @throws Exception - if an error occurred or the arguments has not been supplied correctly.
     */
    public void InsertDecKey(BigInteger id,RSAPrivateKey PriKey) throws Exception {
        // It extracts the vote, decrypt it and add to the database along with the key.
        try {
            if (id == null)
                throw new Exception("The voter identifier has not been supplied");
            if (PriKey == null)
                throw new Exception("The decryption key has not been supplied");

            //Connent to the Databse.
            ConnectDB();

            //Fetch the Encrypted vote from the database to decrypt it first.
            pRegStmt = connect.prepareStatement("SELECT EncVote From votes WHERE ID=?");
            pRegStmt.setString(1, id.toString());
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("The ID is not correct");
            String encVote = rset.getString("EncVote");
            rset.close();
            pRegStmt.close();

            System.out.println("Using: " + encVote);

            //Decrypt the vote
            RSAMachine rsa = new RSAMachine();
            rsa.loadMachine(encVote);
            StringTokenizer tokens = new StringTokenizer(encVote,"=&");
            tokens.nextToken("=&");
            BigInteger ciphertext = new BigInteger(tokens.nextToken("=&"));
            
            BigInteger vote = rsa.decrypt(ciphertext, PriKey);
            String str_vote = MentalPokerWrapper.int2string(vote);
            System.out.println("Got vote: " + vote);
            System.out.println("Got vote: " + str_vote);
            String [] voteArr = str_vote.split(":");
            //Store the vote with the decryption key to the database.
            pRegStmt = connect.prepareStatement("UPDATE votes SET DecKey=?, Vote=? WHERE ID=?");
            pRegStmt.setBytes(1, PriKey.getEncoded());
            pRegStmt.setString(2, voteArr[1]);
            pRegStmt.setString(3,id.toString());

            if (pRegStmt.executeUpdate() == 0)
                throw new Exception("Cannot add the private key to the database");

            //Cleaning up the connections
            if (rset != null)
                rset.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
        }
        catch (Exception e) {
            if (rset != null)
                rset.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Given a candidate, counts how many votes they had.
     * @param candidate - The candidate name
     * @return The number of votes for the candidate.
     */
    public long countVotes(String candidate) throws Exception
    {
        try
        {
            //Connent to the Databse.
            ConnectDB();
            
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM votes WHERE Vote=?");
            pRegStmt.setString(1, candidate);
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error occured while retrieving the votes");
            
            long NumOfVotes = rset.getLong(1);
            
            if (rset != null)
                rset.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();
            
            return NumOfVotes;
        }
        catch(Exception e)
        {
            if (rset != null)
                rset.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (connect != null)
                connect.close();

            throw new Exception(e.getMessage());
        }
    }
    
    /**
     * This is called after decrypting all the votes by the CTF. It will create a file containing all the voters of
     * (ID, Vote, Encrypted Vote, Decryption Key). This will be save on the file supplied in the argument.
     * @param FilePath - The file to store the voting results in.
     * @throws Exception - if an error occurs or the arguments has not been sent correctly.
     */
    public void retreive_results(String FilePath) throws Exception {
        try {
            //Checking the file path.
            if (FilePath == null)
                throw new Exception("File Path has not been supplied");
            
            //Connent to the Databse.
            ConnectDB();

            //Reading from the database and writing the list of (ID, Encrypted vote, Decryption Key, vote) to the file.
            rset=connect.createStatement().executeQuery("SELECT * FROM votes");
            if (!rset.next())
                throw new Exception("No voter has voted.");
            
            writer = new Formatter(FilePath); //Open the file to write to it.

            //Wrtie the list of voters who vote for the given vote to the file
            writer.format("ID \t Vote\t Encrypted Vote\t Decryption Key\r\n");
            String encVote = null, PriKey = null;
            do {
                encVote = new String(rset.getBlob("EncVote").getBytes(1, (int) rset.getBlob("EncVote").length()));
                /*
                 * Might not have an entry here, but that's OK
                 */
                try
                {
                    PriKey = new String(rset.getBlob("DecKey").getBytes(1, (int) rset.getBlob("DecKey").length()));
                }
                catch(Exception e)
                {
                    System.out.println("No private key entry. Continuing.");
                }
                writer.format("%s\t%s\t%s\t%s \r\n", rset.getString("ID"), rset.getString("Vote"),encVote,PriKey);
            } while (rset.next());

            //Cleaning Resources.
            if (rset != null)
                rset.close();
            if (connect != null)
                connect.close();
            if (writer != null)
                writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            if (rset != null)
                rset.close();
            if (connect != null)
                connect.close();
            if (writer != null)
                writer.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Creates a connection to the database and a statement object. This will only be called internally
     *  by the DB class.
     * @throws Exception if the JDBC driver cannot be loaded, connection cannot be established or Statement object cannot be created.
     */
    private void ConnectDB() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            if (connect != null) {
                connect.close();
            }
            connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/evote", "root", "");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            if (connect != null) {
                connect.close();
            }
        }
    }

    /**
     * Receives the path of the file containing all the valid IDs to be added the database.
     * @param filePath - The location of the file of all valid IDs.
     * @throws Exception - if an error occurs of the path has not been supplied.
     */
    public void AddIDsToDB(String filePath) throws Exception {
        try {
            //Opening the file.
            if (filePath == null) {
                throw new Exception("File Path has not been supplied");
            }

            File IDs = new File(filePath);

            if (!IDs.exists())
                throw new Exception("File cannot be found");
            if (!IDs.isFile())
                throw new Exception("The path doean't point to a file");
            if (!IDs.canRead())
                throw new Exception("The file at " + filePath + " cannot be read");

            //Connent to the Databse.
            ConnectDB();

            //Prepare the Prepared Statements
            PreparedStatement pstmt = connect.prepareStatement("INSERT INTO ids (ID) VALUES (?)");

            //Readng the content of the file and insert it to the database.
            reader = new Scanner(IDs);
            String ID = null;
            while (reader.hasNext()) {
                ID = reader.next();
                pstmt.setString(1, ID);
                if (pstmt.executeUpdate() != 1) { //if any error ocuurs during adding IDs to the databse an
                    //Excption will be thrown and all the IDs currenlt in the database will be removed
                    // such that the whole process can be redone again after fixing the file and to make
                    // sure that no voter has been missed.
                    connect.createStatement().executeUpdate("DELETE * FORM ids");
                    throw new Exception("The ID: " + ID + " cannot be added.");
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            //Close the Scanner object.
            if (reader != null) {
                reader.close();
            }
            //Close the connection to the Databse;
            if (connect != null) {
                connect.close();
            }
        }

    }

    /**
     * This is called to add only one ID to the database.
     * @param id - The ID to be added to the database.
     * @throws Exception - if there is any error.
     */
    public void AddIDToDB(BigInteger id) throws Exception {
        try {
            //Opening the file.
            if (id == null)
                throw new Exception("The ID hasn't been supplied.");

            //Connent to the Databse.
            ConnectDB();

            //Prepare the Prepared Statements
            PreparedStatement pstmt = connect.prepareStatement("INSERT INTO ids (ID) VALUES (?)");

            //Add the ID to the database.
            pstmt.setString(1, id.toString());
            if (pstmt.executeUpdate() != 1)
                throw new Exception("The ID: " + id.toString() + " cannot be added.");

            //Close the Scanner object.
            if (reader != null)
                reader.close();
            //Close the connection to the Databse;
            if (connect != null)
                connect.close();
        }
        catch (Exception e) {
            //Close the Scanner object.
            if (reader != null)
                reader.close();
            //Close the connection to the Databse;
            if (connect != null)
                connect.close();
            throw new Exception(e.getMessage());
        }

    }

    /**
     * This is called to check whether an ID is valid.
     * @param id - The ID that needed to be checked
     * @return true - if the ID is valid.
     * @throws Exception - if anything goes wrong.
     */
    public boolean IsValidID(BigInteger id) throws Exception {
        try {
            boolean bool;
            if (id == null)
                throw new Exception("The ID has not been supplied");

            //Connect to the Database
            ConnectDB();

            //Create the prepared ststement
            pRegStmt = connect.prepareStatement("SELECT COUNT(*) FROM ids WHERE ID=?");
            pRegStmt.setString(1, id.toString());

            //Check if the id is valid.
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while checking for the id");
            if (rset.getInt(1) == 1)
                bool = true;
            else
                bool = false;

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            return bool;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This is called to retrieve the vote of a given voter by supplying her ID.
     * @param id - The ID of the voter to retrieve her vote.
     * @return The vote retrieved from the database.
     * @throws Exception - If there is any error or the ID has not been supplied.
     */
    public String getVote(BigInteger id) throws Exception {
        try {
            if (id == null)
                throw new Exception("The ID hasn't been sent");

            //Check that the user has vote
            if (!this.HasVoted(id))
                throw new Exception("The user has not voted yet");

            //Connect to the database
            ConnectDB();

            //Extract the vote.
            this.pRegStmt = connect.prepareStatement("SELECT Vote FROM votes WHERE ID=?");
            pRegStmt.setString(1, id.toString());
            rset = pRegStmt.executeQuery();
            if (!rset.next())
                throw new Exception("An error has occured while extracting the vote");

            String vote = rset.getString(1);

            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            return vote;
        }
        catch (Exception e) {
            if (connect != null)
                connect.close();
            if (pRegStmt != null)
                pRegStmt.close();
            if (rset != null)
                rset.close();

            throw new Exception(e.getMessage());
        }
    }

    /*
      This is only for testing.
    public static void main(String[] args) {
         try {
            DB db = new DB();
            Scanner in = new Scanner(System.in);
            System.out.println("Destination file: ");

            BigInteger x = new BigInteger(in.nextLine());
            byte[] b = new byte[10];
            for (int i=0;i<10;i++)
                b[i]=(byte)i;
            System.out.println(db.IsIDDupl(x));

         }
         catch (Exception e) {
             System.out.println(e.getMessage());
         }
     }
     * 
     */
}
