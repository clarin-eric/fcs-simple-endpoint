/**
 * This software is copyright (c) 2013-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * This class implements a resource info record, which provides supplementary
 * information about a resource that is available at the endpoint.
 *
 * @see EndpointDescription
 */
public class ResourceInfo {

    /**
     * Enumeration to indicate the content encoding of a layer.
     */
    public enum AvailabilityRestriction {
        /**
         * No authentication is required.
         */
        NONE,
        /**
         * Only authentication via home institution is required.
         */
        AUTH_ONLY,
        /**
         * An additional 'userID' attribute is required for authentication.
         */
        PERSONAL_IDENTIFIER
    }
    
    private final String pid;
    private final Map<String, String> title;
    private final Map<String, String> description;
    private final Map<String, String> institution;
    private final String landingPageURI;
    private final List<String> languages;
    private final AvailabilityRestriction availabilityRestriction;
    private final List<DataView> availableDataViews;
    private final List<Layer> availableLayers;
    private final List<ResourceInfo> subResources;


    /**
     * Constructor.
     *
     * @param pid
     *            the persistent identifier of the resource
     * @param title
     *            the title of the resource represented as a map with pairs of
     *            language code and title
     * @param description
     *            the description of the resource represented as a map with
     *            pairs of language code and description or <code>null</code> if
     *            not applicable
     * @param institution
     *            the institution of the resource represented as a map with
     *            pairs of language code and institution names or <code>null</code>
     *            if not applicable
     * @param landingPageURI
     *            a URI to the landing page of the resource or <code>null</code>
     *            if not applicable
     * @param languages
     *            the languages represented within this resource represented as
     *            a list of ISO-632-3 three letter language codes
     * @param availabilityRestriction
     *            possible access restrictions / requirements for using this
     *            resource
     * @param availableDataViews
     *            the list of available data views for this resource
     * @param availableLayers
     *            the list if layers available for Advanced Search or
     *            <code>null</code> if not applicable
     * @param subResources
     *            a list of resource sub-ordinate to this resource or
     *            <code>null</code> if not applicable
     */
    public ResourceInfo(String pid, Map<String, String> title,
            Map<String, String> description, Map<String, String> institution,
            String landingPageURI, List<String> languages,
            AvailabilityRestriction availabilityRestriction,
            List<DataView> availableDataViews, List<Layer> availableLayers,
            List<ResourceInfo> subResources) {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        this.pid = pid;

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
        if ((institution != null) && !institution.isEmpty()) {
            this.institution = Collections.unmodifiableMap(institution);
        } else {
            this.institution = null;
        }

        this.landingPageURI = landingPageURI;
        if (languages == null) {
            throw new NullPointerException("languages == null");
        }
        if (languages.isEmpty()) {
            throw new IllegalArgumentException("languages is empty");
        }
        this.languages = languages;

        if (availabilityRestriction == null) {
            throw new NullPointerException("availabilityRestriction == null");
        }
        this.availabilityRestriction = availabilityRestriction;

        if (availableDataViews == null) {
            throw new IllegalArgumentException("availableDataViews == null");
        }
        this.availableDataViews =
                Collections.unmodifiableList(availableDataViews);

        if ((availableLayers != null) && !availableDataViews.isEmpty()) {
            this.availableLayers =
                    Collections.unmodifiableList(availableLayers);
        } else {
            this.availableLayers = null;
        }

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
     * Get the institution of this resource.
     * 
     * This is an optional attribute for endpoints that host resources from
     * different institution but still want to bundle them in one endpoint.
     * If not specified then it is the default institution that hosts the
     * FCS endpoint.
     *
     * @return the institution of this resource or <code>null</code> if not
     *         applicable or specified
     */
    public Map<String, String> getInstitution() {
        return institution;
    }


    /**
     * Get the institution of this resource for a specific language code.
     *
     * @param language
     *            the language code
     * @return the institution of this resource or <code>null</code> if not
     *         applicable or specified
     */
    public String getInstitution(String language) {
        return (institution != null) ? institution.get(language) : null;
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
     * Check, if this resource has any kind of availability restriction.
     *
     * @return <code>true</code> if resource declares an availability
     *         restriction of any kind.
     */
    public boolean hasAvailabilityRestriction() {
        return !AvailabilityRestriction.NONE.equals(availabilityRestriction);
    }
    
    
    /**
     * Get the availabiliy restriction for this resource.
     * 
     * @return the availability restriction, or <code>null</code> if none.
     */
    public AvailabilityRestriction getAvailabilityRestriction() {
        return availabilityRestriction;
    }


    /**
     * Get the list of data views that are available for this resource.
     *
     * @return the list of data views
     */
    public List<DataView> getAvailableDataViews() {
        return availableDataViews;
    }


    /**
     * Get the list of layers that are available in Advanced Search for this
     * resource.
     *
     * @return the list of layers or <code>null</code>
     */
    public List<Layer> getAvailableLayers() {
        return availableLayers;
    }


    /**
     * Check if any layers are available for Advanced Search
     *
     * @return <code>true</code> if any layer for Advanced Search is available,
     *         <code>false</code> otherwise
     */
    public boolean hasAvailableLayers() {
        return (availableLayers != null);
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

} // class ResourceInfo
