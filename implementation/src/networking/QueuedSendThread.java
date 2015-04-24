/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package networking;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

/**
 * Maintains a blocking queue of messages to send to the given
 * streams
 *
 */
public class QueuedSendThread extends Thread
{

    private final BlockingQueue<Message> queue;
    private BufferedReader read;
    private PrintWriter networkout;

    /**
     * Constructs the queue that will send data
     *
     * @param outbound The output stream to send messages to
     * @param queue The BlockingQueue to monitor. This should be used somewhere
     * in your application.
     */
    public QueuedSendThread(PrintWriter outbound,
                             BlockingQueue<Message> queue)
    {
        this.queue = queue;
        networkout = outbound;
        queue = null;
    }

    /**
     * Is used to send messages from here to the other end of the connection
     * Invoke with .start()
     */
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                String fromUser = queue.take().toString();

                System.out.println("Sending " + fromUser);
                networkout.println(fromUser);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
