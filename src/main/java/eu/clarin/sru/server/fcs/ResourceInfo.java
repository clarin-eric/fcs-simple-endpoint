package eu.clarin.sru.server.fcs;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * This class implements a resource info record, which provides supplementary
 * information about a resource that is available at the endpoint.
 * 
 * @see ResourceInfoInventory
 */
public class ResourceInfo {
    private final String pid;
    @Deprecated
    private final int resourceCount;
    private final Map<String, String> title;
    private final Map<String, String> description;
    private final String landingPageURI;
    private final List<String> languages;
    private final List<String> availableDataViews;
    private List<ResourceInfo> subResources;
    

    /**
     * Constructor.
     *
     * @param pid
     *            the persistent identifier of the resource
     * @param resourceCount
     *            the number of items within the resource or <code>-1</code> if
     *            not applicable
     * @param title
     *            the title of the resource represented as a map with pairs of
     *            language code and title
     * @param description
     *            the description of the resource represented as a map with
     *            pairs of language code and description or <code>null</code> if
     *            not applicable
     * @param landingPageURI
     *            a URI to the landing page of the resource or <code>null</code>
     *            if not applicable
     * @param languages
     *            the languages represented within this resource represented as
     *            a list of ISO-632-3 three letter language codes
     * @param subResources
     *            a list of resource sub-ordinate to this resource or
     *            <code>null</code> if not applicable
     */
    public ResourceInfo(String pid, int resourceCount,
            Map<String, String> title, Map<String, String> description,
            String landingPageURI, List<String> languages,
            List<String> availableDataViews,
            List<ResourceInfo> subResources) {
        if (pid == null) {
            throw new NullPointerException("id == null");
        }
        this.pid = pid;
        this.resourceCount = (resourceCount > 0) ? resourceCount : -1;
        if (title == null) {
            throw new NullPointerException("title == null");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title is empty");
        }
        this.title = Collections.unmodifiableMap(title);
        if ((description != null) && !description.isEmpty()) {
            this.description = Collections.unmodifiableMap(description);
        } else {
            this.description = null;
        }
        this.landingPageURI = landingPageURI;
        if (languages == null) {
            throw new NullPointerException("languages == null");
        }
        if (languages.isEmpty()) {
            throw new IllegalArgumentException("languages is empty");
        }
        this.languages = languages;
        if (availableDataViews == null){
        	throw new IllegalArgumentException("available data views == null");
        }
        if (availableDataViews.isEmpty()){
        	throw new IllegalArgumentException("available data views are empty");
        }
        this.availableDataViews = availableDataViews;
        if ((subResources != null) && !subResources.isEmpty()) {
            this.subResources = Collections.unmodifiableList(subResources);
        } else {
            this.subResources = null;
        }        
    }


    /**
     * Get the persistent identifier of this resource.
     *
     * @return a string representing the persistent identifier of this resource
     */
    public String getPid() {
        return pid;
    }


    /**
     * Get the number of resources within this resource. If this resource has
     * sub-ordinate resources, this number should be the sum of all items within
     * the sub-ordinate resources plus the number of items within this resource.
     *
     * @return a number of items or <code>-1</code> if not applicable
     */
    @Deprecated
    public int getResourceCount() {
        return resourceCount;
    }


    /**
     * Determine, if this resource has sub-resources.
     *
     * @return <code>true</code> if the resource has sub-resources,
     *         <code>false</code> otherwise
     */
    public boolean hasSubResources() {
        return subResources != null;
    }


    /**
     * Get the title of this resource.
     *
     * @return a Map of titles keyed by language code
     */
    public Map<String, String> getTitle() {
        return title;
    }


    /**
     * Get the title of the resource for a specific language code.
     *
     * @param language
     *            the language code
     * @return the title for the language code or <code>null</code> if no title
     *         for this language code exists
     */
    public String getTitle(String language) {
        return title.get(language);
    }


    /**
     * Get the description of this resource.
     *
     * @return a Map of descriptions keyed by language code
     */
    public Map<String, String> getDescription() {
        return description;
    }


    /**
     * Get the description of the resource for a specific language code.
     *
     * @param language
     *            the language code
     * @return the description for the language code or <code>null</code> if no
     *         title for this language code exists
     */
    public String getDescription(String language) {
        return (description != null) ? description.get(language) : null;
    }


    /**
     * Get the landing page of this resource.
     *
     * @return the landing page of this resource or <code>null</code> if not
     *         applicable
     */
    public String getLandingPageURI() {
        return landingPageURI;
    }


    /**
     * Get the list of languages in this resource represented as ISO-632-3 three
     * letter language code.
     *
     * @return the list of languages in this resource as a list of ISO-632-3
     *         three letter language codes.
     */
    public List<String> getLanguages() {
        return languages;
    }


    /**
     * Get the direct sub-ordinate resources of this resource.
     *
     * @return a list of resources or <code>null</code> if this resource has no
     *         sub-ordinate resources
     */
    public List<ResourceInfo> getSubResources() {
        return subResources;
    }


	public List<String> getAvailableDataViews() {
		return availableDataViews;
	}


} // class ResourceInfo
