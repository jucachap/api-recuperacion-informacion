package edu.co.usbcali.ir.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to write files and generate folder from specific paths
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class WriteFile
{
    /**
     * Writes a content into a given file
     * @param fileContent File content
     * @param filePath File path
     * @throws IOException Throws an exception if cannot write successfully the file
     */
    public static void writeFileContent(String fileContent, String filePath) throws IOException
    {
        File file = new File(filePath);
        try (BufferedWriter buffer = new BufferedWriter(new FileWriter(file)))
        {
            buffer.write(fileContent);
        }
    }
    
    /**
     * Creates a folder from a given path
     * @param path Folder path
     */
    public static void createFolder(String path)
    {
        File folder = new File(path);
        if (folder.isDirectory() && !folder.exists())
        {
            folder.mkdir();
        }
    }
}
