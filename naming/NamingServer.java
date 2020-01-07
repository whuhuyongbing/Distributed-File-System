package naming;

import common.Branch;
import common.FileTree;
import common.Leaf;
import common.Path;
import rmi.RMIException;
import rmi.Skeleton;
import storage.Command;
import storage.Storage;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
    private ExecutorService executorService;
    private FileTree fileTree;
    private List<Command> servers;
    private Skeleton<Service> serviceSkeleton;
    private Skeleton<Registration> registrationSkeleton;
    private  Map<Command, Storage> storageMap;

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        this.executorService = Executors.newCachedThreadPool();
        this.fileTree = new FileTree();
        this.servers = new ArrayList<>();
        this.storageMap = new HashMap<>();
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        this.serviceSkeleton = new Skeleton<>(Service.class, this,new InetSocketAddress(NamingStubs.SERVICE_PORT));
        this.serviceSkeleton.start();
        this.registrationSkeleton = new Skeleton<>(Registration.class,this,new InetSocketAddress(NamingStubs.REGISTRATION_PORT));
        this.registrationSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method waits for both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
       /** throw new UnsupportedOperationException("not implemented");*/
       this.serviceSkeleton.stop();
       this.registrationSkeleton.stop();
       this.stopped(null);

    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    /** The following methods are documented in Service.java.*/
    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        return this.fileTree.isDirectory(path);

    }
    /** Lists the contents of a directory.

     @param directory The directory to be listed.
     @return An array of the directory entries. The entries are not
     guaranteed to be in any particular order.
     @throws FileNotFoundException If the given path does not refer to a
     directory.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        Branch dir = this.fileTree.cd(directory);
        String[] res = new String[dir.list.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = dir.list.get(i).getName();
        return res;
    }
    /** Creates the given file, if it does not exist.

     @param file Path at which the file is to be created.
     @return <code>true</code> if the file is created successfully,
     <code>false</code> otherwise. The file is not created if a file
     or directory with the given name already exists.
     @throws FileNotFoundException If the parent directory does not exist.
     @throws IllegalStateException If no storage servers are connected to the
     naming server.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public boolean createFile(Path file) throws RMIException, FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");**/
        if (file == null)
            throw new NullPointerException();
        if (file.isRoot())
            return false;
        if(!this.isDirectory(file.parent()))
            throw new FileNotFoundException("parent directory dose not exist");
        if (this.fileTree.isExist(file))
            return false;
        else if (this.servers.size() == 0)
            throw new IllegalArgumentException("no storage servers are connected to the naming server");
        else
        {

            Random random = new Random();
            Command server = this.servers.get(random.nextInt(this.servers.size()));
            Storage client_stub = this.storageMap.get(server);
            try {
                this.fileTree.touch(file, client_stub);
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            }
            try {
                return server.create(file);
            } catch (RMIException e) {
                throw new RMIException("can not contact the storage server");
            }
        }
    }

    /** Creates the given directory, if it does not exist.

     @param directory Path at which the directory is to be created.
     @return <code>true</code> if the directory is created successfully,
     <code>false</code> otherwise. The directory is not created if
     a file or directory with the given name already exists.
     @throws FileNotFoundException If the parent directory does not exist.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (directory.isRoot())
            return false;
        Branch parent = this.fileTree.cd(directory.parent());
        try {
            this.fileTree.mkdir(parent, directory.last());
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }

    }
    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/

        return fileTree.delete(path);

    }
    /** Returns a stub for the storage server hosting a file.

     @param file Path to the file.
     @return A stub for communicating with the storage server.
     @throws FileNotFoundException If the file does not exist.
     */
    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        Leaf f = this.fileTree.getFile(file);
        return f.getStorage();
    }
    //The following method is documented in Registration.java
    /** Registers a storage server with the naming server.

     <p>
     The storage server notifies the naming server of the files that it is
     hosting. Note that the storage server does not notify the naming server
     of any directories. The naming server attempts to add as many of these
     files as possible to its directory tree. The naming server then replies
     to the storage server with a subset of these files that the storage
     server must delete from its local storage.

     <p>
     After the storage server has deleted the files as commanded, it must
     prune its directory tree by removing all directories under which no
     files can be found. This includes, for example, directories which
     contain only empty directories.

     @param client_stub Storage server client service stub. This will be
     given to clients when operations need to be performed
     on a file on the storage server.
     @param command_stub Storage server command service stub. This will be
     used by the naming server to issue commands that
     modify the directory tree on the storage server.
     @param files The list of files stored on the storage server. This list
     is merged with the directory tree already present on the
     naming server. Duplicate filenames are dropped.
     @return A list of duplicate files to delete on the local storage of the
     registering storage server.
     @throws IllegalStateException If the storage server is already
     registered.
     @throws NullPointerException If any of the arguments is
     <code>null</code>.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public Path[] register(Storage client_stub, Command command_stub, Path[] files)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (client_stub == null || command_stub == null || files == null)
            throw new NullPointerException();
        if (this.servers.contains(command_stub))
            throw new IllegalStateException("the storage server is already registered");
        List<Path> extra = new ArrayList<>();
        this.servers.add(command_stub);
        this.storageMap.put(command_stub, client_stub);
        for (Path path : files) {
            if (path.isRoot())
                continue;
            try {
                this.fileTree.touch(path, client_stub);
            }  catch (FileAlreadyExistsException | FileNotFoundException e) {
                extra.add(path);
            }
        }
        return extra.toArray(new Path[extra.size()]);
    }
}
