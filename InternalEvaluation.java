package edu.co.usbcali.ir.processes;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.ScoreDoc;

/**
 * Realizes the Internal Evaluation Index for the clusters
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class InternalEvaluation
{
    /**
     * Represents the clusters for the evaluation
     */
    private List<List<Integer>> clusters;
    
    /**
     * Recovered documents
     */
    private ScoreDoc[] scoreDocs;
    
    /**
     * Centroids' list for each cluster
     */
    private List<Float> centroids;
    
    
    /**
     * Average for all the recovered documents
     */
    private float docsAvg;
    
    /**
     * Initializes the necessary properties for the Internal Evaluation index
     * @param clusters Documents clustering
     * @param scoreDocs Recovered documents to get the score per document
     */
    public InternalEvaluation(List<List<Integer>> clusters, ScoreDoc[] scoreDocs)
    {
        this.clusters = clusters;
        this.scoreDocs = scoreDocs;
        
        docsAvg = getDocumentsAvg();
        centroids = getClusterCentroid();
    }
    
    /**
     * Gets the Square Sum Between Clusters (SSB) index
     * @return SSB index result
     */
    public Float getSSBResult()
    {
        List<Float> clustersSSB = getClusterSSB();
        float ssb = getClustersTestSum(clustersSSB);
        
        return ssb;
    }
    
    /**
     * Gets the Square Sum Between Clusters (SSB) index for each cluster
     * @return List with SSB result for each cluster
     */
    private List<Float> getClusterSSB()
    {
        List<Float> clustersSSB = new ArrayList<>();
        
        for (int i = 0; i < clusters.size(); i++)
        {
            List<Integer> cluster = clusters.get(i);
            
            float centroid = centroids.get(i);
            float docCount = cluster.size();
            
            float ssb = (float) (docCount * Math.pow((centroid - docsAvg), 2));
            clustersSSB.add(ssb);
        }
        
        return clustersSSB;
    }
    
    /**
     * Gets the Square Sum Within Clusters (SSW) index
     * @return SSW index result
     */
    public Float getSSWResult()
    {
        List<Float> clustersSSW = getClusterSSW();
        float ssw = getClustersTestSum(clustersSSW);
        
        return ssw;
    }
    
    /**
     * Gets the Square Sum Within Clusters (SSW) index for each cluster
     * @return List with SSW result for each cluster
     */
    private List<Float> getClusterSSW()
    {
        List<Float> clustersSSW = new ArrayList<>();
        
        for (int i = 0; i < clusters.size(); i++)
        {
            List<Integer> cluster = clusters.get(i);
            
            float centroid = centroids.get(i);
            float ssw = 0;
            
            for (Integer docIndex : cluster)
            {
                if (docIndex < scoreDocs.length)
                {
                    ssw += Math.pow((scoreDocs[docIndex].score - centroid), 2);
                }
            }
            
            clustersSSW.add(ssw);
        }
        
        return clustersSSW;
    }
    
    
    /**
     * Gets the centroid for each cluster
     * @return List with centroids for each cluster
     */
    private List<Float> getClusterCentroid()
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
     * Sum the internal evaluation index result for each cluster
     * @param clustersTest Test result for each cluster
     * @return Sum of the clusters' test result
     */
    private float getClustersTestSum(List<Float> clustersTest)
    {
        float testSum = 0;
        for (Float ssb : clustersTest)
        {
            testSum += ssb;
        }
        
        return testSum;
    }
    
    /**
     * Gets the recovered documents average
     * @return Document average
     */
    private Float getDocumentsAvg()
    {
        float scoreAcum = 0;
        float docCount = scoreDocs.length;
        
        for (ScoreDoc scoreDoc : scoreDocs)
        {
            scoreAcum += scoreDoc.score;
        }
        
        float docsAvg = 0;
        if (docCount != 0)
        {
            float docsDiv = 1 / docCount;
            docsAvg = docsDiv * scoreAcum;
        }
        
        return docsAvg;
    }
    
    /**
     * Gets the clusters' centroids
     * @return Centroids list
     */
    public List<Float> getCentroids()
    {
        return centroids;
    }
}
