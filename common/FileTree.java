package common;

import storage.Storage;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-01 2:17 a.m.
 * @description
 */
public class FileTree implements Serializable {

    Branch root;

    public FileTree() {
        this.root = new Branch(Constant.BACKSLASH_ROOT);
    }


    /** determine a path is a directory
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public boolean isDirectory(Path path) throws FileNotFoundException {
        if (path.isRoot())
            return true;
        Iterator<String> files = path.iterator();
        Branch cur = this.root;

        while (files.hasNext()) {
            String file = files.next();
            if (!this.isDirectory(cur, file))
                return false;
            else
                cur = this.cd(cur, file);
        }
        return true;
    }

    /** make a directory in a branch
     *
     * @param dir directory name
     * @param parent parent directory
     */
    public void mkdir(Branch parent, String dir) throws FileAlreadyExistsException {
        if (parent == null || dir == null)
            throw new NullPointerException();
        try {
            if (this.isDirectory(parent, dir) || this.isFile(parent, dir))
                throw new FileAlreadyExistsException("directory already exist");
        } catch (FileNotFoundException e) {
            Branch branch = new Branch(dir);
            parent.list.add(branch);
        }
    }

    /** touch a file with storage
     *
     * @param file filename
     * @param parent parent directory
     * @param storage
     */
    public void touch(String file, Branch parent, Storage storage) throws FileAlreadyExistsException {
        if (file == null || parent == null)
            throw new NullPointerException();
        try {
            if (this.isFile(parent, file) || this.isDirectory(parent, file))
                throw new FileAlreadyExistsException(file);
        } catch (FileNotFoundException e) {
            Leaf f = new Leaf(file, storage);
            parent.list.add(f);
        }

    }

    /** touch a file without storage
     *
     * @param file
     * @param branch
     */
    public void touch(String file, Branch branch) throws FileAlreadyExistsException {
        this.touch(file, branch, null);
    }

    public void touch(Path path, Storage storage) throws FileNotFoundException, FileAlreadyExistsException {
        this.mkdirs(this.root, path.parent());
        Branch parent = this.cd(path.parent());
        this.touch(path.last(), parent,storage);
    }

    /** list branches of a  branch
     *
     * @param parent
     * @return
     */
    public List<Branch> listBranch(Branch parent) {
        if (parent == null)
            throw new NullPointerException();
        List<Branch> children = new ArrayList<>();
        for (Node n : parent.list) {
            if (n instanceof Branch)
                children.add((Branch) n);
        }
        return children;
    }

    /** list leafs of a branch
     *
     * @param parent
     * @return
     */
    public List<Leaf> listLeaf(Branch parent) {
        if (parent == null)
            throw new NullPointerException("parent can not be null");
        List<Leaf> children = new ArrayList<>();
        for (Node n : parent.list) {
            if (n instanceof Leaf)
                children.add((Leaf) n);
        }
        return children;
    }

    /** get child node
     *
     * @param branch
     * @param file
     * @return node if exists else null
     */

    private Node getChild(Branch branch, String file) {
        if (branch == null || file == null)
            throw new NullPointerException();

        for (Node node : branch.list) {
            if (node.equals(file))
                return node;
        }
        return null;
    }

    /** determine a node is a file or not
     *
     * @param parent
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public boolean isFile(Branch parent, String file) throws FileNotFoundException{
        if (parent == null || file == null)
            throw new NullPointerException();
        for (Node n : parent.list) {
            if (n instanceof Leaf && n.getName().equals(file))
                return true;
            if (n instanceof Node && n.getName().equals(file))
                return false;
        }
        throw new FileNotFoundException();
    }

    /** determine whether a directory
     *
     * @param parent
     * @param dir
     * @return
     * @throws FileNotFoundException
     */

    public boolean isDirectory (Branch parent, String dir) throws FileNotFoundException{
        if (parent == null || dir == null)
            throw new NullPointerException();
        if (dir.equals(Constant.BACKSLASH_ROOT))
            return true;
        for (Node n : parent.list) {
            if (n instanceof Leaf && n.getName().equals(dir))
                return false;
            if (n instanceof Branch && n.getName().equals(dir))
                return true;
        }
        throw new FileNotFoundException();
    }

