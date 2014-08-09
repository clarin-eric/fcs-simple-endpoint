package eu.clarin.sru.server.fcs;

import java.net.URI;
import java.util.List;

import eu.clarin.sru.server.SRUException;


/**
 * An interface for abstracting resource endpoint descriptions. This interface
 * allows you to provide a version of a endpoint description tailored to your
 * environment.
 * <p>
 * The implementation of this interface <em>must</em> be thread-safe.
 * </p>
 */
public interface EndpointDescription {
    /**
     * Constant for a (synthetic) persistent identifier identifying the top-most
     * (= root) resources in the resource inventory.
     */
    public static final String PID_ROOT = "root";

    /**
     * Destroy the resource info inventory. Use this method for any cleanup the
     * resource info inventory needs to perform upon termination, i.e. closing
     * of persistent database connections, etc.
     */
    public void destroy();


    /**
     * Get the list of capabilities supported by this endpoint. The list
     * contains the appropriate URIs defined by the CLARIN-FCS specification to
     * indicate support for certain capabilities. This list <em>must</em> always
     * contain at least
     * <code>http://clarin.eu/fcs/capability/basic-search</code> for the
     * <em>Basic Search</em> capability.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @return the list of capabilities supported by this endpoint
     */
    public List<URI> getCapabilities();


    /**
     * Get the list of data views supported by this endpoint. This list
     * <em>must</em> always contain an entry for the
     * <em>Generic Hits (HITS)</em> data view.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @return the list of data views supported by this endpoint
     */
    public List<DataView> getSupportedDataViews();


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
    public List<ResourceInfo> getResourceList(String pid)
            throws SRUException;

} // interface EndpointDescription
