package com.notebridge.project.response;

public class FileResponse {
    private String fileName;
    private byte[] fileContent;

    public FileResponse(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
