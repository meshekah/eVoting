package client.networking;

import networking.Message;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import networking.QueuedSendThread;

/**
 * The class that holds the logic for a client to communicate with the CTF
 */
public class ListenClient extends Thread
{

    /**
     * Global should run variable
     */
    boolean shouldRun;
    
    /**
     * The messaging ID of this client
     */
    private int ID;

    /**
     * This thread sends data to the server
     */
    private QueuedSendThread sendThread;

    /**
     * A BlockingQueue of messages to send
     */
    private final BlockingQueue<Message> outmessages;

    /**
     * A BlockingQueue of messages we have received
     */
    private final BlockingQueue<Message> inmessages;

    private String host;
    private int port;

    /**
     * Constructs a new ListenClient
     */
    public ListenClient()
    {
        outmessages = new LinkedBlockingQueue<Message>();
        inmessages = new LinkedBlockingQueue<Message>();
        shouldRun = true;
    }

    /**
     * Adds a message to the queue that this client is sending to the
     * server
     *
     * @param m The message to send to the server
     */
    public void send(Message m)
    {
        try
        {
            outmessages.put(m);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Removes the oldest message that the server has sent this client
     *
     * @return The oldest message in the queue from the server
     */
    public Message receive()
    {
        try
        {
            return inmessages.take();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ceases the networking of the thread
     */
    public void cease()
    {
        shouldRun = false;
        try
        {
            sleep(500);
        }
        catch(Exception e)
        {
           e.printStackTrace();
        }
        
    }
    
    /**
     * Starts the networking for this object in a
     * separate thread. Induces a 2 second delay
     * to allow the server and client to sync up.
     *
     * @param host The address of the host to connect to. This can be either
     * and IP (such as 127.0.0.1) or a string (www.google.com)
     *
     * @param port The port to connect to on the server
     */
    public void startNetworking(String host, int port)
    {
        this.host = host;
        this.port = port;

        this.start();

        // allow 2 seconds to get an ID number
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while (t1-t0<2000);    
    }

    /**
     * Returns the session ID of this client
     * @return The session ID
     */
    public int getID()
    {
        return ID;
    }

    /**
     * Performs the bulk of the networking work for the client.
     * Should be called with start()
     */
    public void run()
    {
        Socket clientSocket = null; // the socket for a new client
        PrintWriter networkout = null;
        BufferedReader networkin = null;
        try
        {
            clientSocket = new Socket(host, port);
            System.out.println("Connected to: "
                    + clientSocket.getRemoteSocketAddress().toString() + ","
                    + clientSocket.getLocalPort());
            System.out.println("This connection is: "
                    + clientSocket.getLocalSocketAddress().toString());

            /* Get the I/O streams to communicate */
            networkout = new PrintWriter(clientSocket.getOutputStream(), true);
            networkin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            /* Read from console */
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String fromServer;

            /* Wait to hear from the server */
            while(true)
            {
                if(shouldRun == false)
                    return;
                
                fromServer = networkin.readLine();

                /* Echo server response */
                System.out.println("Server: " + fromServer);

                Message received = new Message(fromServer, true);

                if(fromServer == null)
                    break;

                /* Die on "Bye." */
                if (received.getContents().equals("Bye."))
                {
                    break;
                }

                /* If we got a connected message, start our sending thread */
                if(received.getContents().equals("CONNECTED") && received.getID() == 0)
                {
                    /* Spin off and start the output thread */
                    QueuedSendThread thread = new QueuedSendThread(networkout, outmessages);
                    thread.start();
                    continue;
                }
                else if(received.getContents().equals("CONFIG"))
                {
                    ID = received.getID();
                    System.out.println("ID: " + ID);
                    continue;
                }
                inmessages.put(received);
            }

            /* Close all the streams */
            networkout.close();
            networkin.close();
            stdIn.close();
            clientSocket.close();
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host.");
//            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("I/O Exception");
//            System.exit(1);
        }
        catch (InterruptedException e)
        {
            System.err.println("InterruptedException");
//            System.exit(1);
        }

    }

}
