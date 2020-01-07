package rmi;

import common.Info;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/** RMI Skeleton's Listening Thread

 <p>
 Upon receiving a client request, this thread creates multiple
 client threads to communicate with the client stubs.
 */
public class ListenThread<T> implements Runnable {
    private Class<T> tClass;
    private T server;
    private Skeleton<T> skeleton;
    private ServerSocket serverSocket;      // the socket waiting for client request
    private volatile boolean isCancelled;   // make the flag volatile to ensure thread safety

    public ListenThread(Class<T> tClass, T server, Skeleton<T> skeleton, ServerSocket serverSocket)
    {
        if (tClass == null || server == null || serverSocket == null)
            throw new NullPointerException();

        this.isCancelled = false;
        this.tClass = tClass;
        this.server = server;
        this.skeleton = skeleton;
        this.serverSocket = serverSocket;
    }

    @Override
    public synchronized void run()
    {
        isCancelled = false;
        try {
            while (!isCancelled) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientThread(clientSocket)).start();
            }
        }
        catch (IOException ioe) {
//             ioe.printStackTrace();
            cancel();
        }
    }

    /** Stop the thread and close the listening socket
     */
    public void cancel()
    {
        isCancelled = true;

        if (serverSocket != null && !serverSocket.isClosed())
            try {
                serverSocket.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }

    }

    private class ClientThread implements Runnable
    {
        /**The socket to communicate with client stubs*/
        private Socket clientSocket;

        public ClientThread(Socket clientSocket)
        {
            if (clientSocket == null)
                throw new NullPointerException();

            this.clientSocket = clientSocket;
        }

        @Override
        public synchronized void run()
        {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            Object resultObj;

            try {
                if (clientSocket == null || clientSocket.isClosed())
                    return;

                input = new ObjectInputStream(clientSocket.getInputStream());
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                output.flush();
                Info info = (Info) input.readObject();

                try {
                    Class<?>[] argsTypes = info.getArgsTypes();
                    Method invokedMethod = tClass.getMethod(info.getMethodName(), argsTypes);
                    Object[] args = info.getArgs();
                    if (args.length == 1 && args[0] == null) {
                        args[0] = argsTypes[0].cast(null);
                    }
                    resultObj = invokedMethod.invoke(server, args);
                }
                catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                        NullPointerException | SecurityException | IllegalArgumentException e) {
                    /** Transmit remote exceptions back to the client*/
                    resultObj = e;
                }

                output.writeObject(resultObj);
            }
            /** This exception is caused by readObject()*/
            catch (ClassNotFoundException | IOException e) {
                skeleton.service_error(new RMIException(e));

                try {
                    /** Transmit exceptions back to the client*/
                    output = new ObjectOutputStream(clientSocket.getOutputStream());
                    output.writeObject(e);
                }
                catch (IOException ioe) {
//                     ioe.printStackTrace();
                }
            } finally {
                try {
                    if (input != null) input.close();
                    if (output != null) output.close();
                    if (clientSocket != null) clientSocket.close();
                }
                catch (IOException ioe) {
//                     ioe.printStackTrace();
                }
            }
        }
    }

}
