package ctf.networking;

import networking.Message;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import networking.QueuedSendThread;

/**
 * Constructs a ListenServer, which handles the majority of the CTF netowrking
 */
public class ListenServer extends Thread
{

    /**
     * A BlockingQueue of messages we have received
     */
    private final BlockingQueue<Message> inmessages;
    /**
     * The address and port we will connect to
     */
    private InetSocketAddress address;
    /**
     * A HashSet of the connected clients to this server
     */
    private LinkedList<Socket> clients = new LinkedList<Socket>();
    private LinkedList<Integer> clientID = new LinkedList<Integer>();
    private LinkedList<ListenServerThread> clientThreads = new LinkedList<ListenServerThread>();

    /**
     *
     * @param host The name of the host to connect to
     * @param port The port that the socket should listen on. Note that
     * this must be >= 0.
     */
    public ListenServer(String host, int port)
    {
        address = new InetSocketAddress(host, port);
        inmessages = new LinkedBlockingQueue<Message>();
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
            int index = clientID.indexOf(m.getID());
            if(index < 0)
            {
                System.err.println("Client " + m.getID() + " not connected");
                return;
            }
            ListenServerThread thread = clientThreads.get(index);
            thread.put(m);
        }
        catch (Exception e)
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
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Starts a new thread to handle all the network I/O
     * and returns.
     *    
     */
    public void listen()
    {
        this.start();
    }

    /**
     * Performs the actual I/O loop using a ServerSocket
     */
    @Override
    public void run()
    {
        ServerSocket serverSocket = null;
        boolean listening = true;
        try
        {
            serverSocket = new ServerSocket();
            serverSocket.bind(address);

            while (listening)
            {
                Socket newClient = serverSocket.accept();

                ListenServerThread thread = new ListenServerThread(newClient);
                thread.start();

                clients.add(newClient);
                clientThreads.add(thread);
                clientID.add(thread.getID());

                System.out.println("New Client added.");
            }
            serverSocket.close();
        }
        catch (IOException e)
        {
            System.err.println("Networking error.");
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private class ListenServerThread extends Thread
    {

        /**
         * A BlockingQueue of messages to send
         */
        private final BlockingQueue<Message> outmessages;
        /**
         * The connected client
         */
        private Socket socket;
        /**
         * The ID for this client
         */
        private int ID;

        /**
         *
         * @param socket - The socket that will be handled
         */
        public ListenServerThread(Socket socket)
        {
            super("ListenMultiServerThread");
            this.socket = socket;

            ID = new Random().nextInt();
            outmessages = new LinkedBlockingQueue<Message>();
        }

        public int getID()
        {
            return ID;
        }

        public void put(Message m)
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

        @Override
        public void run()
        {
            try
            {
                PrintWriter networkout = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader networkin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;


                /* Spin off and start the output thread */
                QueuedSendThread thread = new QueuedSendThread(networkout, outmessages);
                thread.start();

                /* Delay for a second */
                sleep(1000);

                outmessages.put(new Message("CONNECTED", 0));
                outmessages.put(new Message("CONFIG", ID));

                while (true)
                {
                    while (networkin.ready() == false);

                    inputLine = networkin.readLine();

                    if (inputLine.equals("exit"))
                    {
                        outmessages.put(new Message("Bye.", ID));
                        break;
                    }

                    inmessages.put(new Message(inputLine, true));
                }
                networkout.close();
                networkin.close();
                socket.close();
            }
            catch (Exception e)
            {
                System.out.println("Thread error");
                e.printStackTrace();
            }
        }
    }
}
