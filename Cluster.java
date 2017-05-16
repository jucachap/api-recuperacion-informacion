package edu.co.usbcali.ir.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.search.ScoreDoc;

import edu.co.usbcali.ir.constants.PathsConstants;

/**
 * Makes all the clustering process for the recovered documents based on method using Covering Array
 * to find the best cluster for documents
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class Cluster
{
    /**
     * Gets the best clustering for the documents recovered from the indexed files
     * @param scoreDocs Recovered documents
     * @param results Max number of documents recovered
     * @return List with documents clustering
     * @throws Exception Throws an exception if the Covering Array file is not read successfully or if the 
     * max of results is bigger than 20 (max columns of Covering Array)
     */
    @SuppressWarnings("unused")
    public List<List<Integer>> getDocumentsClustering(ScoreDoc[] scoreDocs, int results) throws Exception
    {
        if (results > 20)
        {
            throw new Exception("The max number of documents to clustering is 20");
        }
        
        List<Integer[]> coveringArray = getCoveringArray();
        
        float bestSSE = Float.MAX_VALUE;
        List<List<Integer>> bestClusters = null;
        Integer[] bestCoverArray = null;
        
        for (Integer[] coverArray : coveringArray)
        {
            List<List<Integer>> clusters = getCluster(coverArray, results);
            List<Float> clustersSSE = getClusterSSE(clusters, scoreDocs);
            float lineSSE = getLineSSE(clustersSSE);
            
            if (lineSSE < bestSSE)
            {
                bestSSE = lineSSE;
                bestClusters = clusters.stream().collect(Collectors.toList());
                bestCoverArray = Arrays.copyOf(coverArray, coverArray.length, Integer[].class);
            }
        }
        
        return bestClusters;
    }
    
    /**
     * Groups the documents in the respective cluster for one Covering Array line
     * @param items Covering Array line
     * @param results Max number of documents recovered
     * @return List with documents clustering for the line
     */
    private List<List<Integer>> getCluster(Integer[] items, int results)
    {
        List<List<Integer>> clusters = new ArrayList<>();
        
        for (int i = 0; i < 5; i++)
        {
            List<Integer> c = new ArrayList<>();
            clusters.add(c);
        }
        
        for (int i = 0; i < results; i++)
        {
            clusters.get(items[i]).add(i);
        }
        
        return clusters;
    }
    
    /**
     * Gets the Sum of Square Error (SSE) from each cluster
     * @param clusters Documents clustering
     * @param scoreDocs Recovered documents to get the score per document
     * @return List with SSE result for each cluster
     */
    private List<Float> getClusterSSE(List<List<Integer>> clusters, ScoreDoc[] scoreDocs)
    {
        List<Float> clustersSSE = new ArrayList<>();
        List<Float> centroids = getClusterCentroid(clusters, scoreDocs);
        
        for (int i = 0; i < clusters.size(); i++)
        {
            List<Integer> cluster = clusters.get(i);
            
            float centroid = centroids.get(i);
            float sse = 0;
            
            for (Integer docIndex : cluster)
            {
                if (docIndex < scoreDocs.length)
                {
                    sse += Math.sqrt(Math.pow(centroid, 2) + Math.pow(scoreDocs[docIndex].score, 2));
                }
            }
            
            clustersSSE.add(sse);
        }
        
        return clustersSSE;
    }
    
    /**
     * Gets the centroid for each cluster
     * @param clusters Documents clustering
     * @param scoreDocs Recovered documents to get the score per document
     * @return List with centroids for each cluster
     */
    private List<Float> getClusterCentroid(List<List<Integer>> clusters, ScoreDoc[] scoreDocs)
    {
        List<Float> centroids = new ArrayList<>();
        
        for (int i = 0; i < clusters.size(); i++)
        {
            List<Integer> cluster = clusters.get(i);
            
            float scoreAcum = 0;
            float docCount = 0;
            
            for (Integer docIndex : cluster)
            {
                if (docIndex < scoreDocs.length)
                {
                    docCount++;
                    scoreAcum += scoreDocs[docIndex].score;
                }
            }
            
            float centroid = 0;
            if (docCount != 0)
            {
                float docsDiv = 1 / docCount;
                centroid = docsDiv * scoreAcum;
            }
            
            centroids.add(centroid);
        }
        
        return centroids;
    }
    
    /**
     * Sum the SSE result for each cluster
     * @param clustersSSE SSE for each cluster
     * @return Sum of the clusters´ SSE
     */
    private float getLineSSE(List<Float> clustersSSE)
    {
        float lineSSE = 0;
        for (Float sse : clustersSSE)
        {
            lineSSE += sse;
        }
        
        return lineSSE;
    }
    
    /**
     * Gets the cluster that include a specific document
     * @param clusters Documents clustering
     * @param document Document index
     * @return Cluster label for the document
     */
    public String getDocumentCluster(List<List<Integer>> clusters, int document)
    {
        String cluster = "";
        
        for (int i = 0; i < clusters.size(); i++)
        {
            if (clusters.get(i).contains(document))
            {
                cluster = "Cluster " + i;
                break;
            }
        }
        
        return cluster;
    }
    
    /**
     * Reads and parse the Covering Array file converting it into array which for the clustering process
     * @return Covering Array into a List
     * @throws IOException Throws an exception if the Covering Array file is not read successfully
     */
    private List<Integer[]> getCoveringArray() throws IOException
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
            PathsConstants.COVERING_ARRAY_FILE);
        
        BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
        
        List<Integer[]> coveringArray = new ArrayList<>();
        
        String line;
        while ((line = buffer.readLine()) != null)
        {
            String[] sItems = line.split(" ");
            Integer[] items = new Integer[sItems.length];
            
            for (int i = 0; i < sItems.length; i++)
            {
                items[i] = Integer.parseInt(sItems[i]);
            }
            
            coveringArray.add(items);
        }
        
        return coveringArray;
    }
}
