package edu.co.usbcali.ir.processes;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.co.usbcali.ir.constants.LuceneConstants;

/**
 * Recovers the documents from the indexed documents based in a search query
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class Searcher
{
    /**
     * Index searcher object to read the index files
     */
    private IndexSearcher indexSearcher;
    
    /**
     * Query parser object to analyze the string query
     */
    private QueryParser queryParser;
    
    /**
     * Query object to search the documents in the index
     */
    private Query query;

    /**
     * Initializes the Searcher objects, configuring the IndexSearcher and the QueryParser
     * @param indexDirPath Path from the index files
     * @throws IOException Throws an exception when there is a problem in the index directory
     */
    public Searcher(String indexDirPath) throws IOException
    {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        
        indexSearcher = new IndexSearcher(reader);
        queryParser = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
    }

    /**
     * Searches the passed query in the index files recovering the matched documents
     * @param searchQuery Query to search in documents
     * @param results Max of documents returned in the search
     * @return Recovered documents 
     * @throws IOException Throws an exception when there is a problem in the index directory
     * @throws ParseException Throws an exception if the search query cannot be parsed successfully
     */
    public TopDocs search(String searchQuery, int results) throws IOException, ParseException
    {
        query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, results);
    }

    /**
     * Gets the Document object from a recovered document from the index
     * @param scoreDoc Recovered document
     * @return Document object from the index
     * @throws CorruptIndexException Throws an exception when the index searcher object has a problem
     * @throws IOException Throws an exception when there is a problem in the index directory
     */
    public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException
    {
        return indexSearcher.doc(scoreDoc.doc);
    }
}
