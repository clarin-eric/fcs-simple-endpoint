package eu.clarin.sru.server.fcs;

import java.util.List;

import eu.clarin.sru.server.SRUException;


/**
 * An interface for abstracting resource info inventories. This class allows you
 * to provide a version of a resource info inventory tailored to your
 * environment, e.g. retrieving the information from a database, etc.
 * <p>
 * The implementation of this interface <em>must</em> be thread-safe.
 * </p>
 */
public interface ResourceInfoInventory {
    /**
     * Constant for a (synthetic) persistent identifier identifying the top-most
     * (= root) resource in the resource info inventory.
     */
    public static final String PID_ROOT = "root";


    /**
     * Destroy the resource info inventory. Use this method for any cleanup the
     * resource info inventory needs to perform upon termination, i.e. closing
     * of persistent database connections, etc.
     */
    public void destroy();


    /**
     * Get a list of all resources sub-ordinate to a resource identified by a
     * given persistent identifier.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @param pid
     *            the persistent identifier of the superior resource
     * @return a list of all sub-ordinate ResourceInfo or <code>null</code> if
     *         not applicable
     * @throws SRUException
     *             if an error occurred
     */
    public List<ResourceInfo> getResourceInfoList(String pid)
            throws SRUException;

} // interface ResourceInfoInventory
