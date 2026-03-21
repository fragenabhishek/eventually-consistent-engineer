package com.fragenabhishek.designpatterns.structural;

import java.util.ArrayList;
import java.util.List;

public interface FileSystem {
    int getSize();
}

class File implements FileSystem{
    int size;
    @Override
    public int getSize() {
        return size;
    }
}

class Folder implements FileSystem{
    List<FileSystem> children;
    @Override
    public int getSize() {
        int total = 0;
        for (FileSystem child : children){
            total = total + child.getSize();
        }
        return total;
    }
}

class MainTest1{
    public static void main(String[] args) {

        File fileA = new File();
        fileA.size = 10;

        File fileB = new File();
        fileB.size = 20;

        File fileC = new File();
        fileC.size = 30;

        File fileD = new File();
        fileD.size = 40;

        Folder folderX = new Folder();
        folderX.children = new ArrayList<>();
        folderX.children.add(fileC);
        folderX.children.add(fileD);

        Folder folderRoot = new Folder();
        folderRoot.children = new ArrayList<>();
        folderRoot.children.add(fileA);
        folderRoot.children.add(fileB);
        folderRoot.children.add(folderX);

        System.out.println("Total size of folderRoot: 0" + folderRoot.getSize());


    }
}
