

package networking;

import java.util.Random;

/**
 * The Message class defines the structure for the communication
 * between the CTF and it's clients
 */
public class Message
{
    private String contents;
    private int ID;

    /**
     * Constructs a new message with the given text
     * @param contents The contents of the message to end
     * @param parse Parse the given String as a whole message or not
     */
    public Message(String contents, boolean parse)
    {
        if(parse == true)
        {
            int delimeter = contents.indexOf(':');
            this.contents = contents.substring(delimeter+1);
            this.ID = Integer.parseInt(contents.substring(0, delimeter));
        }
        else
        {
            this.contents = contents;
            this.ID = new Random().nextInt();
        }
    }

    /**
     * Constructs a new message with the given text
     * @param contents The contents of the message to end
     * @param ID The ID number for this message
     */
    public Message(String contents, int ID)
    {
        this.contents = contents;
        this.ID = ID;
    }

    
    /**
     * Converts the message to a string of the form ID:CONTENTS
     * @return A string representation of the message
     */
    @Override
    public String toString()
    {
        return this.ID + ":" + this.contents;
    }

    /**
     * Sets the message ID
     * @param ID The new ID
     */
    public void setID(int ID)
    {
        this.ID = ID;
    }

    /**
     * Get the message ID
     * @return The message ID
     */
    public int getID()
    {
        return ID;
    }

    /**
     * Get the contents of the message
     * @return The message contents
     */
    public String getContents()
    {
        return contents;
    }
}
