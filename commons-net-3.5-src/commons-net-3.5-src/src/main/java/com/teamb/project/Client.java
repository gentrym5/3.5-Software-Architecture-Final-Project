package com.teamb.project;
import org.apache.commons.net.ftp.FTPFile;

public class Client {
    public static void main(String[] args) {
        //this comes from the Apache 3.5 library
        FTPFile oldFile = new FTPFile();
        oldFile.setName("budget.pdf");

        //wrap it in our Adapter
        FileSystem myFile = new FTPAdapter(oldFile);

        //app only talks to the Interface
        System.out.println("File: " + myFile.getFileName());
    }
}