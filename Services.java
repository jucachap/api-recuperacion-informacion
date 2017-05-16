package edu.co.usbcali.ir.rest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.co.usbcali.ir.constants.LuceneConstants;
import edu.co.usbcali.ir.constants.PathsConstants;
import edu.co.usbcali.ir.processes.Cluster;
import edu.co.usbcali.ir.processes.ExtractReutersNews;
import edu.co.usbcali.ir.processes.Indexer;
import edu.co.usbcali.ir.processes.Searcher;
import edu.co.usbcali.ir.util.TextFileFilter;

/**
 * Provides the necessary services to make all the IR processes: extract, index and search documents
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
@Path("/reuters")
public class Services
{
    /**
     * The servlet context for the application
     */
    @Context 
    private ServletContext context;
    
    /**
     * Makes a search in indexed documents. The documents can be clustered using Covering Array method
     * @param searchQuery Query to search in documents
     * @param cluster Indicates if the documents are going to be clustered or not
     * @param results Max of documents returned in the search
     * @return A JSON output with recovered documents and elapsed time to search them  
     */
    @GET
    @Path("/search/{searchQuery}/{cluster}/{results}")
    @Produces("application/json")
    @SuppressWarnings({ "unchecked" })
    public Response getDocuments(@PathParam("searchQuery") String searchQuery,
        @PathParam("cluster") boolean cluster, @PathParam("results") int results)
    {
        try
        {
            Searcher searcher = new Searcher(context.getRealPath(PathsConstants.INDEX_PATH));
            
            long startTime = System.currentTimeMillis();
            TopDocs hits = searcher.search(searchQuery, results);
            long endTime = System.currentTimeMillis();
            
            Cluster clus = new Cluster();
            List<List<Integer>> clusters = null;
            
            if (cluster)
            {
                List<List<Integer>> clusteringResult = clus.getDocumentsClustering(hits.scoreDocs, results);
                clusters = clusteringResult.stream().collect(Collectors.toList());
            }
            
            JSONObject json = new JSONObject();
            JSONArray docs = new JSONArray();
            
            for (int i = 0; i < hits.scoreDocs.length; i++)
            {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                Document doc = searcher.getDocument(scoreDoc);
                
                JSONObject d = new JSONObject();
                d.put("path", doc.get(LuceneConstants.FILE_PATH));
                
                if (cluster)
                {
                    d.put("cluster", clus.getDocumentCluster(clusters, i));
                }
                else
                {
                    d.put("cluster", "Default");
                }
                
                docs.add(d);
            }
            
            json.put("documents", docs);
            json.put("time", (endTime - startTime));
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
        catch (IOException | ParseException ex)
        {
            JSONObject json = new JSONObject();
            json.put("status", "Exception");
            json.put("response", ex.getMessage());
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
        catch (Exception ex)
        {
            JSONObject json = new JSONObject();
            json.put("status", "Exception");
            json.put("response", ex.getMessage());
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
    }
    
    /**
     * Extracts the TXT documents from Reuters SGM files
     * @return A JSON output with the process result
     */
    @GET
    @Path("/extract")
    @Produces("application/json")
    @SuppressWarnings({ "unchecked" })
    public Response extractNews()
    {
        try
        {
            ExtractReutersNews extract = new ExtractReutersNews();
            
            long startTime = System.currentTimeMillis();
            extract.extractNewsFromSgm(context.getRealPath(PathsConstants.SGM_PATH),
                context.getRealPath(PathsConstants.DATA_PATH));
            long endTime = System.currentTimeMillis();
            
            JSONObject json = new JSONObject();
            json.put("status", "Success");
            json.put("response", "News extracted Successfully");
            json.put("time", (endTime - startTime));
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
        catch (IOException ex)
        {
            JSONObject json = new JSONObject();
            json.put("status", "Exception");
            json.put("response", ex.getMessage());
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
    }
    
    /**
     * Indexes the TXT documents for searches 
     * @return A JSON output with the process result
     */
    @GET
    @Path("/index")
    @Produces("application/json")
    @SuppressWarnings({ "unchecked" })
    public Response indexNews()
    {
        try
        {
            Indexer indexer = new Indexer(context.getRealPath(PathsConstants.INDEX_PATH));
            
            long startTime = System.currentTimeMillis();
            int numIndexed = indexer.createIndex(context.getRealPath(PathsConstants.DATA_PATH),
                new TextFileFilter());
            long endTime = System.currentTimeMillis();
            
            indexer.close();
            
            JSONObject json = new JSONObject();
            json.put("status", "Success");
            json.put("response", "Files indexed Successfully");
            json.put("indexed", numIndexed);
            json.put("time", (endTime - startTime));
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
        catch (IOException ex)
        {
            JSONObject json = new JSONObject();
            json.put("status", "Exception");
            json.put("response", ex.getMessage());
            
            return Response.status(200).entity(json.toJSONString()).build();
        }
    }
}
