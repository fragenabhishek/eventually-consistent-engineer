package com.fragenabhishek.designpatterns.structural;

import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================
 *  COMPOSITE PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Treat individual objects and groups of objects uniformly.
 *            Compose objects into tree structures.
 *
 *  Problem:  A file system has Files and Folders. A Folder can contain Files AND
 *            other Folders. You want to call getSize() on anything — file or folder —
 *            and get the right answer without type-checking.
 *
 *  Solution: Define a common FileSystem interface. File returns its own size.
 *            Folder recursively sums the sizes of all its children (files + subfolders).
 *            Client code treats both identically.
 *
 *  Structure:
 *    FileSystem  →  Component interface (common for leaf and composite)
 *    File        →  Leaf (has a size, no children)
 *    Folder      →  Composite (has children which are also FileSystem items)
 *
 *  Key: getSize() on Folder is recursive — it works because both File and Folder
 *       implement the same interface. This is the power of the Composite pattern.
 *
 *  Real-world: HTML DOM tree, Swing JComponent, org chart hierarchies, menu systems
 * =====================================================
 */

// --- Component interface ---
public interface FileSystem {
    int getSize();
    String getName();
}

// --- Leaf: a single file with a fixed size ---
class File implements FileSystem {
    private final String name;
    private final int size;

    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getName() {
        return name;
    }
}

// --- Composite: a folder that contains files and/or other folders ---
class Folder implements FileSystem {
    private final String name;
    private final List<FileSystem> children = new ArrayList<>();

    public Folder(String name) {
        this.name = name;
    }

    public void add(FileSystem item) {
        children.add(item);
    }

    @Override
    public int getSize() {
        int total = 0;
        for (FileSystem child : children) {
            total += child.getSize();           // recursive — works for files AND subfolders
        }
        return total;
    }

    @Override
    public String getName() {
        return name;
    }
}

// --- Demo ---
class CompositeDemo {
    public static void main(String[] args) {
        // Build a tree structure:
        //   root/
        //   ├── fileA (10)
        //   ├── fileB (20)
        //   └── subFolder/
        //       ├── fileC (30)
        //       └── fileD (40)

        File fileA = new File("fileA.txt", 10);
        File fileB = new File("fileB.txt", 20);
        File fileC = new File("fileC.txt", 30);
        File fileD = new File("fileD.txt", 40);

        Folder subFolder = new Folder("subFolder");
        subFolder.add(fileC);
        subFolder.add(fileD);

        Folder root = new Folder("root");
        root.add(fileA);
        root.add(fileB);
        root.add(subFolder);

        // Uniform treatment — getSize() works on both files and folders
        System.out.println(fileA.getName() + " size: " + fileA.getSize());           // 10
        System.out.println(subFolder.getName() + " size: " + subFolder.getSize());   // 70
        System.out.println(root.getName() + " total size: " + root.getSize());       // 100
    }
}
