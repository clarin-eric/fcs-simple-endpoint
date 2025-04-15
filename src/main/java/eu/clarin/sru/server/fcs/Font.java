/**
 * This software is copyright (c) 2013-2025 by
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to provide information about a required Fonts that is
 * available by the Endpoint.
 */
public class Font {
    private final String id;
    private final String name;
    private final URI descriptionUrl;
    private final String license; // SPDX or custom
    private final List<URI> licenseUrls;
    private final List<DownloadUrl> downloadUrls;

    /**
     * Constructor.
     *
     * @param id             the identifier for this font
     * @param name           the name of the font
     * @param descriptionUrl the URL to a description of the font or
     *                       <code>null</code> if not applicable
     * @param license        the license for using the font (in SPDX format; may
     *                       specify custom licenses, too, then use the
     *                       <code>licenseUrls</code> to provide the actual text of
     *                       the license)
     * @param licenseUrls    a list of URLs to license files for the font or
     *                       <code>null</code> if not applicable
     * @param downloadUrls   a list of download URLs to retrieve the actual font
     *                       files
     */
    public Font(String id, String name, URI descriptionUrl, String license, List<URI> licenseUrls,
            List<DownloadUrl> downloadUrls) {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id is empty");
        }
        this.id = id;

        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;

        this.descriptionUrl = descriptionUrl;

        if (license == null) {
            throw new NullPointerException("license == null");
        }
        if (license.isEmpty()) {
            throw new IllegalArgumentException("license is empty");
        }
        this.license = license;

        if ((licenseUrls != null) && !licenseUrls.isEmpty()) {
            this.licenseUrls = Collections.unmodifiableList(licenseUrls);
        } else {
            this.licenseUrls = null;
        }

        if (downloadUrls == null) {
            throw new NullPointerException("downloadUrls == null");
        }
        if (downloadUrls.isEmpty()) {
            throw new IllegalArgumentException("downloadUrls is empty");
        }
        this.downloadUrls = Collections.unmodifiableList(downloadUrls);
    }

    /**
     * Get the referrable identifier of this font.
     *
     * @return a string representing the identifier of this font
     */
    public String getId() {
        return id;
    }

    /**
     * Get the name of this font.
     *
     * @return the name of this font
     */
    public String getName() {
        return name;
    }

    /**
     * Get a URI to a description of this font.
     *
     * @return the description URI of this font or <code>null</code> if not
     *         applicable
     */
    public URI getDescriptionUrl() {
        return descriptionUrl;
    }

    /**
     * Get the SPDX license description of this font.
     *
     * @return a string with an SPDX license expression
     */
    public String getLicense() {
        return license;
    }

    /**
     * Get the list of license URLs about this font.
     *
     * @return the list of URI to license files for fonts or <code>null</code> if
     *         not applicable
     */
    public List<URI> getLicenseURLs() {
        return licenseUrls;
    }

    /**
     * Get the list of download URLs that allows to retrieve the font files.
     *
     * @return the list of download URI infos for fonts
     */
    public List<DownloadUrl> getDownloadURLs() {
        return downloadUrls;
    }

    public static class DownloadUrl {
        private final URI url;
        private final String variant;
        private final String fontFamily;

        /**
         * Constructor.
         *
         * @param url        the URL to download the font file
         * @param variant    the font variant (e.g., regular, bold, ...), or
         *                   <code>null</code> if not applicable
         * @param fontFamily the font-family of the font or <code>null</code> if not
         *                   applicable
         */
        public DownloadUrl(URI url, String variant, String fontFamily) {
            if (url == null) {
                throw new NullPointerException("url == null");
            }
            this.url = url;

            this.variant = variant;
            this.fontFamily = fontFamily;
        }

        /**
         * Get the font download URL.
         *
         * @return the URI to download the font file
         */
        public URI getURL() {
            return url;
        }

        /**
         * Get the font variant that is available via this URL.
         *
         * @return the font variant or <code>null</code> if not applicable
         */
        public String getVariant() {
            return variant;
        }

        /**
         * Get the font-family name of this font.
         *
         * @return a string with the font-family for this font or <code>null</code> if
         *         not applicable
         */
        public String getFontFamily() {
            return fontFamily;
        }
    }

}
