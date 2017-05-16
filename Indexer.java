package edu.co.usbcali.ir.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import edu.co.usbcali.ir.constants.LuceneConstants;

/**
 * Generates the index files adding all the TXT documents with news
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class Indexer
{
    /**
     * Index writer object to add and index the TXT documents 
     */
    private IndexWriter writer;

    /**
     * Configures the index writer for index the files in a directory
     * @param indexDirPath Path to save the index files
     * @throws IOException Throws an exception when there is a problem in the index directory
     */
    public Indexer(String indexDirPath) throws IOException
    {
        FSDirectory indexDirectory = FSDirectory.open(Paths.get(indexDirPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        
        writer = new IndexWriter(indexDirectory, config);
    }

    /**
     * Closes the index writer object
     * @throws CorruptIndexException Throws an exception when the index writer object has a problem
     * @throws IOException Throws an exception when there is a problem working in the directory
     */
    public void close() throws CorruptIndexException, IOException
    {
        writer.close();
    }

    /**
     * Generates a Document object from a news saved in a TXT. The document has the news content, file name
     * and the file path to access it in the server
     * @param file TXT file with a news
     * @return Document with the required info
     * @throws IOException Throws an exception if the file cannot be loaded successfully
     */
    private Document getDocument(File file) throws IOException
    {
        Document document = new Document();
        
        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS);
        type.setStored(true);
        type.setTokenized(true);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorPositions(true);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPayloads(true);
        
        Field contentField = new Field(LuceneConstants.CONTENTS, getContent(file), type);
        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), type);
        Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), type);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }
    
    /**
     * Gets the news content from a TXT file
     * @param file TXT file with a news
     * @return String with the news content
     * @throws IOException Throws an exception if the file cannot be loaded successfully
     */
    private String getContent(File file) throws IOException
    {
        String content = "";
        try (FileReader reader = new FileReader(file))
        {
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            
            while((line = buffer.readLine()) != null)
            {
                content += line;
            }
        }
        
        return content;
    }

    /**
     * Adds a document to the index using the configured index writer
     * @param file TXT file with a news to generate a Document
     * @throws IOException Throws an exception if the file cannot be loaded successfully
     */
    private void indexFile(File file) throws IOException
    {
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    /**
     * Reads the TXT files from the directory and add each one to the index
     * @param dataDirPath Path from the TXT news files
     * @param filter Filter to read only TXT files in the directory
     * @return Number of indexed documents
     * @throws IOException Throws an exception when there is an issue reading or writing the files
     */
    public int createIndex(String dataDirPath, FileFilter filter) throws IOException
    {
        File[] files = new File(dataDirPath).listFiles();

        for (File file : files)
        {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() 
                    && filter.accept(file))
            {
                indexFile(file);
            }
        }
        
        return writer.numDocs();
    }
}
