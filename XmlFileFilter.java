package edu.co.usbcali.ir.util;

import java.io.File;
import java.io.FileFilter;

/**
 * XML filter for files in a specific paths
 * @author Joan Romero
 * @author Juan Carlos Chaparro
 */
public class XmlFileFilter implements FileFilter
{
    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(File path)
    {
        return path.getName().toLowerCase().endsWith(".xml");
    }
}
