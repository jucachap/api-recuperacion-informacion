package edu.co.usbcali.ir.processes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.benchmark.utils.ExtractReuters;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.co.usbcali.ir.util.WriteFile;

/**
 * Extracts and save the news into TXT files. This files can be created from two sources: Reuters SGM and
 * Reuters XML files 
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class ExtractReutersNews
{
    /**
     * The servlet context for the application
     */
    @Context 
    private ServletContext context;
    
    /**
     * Extracts the news into TXT files from Reuters SGM files
     * @param sgmDirPath Path from the Reuters SGM files 
     * @param dataDirPath Path to save the TXT files
     * @throws IOException Throws an exception if the extract process fails reading or writing files
     */
    public void extractNewsFromSgm(String sgmDirPath, String dataDirPath) throws IOException
    {
        Path sgmPath = Paths.get(sgmDirPath);
        Path dataPath = Paths.get(dataDirPath);
        
        WriteFile.createFolder(dataDirPath);
        
        ExtractReuters extractor = new ExtractReuters(sgmPath, dataPath);
        extractor.extract();
    }
    
    /**
     * Extracts the news into TXT files from Reuters XML files
     * @param xmlDirPath Path from the Reuters XML files
     * @param dataDirPath Path to save the TXT files
     * @param filter Filter to read only XML files in the directory
     * @throws JDOMException Throws an exception when there is a problem reading the XML file
     * @throws IOException Throws an exception when there is an issue reading or writing the files
     * @deprecated Replaced by {@link #extractNewsFromSgm()}
     */
    @Deprecated
    public void extractNewsFromXml(String xmlDirPath, String dataDirPath, FileFilter filter) 
            throws JDOMException, IOException
    {
        File[] files = new File(xmlDirPath).listFiles();

        for (File file : files)
        {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() 
                    && filter.accept(file))
            {
                generateNewFromXml(file, dataDirPath);
            }
        }
    }
    
    /**
     * Generates the TXT files from a Reuters XML file
     * @param xmlFile Reuters XML file with news
     * @param dataDirPath Path to save the TXT files
     * @throws JDOMException Throws an exception when there is a problem reading the XML file
     * @throws IOException Throws an exception when there is an issue reading or writing the files
     */
    @Deprecated
    private void generateNewFromXml(File xmlFile, String dataDirPath) throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();

        String baseFileName = FilenameUtils.removeExtension(xmlFile.getName());
        String lineSeparator = System.getProperty("line.separator");

        Document document = (Document) builder.build(xmlFile);
        Element rootNode = document.getRootElement();
        List<Element> listReuters = rootNode.getChildren("REUTERS");

        for (Element reuters : listReuters)
        {
            String newId = reuters.getAttributeValue("NEWID");
            String date = reuters.getChildText("DATE");

            List<Element> listText = reuters.getChildren("TEXT");
            Element text = listText.get(0);

            String title = text.getChildText("TITLE");
            String body = text.getChildText("BODY");

            String reuterContent = title + lineSeparator + date + lineSeparator + lineSeparator + body;
            String reuterPath = dataDirPath + "/" + baseFileName + "-" + newId + ".txt";
            
            WriteFile.createFolder(dataDirPath);
            WriteFile.writeFileContent(reuterContent, reuterPath);
        }
    }
}
