package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/** Distributed filesystem paths.

    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Serializable
{
    private final List<String> components;

    public List<String> getComponents() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < this.components.size(); i++) {
            result.add(this.components.get(i));
        }
        return result;
    }
    /** Creates a new path which represents the root directory. */
    public Path()
    {

        /**throw new UnsupportedOperationException("not implemented");*/
        this.components = new ArrayList<String>();
        this.components.add(Constant.BACKSLASH_ROOT);
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
        //throw new UnsupportedOperationException("not implemented");
        this.components = path.getComponents();
        if (component.contains(Constant.BACKSLASH_ROOT) || component.contains(Constant.COLON_RESERVED) || component.trim().length() == 0)
            throw new IllegalArgumentException("the component can not contain colon and forward slash");
        else
            this.components.add(component);
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
        //throw new UnsupportedOperationException("not implemented");
        if (!path.startsWith(Constant.BACKSLASH_ROOT)) {
            throw new IllegalArgumentException("the path have to start with separator");
        } else if (path.contains(Constant.COLON_RESERVED)) {
            throw new IllegalArgumentException("the path can not contain colon");
        } else if (path.trim().length() == 0) {
            throw new IllegalArgumentException("the path can not be blank");
        } else {
            this.components = new ArrayList<>();
            this.components.add(Constant.BACKSLASH_ROOT);
            String[] directories = path.split(Constant.BACKSLASH_ROOT);
            for (String directory : directories) {
                if (directory.trim().length() != 0) {
                    this.components.add(directory);
                }
            }
        }
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        String[] list = new String[this.components.size() - 1];

        for (int i = 1; i < this.components.size(); i++) {
            list[i-1] = this.components.get(i);
        }

        return Arrays.asList(list).iterator();
    }

    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
        /**throw new UnsupportedOperationException("not implemented");*/

        List<java.nio.file.Path> collects;
        try {
            collects = Files.walk(Paths.get(directory.getPath()))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new FileNotFoundException("the root directory does not exist");
        }
        String[] paths = new String[collects.size()];
        for (int i = 0; i < collects.size(); i++) {
            paths[i] = collects.get(i).toString().substring(directory.getPath().length());
        }
        Path[] result = new Path[paths.length];
        for (int i = 0; i < paths.length; i++) {
            result[i] = new Path(paths[i]);
        }
        return result;
    }


    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
       /** throw new UnsupportedOperationException("not implemented");*/
       return this.components.size() == 1;
    }

    /** Returns the path to the parent of this path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent() throws IllegalArgumentException
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (this.isRoot()) {
            throw new IllegalArgumentException("the root path has no parent");
        } else {
            Path cur = new Path(Constant.BACKSLASH_ROOT);
            for (int i = 1; i < this.components.size() - 1; i++){
                cur = new Path(cur, this.getComponents().get(i));
            }
            return cur;
        }
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        if (this.isRoot())
            throw new IllegalArgumentException("the root path has no last component");
        else
            return this.components.get(this.components.size() - 1);
    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        return this.toString().startsWith(other.toString());
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        return new File(root.toString() + this.toString());
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        return this.toString().equals(other.toString());
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        return this.toString().hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
        /**throw new UnsupportedOperationException("not implemented");*/
        String result = Constant.BACKSLASH_ROOT;
       for (int i = 1; i < this.getComponents().size(); i++) {
           if (i == this.getComponents().size() - 1)
               result += getComponents().get(i);
           else {
               result += this.getComponents().get(i) + Constant.BACKSLASH_ROOT;
           }
       }
        return result;
    }
}