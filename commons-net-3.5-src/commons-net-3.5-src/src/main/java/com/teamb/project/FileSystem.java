package com.teamb.project;

public interface FileSystem {
    String getFileName();
    boolean isAFolder();
    long getFileSize();
}