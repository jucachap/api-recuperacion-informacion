package edu.co.usbcali.ir.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

/**
 * Converts the Reuters SGM file into Reuters XML using the standard format for XML file
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class SgmToXmlConverter
{
    /**
     * Generates a XLM file from the SGM file reading the content file and adding tags for XML format
     * @param path SGM file path
     * @throws IOException Throws an exception if the process fails reading or writing files 
     * @throws FileNotFoundException 
     */
    public void generateXmlFromSgm(String path) throws FileNotFoundException, IOException
    {
        String xmlContent = "";
        String lineSeparator = System.getProperty("line.separator");

        File sgmFile = new File(path);
        try (FileReader reader = new FileReader(sgmFile))
        {
            BufferedReader buffer = new BufferedReader(reader);

            String line;

            xmlContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator;
            xmlContent += "<collection>" + lineSeparator;

            while ((line = buffer.readLine()) != null)
            {
                if (!line.contains("<!DOCTYPE lewis SYSTEM \"lewis.dtd\">"))
                {
                    line = processCharactersInLine(line);
                    xmlContent += line + lineSeparator;
                }
            }

            xmlContent += "</collection>";
        }

        String xmlPath = FilenameUtils.removeExtension(sgmFile.getName()) + ".xml";

        WriteFile.writeFileContent(xmlContent, xmlPath);
        
    }
    
    /**
     * Cleans the line from special characters 
     * @param line File line 
     * @return Formatted line
     */
    private String processCharactersInLine(String line)
    {
        for (int i = 0; i < 160; i++)
        {
            line = line.replace("&#" + i + ";", "");
            line = line.replace("&#" + String.format("%03d", i) + ";", "");
        }

        line = line.replace("&lt;", "");

        return line;
    }
}
