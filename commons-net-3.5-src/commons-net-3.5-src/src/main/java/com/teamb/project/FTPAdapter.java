package com.teamb.project;

import org.apache.commons.net.ftp.FTPFile;

public class FTPAdapter implements FileSystem {
    private FTPFile apacheFile;

    public FTPAdapter(FTPFile apacheFile) {
        this.apacheFile = apacheFile;
    }

    @Override
    public String getFileName() {
        return apacheFile.getName();
    }

    @Override
    public boolean isAFolder() {
        return apacheFile.isDirectory();
    }

    @Override
    public long getFileSize() {
        return apacheFile.getSize();
    }
}