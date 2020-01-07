package storage;

import rmi.Stub;

import java.net.InetSocketAddress;

/** Default port numbers for the naming server and convenience methods for
 making naming server stubs. */
public abstract class StorageServerStubs
{
    /** Default storage server command service port. */
    public static final int     COMMAND_PORT = 8000;
    /** Default storage server storage port. */
    public static final int     STORAGE_PORT = 8001;

    /** Returns a stub for a naming server client service interface.

     @param hostname Naming server hostname.
     @param port Client service interface port.
     */
    public static Command command(String hostname, int port)
    {
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        return Stub.create(Command.class, address);
    }

    /** Returns a stub for a naming server client service interface.

     <p>
     The default port is used.

     @param hostname Naming server hostname.
     */
    public static Command command(String hostname)
    {
        return command(hostname, COMMAND_PORT);
    }

    /** Returns a stub for a naming server registration interface.

     @param hostname Naming server hostname.
     @param port Registration interface port.
     */
    public static Storage storage(String hostname, int port)
    {
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        return Stub.create(Storage.class, address);
    }

    /** Returns a stub for a naming server registration interface.

     <p>
     The default port is used.

     @param hostname Naming server hostname.
     */
    public static Storage storage(String hostname)
    {
        return storage(hostname, STORAGE_PORT);
    }
}
