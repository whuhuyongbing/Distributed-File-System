package storage;

import common.Path;
import naming.Registration;
import rmi.RMIException;
import rmi.Skeleton;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
    private File root;
    private volatile boolean cancel;

    /** Creates a storage server, given a directory on the local filesystem.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (root == null)
            throw new NullPointerException("root can not be null");
        this.root = root;
        this.cancel = false;
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    @SuppressWarnings("unchecked")
    public synchronized void start(String hostname, Registration naming_server) throws RMIException, UnknownHostException, FileNotFoundException
    {
       /** throw new UnsupportedOperationException("not implemented");*/
       if (this.cancel)
           throw new RMIException("it can not be restart");

        Skeleton<Command> commandSkeleton = new Skeleton(Command.class, this);
        Skeleton<Storage> storageSkeleton = new Skeleton(Storage.class, this);
        commandSkeleton.start();
        storageSkeleton.start();
        Storage client_stub = StorageServerStubs.storage(hostname, storageSkeleton.getSocketAddr().getPort());
        Command command_stub = StorageServerStubs.command(hostname, commandSkeleton.getSocketAddr().getPort());
        Path[] PathReg = Path.list(this.root);
        Path[] badPaths = naming_server.register(client_stub, command_stub, PathReg);
        if (badPaths != null && badPaths.length != 0) {
            for (Path p : badPaths) {
                File file = p.toFile(this.root);
                file.delete();
                deleteBlankDirectory(p.parent());
            }

        }
    }

    /** delete the blank directory
     *
     *
     * @param path
     */
    private void deleteBlankDirectory(Path path) {
        if (path.isRoot())
            return;
        File file = path.toFile(this.root);
        if (file.isDirectory() && file.list().length == 0) {
            file.delete();
        }
        deleteBlankDirectory(path.parent());
    }


    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        this.cancel = true;
        this.stopped(null);

    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    /** Returns the length of a file, in bytes.

     @param file Path to the file.
     @return The length of the file.
     @throws FileNotFoundException If the file cannot be found or the path
     refers to a directory.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public synchronized long size(Path file) throws FileNotFoundException, NullPointerException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (file == null)
            throw new NullPointerException();
        File f = file.toFile(this.root);
        if (!f.exists() || f.isDirectory())
            throw new FileNotFoundException("file not exist");
        return f.length();
    }

    /** Reads a sequence of bytes from a file.

     @param file Path to the file.
     @param offset Offset into the file to the beginning of the sequence.
     @param length The number of bytes to be read.
     @return An array containing the bytes read. If the call succeeds, the
     number of bytes read is equal to the number of bytes requested.
     @throws IndexOutOfBoundsException If the sequence specified by
     <code>offset</code> and
     <code>length</code> is outside the
     bounds of the file, or if
     <code>length</code> is negative.
     @throws FileNotFoundException If the file cannot be found or the path
     refers to a directory.
     @throws IOException If the file read cannot be completed on the server.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public synchronized byte[] read(Path file, long offset, int length) throws FileNotFoundException, IOException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (offset < 0l || length < 0)
            throw new IndexOutOfBoundsException();
        byte[] result = null;
        try {
            File f = file.toFile(this.root);
            result = new byte[length];
            FileInputStream fileInputStream = new FileInputStream(f);
            if (f.length() < length || offset >= f.length())
                /** handle empty file return byte[0] not null*/
                if (f.length() == 0 && length == 0 && offset == 0l)
                    return result;
                else
                    throw new IndexOutOfBoundsException();
            fileInputStream.getChannel().position(offset);
            fileInputStream.read(result, 0, length);
            fileInputStream.close();
       } catch (NoSuchFileException e) {
           throw new FileNotFoundException();
       }
        return result;
    }

    /** Writes bytes to a file.

     @param file Path to the file.
     @param offset Offset into the file where data is to be written.
     @param data Array of bytes to be written.
     @throws IndexOutOfBoundsException If <code>offset</code> is negative.
     @throws FileNotFoundException If the file cannot be found or the path
     refers to a directory.
     @throws IOException If the file write cannot be completed on the server.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public synchronized void write(Path file, long offset, byte[] data) throws FileNotFoundException, IOException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (offset < 0)
            throw new IndexOutOfBoundsException();
        File f = file.toFile(this.root);
        if (f.isDirectory() || !f.exists()) {
            throw new FileNotFoundException();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.getChannel().position(offset);
        fileOutputStream.write(data);
        fileOutputStream.close();
    }

    /** The following methods are documented in Command.java.*/

    /** Creates a file on the storage server.

     @param file Path to the file to be created. The parent directory will be
     created if it does not exist. This path may not be the root
     directory.
     @return <code>true</code> if the file is created; <code>false</code>
     if it cannot be created.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public synchronized boolean create(Path file)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (file == null)
            throw new NullPointerException();
        if (file.isRoot())
            return false;
        File parent = null;
        parent = file.parent().toFile(this.root);

        if (!parent.exists()) {
            try {
                Files.createDirectories(parent.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.createFile(file.toFile(this.root).toPath());
            return true;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }

    }

    /** Deletes a file or directory on the storage server.

     <p>
     If the file is a directory and cannot be deleted, some, all, or none of
     its contents may be deleted by this operation.

     @param path Path to the file or directory to be deleted. The root
     directory cannot be deleted.
     @return <code>true</code> if the file or directory is deleted;
     <code>false</code> otherwise.
     @throws RMIException If the call cannot be completed due to a network
     error.
     */
    @Override
    public synchronized boolean delete(Path path)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (path.isRoot())
            return false;
        File file = path.toFile(this.root);
        return delete(file);
    }
    private boolean delete(File file) {
        if (file == null || !file.exists())
            return false;
        if (file.isFile())
            return file.delete();
        else {
            for (File f : file.listFiles()) {
                if (f.isFile())
                    f.delete();
                else
                    delete(f);
            }
            return file.delete();
        }
    }
}