    /** cd to a directory
     *
     * @param parent
     * @param dir
     * @return
     * @throws FileNotFoundException
     */
    public Branch cd(Branch parent, String dir) throws FileNotFoundException {
        Branch branch = null;
        if (parent == null || dir == null)
            throw new NullPointerException();
        if (dir.equals(Constant.BACKSLASH_ROOT))
            return this.root;
        if (this.isDirectory(parent, dir)) {
            for (Node n : parent.list) {
                if (n instanceof Branch && n.getName().equals(dir)) {
                    branch = (Branch) n;
                }
            }
        } else
            throw new FileNotFoundException("it is a file");
        return branch;
    }

    public Branch cd(Path path) throws FileNotFoundException {
        if (path == null)
            throw new NullPointerException();
        Iterator<String> files = path.iterator();
        Branch cur = this.root;
        while (files.hasNext()) {
            String f = files.next();
            cur = this.cd(cur, f);
        }
        return cur;
    }

    public Leaf getFile(Path path) throws FileNotFoundException {
        Branch parent = this.cd(path.parent());
        for (Node n : parent.list) {
            if (n instanceof Leaf && n.getName().equals(path.last()))
                return (Leaf) n;
        }
        throw new FileNotFoundException();
    }

    /** make directory recursively
     *
     * @param parent
     * @param path
     */
    public void mkdirs(Branch parent, Path path) {
        Iterator<String> iterator = path.iterator();
        Branch cur = parent;
        while (iterator.hasNext()) {
            String f = iterator.next();
            try {
                this.mkdir(cur, f);
                cur = this.cd(cur, f);
            } catch (FileAlreadyExistsException e) {
                try {
                    cur = this.cd(cur, f);
                } catch (FileNotFoundException ex) {
                    throw new IllegalArgumentException("there is a file name in the path");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /** make dir recursively in the root
     *
     * @param path
     */
    public void mkdirs(Path path) {
        this.mkdirs(this.root, path);
    }

    /** delete file
     *
     * @param parent
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public boolean delete(Branch parent, String file) throws FileNotFoundException {
        if (parent == null || file == null)
            throw new NullPointerException();
        if (file.equals(Constant.BACKSLASH_ROOT))
            throw new FileNotFoundException("root can not be deleted");
        if (this.isFile(parent, file))
            for (int i = 0; i < parent.list.size(); i++) {
                if (parent.list.get(i).equals(file) && parent.list.get(i) instanceof Leaf) {
                    parent.list.remove(i);
                    return true;
                } else if (parent.list.get(i).equals(file) && parent.list.get(i) instanceof Branch)
                    throw new FileNotFoundException("can not delete a branch");
            }
        return false;
    }

    /** delete a file if it's a file or empty directory, delete directly
     *
     *
     * @param path path
     * @throws  FileNotFoundException if path is not exist
     * @return true if delete success otherwise false
     */
    public boolean delete(Path path) throws FileNotFoundException {
        if (path == null)
            throw new NullPointerException();
        if (path.isRoot())
            return false;
        Branch parent = this.cd(path.parent());
        Node child = this.getChild(parent, path.last());
        /** leaf  */
        if (child instanceof Leaf) {
            parent.list.remove(child);
            return true;
        }
        /** branch */
        else {
            Branch ch = (Branch) child;
            if (ch.list.size() == 0) {
                parent.list.remove(child);
                return true;
            }
            else
                throw new IllegalArgumentException("directory has file can not be deleted");
        }

    }

    public boolean isExist(Path path) {
        if (path == null)
            throw new NullPointerException("path can not be null");
        if (path.isRoot())
           return true;
        try {
            Branch parent = this.cd(path.parent());
            for (Node n : parent.list) {
                if (n.getName().equals(path.last()))
                    return true;
            }
            return false;
        } catch (FileNotFoundException e) {
            return false;
        }

    }
}
