/**
 * This software is copyright (c) 2011-2022 by
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
package eu.clarin.sru.server.fcs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Verification;

import eu.clarin.sru.server.SRUAuthenticationInfo;
import eu.clarin.sru.server.SRUAuthenticationInfoProvider;
import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUException;


public class AuthenticationProvider implements SRUAuthenticationInfoProvider {
    private static final Logger logger =
            LoggerFactory.getLogger(AuthenticationProvider.class);
    private final List<Verifier> verifiers;


    private AuthenticationProvider(List<Verifier> verifiers) {
        this.verifiers = verifiers;
    }


    public int getKeyCount() {
        if (verifiers != null) {
            return verifiers.size();
        } else {
            return 0;
        }
    }


    @Override
    public SRUAuthenticationInfo getAuthenticationInfo(
            HttpServletRequest request) throws SRUException {
        String value = request.getHeader("Authentication");
        if (value != null) {
            if (value.regionMatches(true, 0, "Bearer", 0, 6)) {
                String rawToken = value.substring(6).trim();
                if (!rawToken.isEmpty()) {
                    return checkToken(rawToken);
                }
            }
        }
        return null;
    }


    private AuthenticationInfo checkToken(String rawToken) throws SRUException {
        try {
            DecodedJWT token = JWT.decode(rawToken);
            logger.debug("token: jti={}, iss={}, aud={}, sub={}, iat={}, exp={}, nbt={}",
                    token.getId(),
                    token.getIssuer(), token.getAudience(),
                    token.getSubject(), token.getIssuedAt(),
                    token.getExpiresAt(), token.getNotBefore());
            if (verifiers != null) {
                for (Verifier verifier : verifiers) {
                    try {
                        logger.debug("trying to verify token with key '{}'",
                                verifier.keyId);
                        if (verifier.verify(token)) {
                            return new AuthenticationInfo(token.getSubject());
                        }
                    } catch (InvalidClaimException e) {
                        throw new SRUException(SRUConstants.SRU_AUTHENTICATION_ERROR,
                                "error processing request authentication",
                                e.getMessage(), e);
                    } catch (TokenExpiredException e) {
                        throw new SRUException(SRUConstants.SRU_AUTHENTICATION_ERROR,
                                "error processing request authentication",
                                "token expired", e);
                    }
                }
                throw new SRUException(SRUConstants.SRU_AUTHENTICATION_ERROR,
                        "error processing request authentication",
                        "Could not verify JSON Web token signature.");
            } else {
                logger.debug("");
                return new AuthenticationInfo(token.getSubject());
            }
        } catch (JWTDecodeException e) {
            throw new SRUException(SRUConstants.SRU_AUTHENTICATION_ERROR,
                    "error processing request authentication",
                    "could not decode JSON Web token", e);
        }
    }


    private static class Verifier {
        private final String keyId;
        private final JWTVerifier verifier;


        private Verifier(String keyId, JWTVerifier verifier) {
            this.keyId = keyId;
            this.verifier = verifier;
        }


        public boolean verify(DecodedJWT token) {
            try {
                verifier.verify(token);
                return true;
            } catch (AlgorithmMismatchException | SignatureVerificationException e) {
                return false;
            }
        }
    }


    private static class AuthenticationInfo implements SRUAuthenticationInfo {
        private final String subject;

        private AuthenticationInfo(String subject) {
            this.subject = subject;
        }

        @Override
        public String getAuthentictaionMethod() {
            return "JWT";
        }

        @Override
        public String getSubject() {
            return subject;
        }

    }


    public static class Builder {
        private List<Key> keys;
        private List<String> audiences = null;
        private boolean ignoreIssuedAt = false;
        private long issuedAtLeeway = -1;
        private long expiresAtLeeway = -1;
        private long notBeforeLeeway = -1;
        private Verification builder;
        private Builder() {
        }


        public Builder withAudience(String audience) {
            if (this.audiences == null) {
                this.audiences = new ArrayList<>();
            }
            this.audiences.add(audience);
            return this;
        }


        public Builder withIgnoreIssuedAt() {
            this.ignoreIssuedAt = true;
            return this;
        }


        public Builder withIssuedAt(long leeway) {
            if (leeway < 0) {
                throw new IllegalArgumentException("leeway < 0");
            }
            this.issuedAtLeeway = leeway;
            return this;
        }


        public Builder withExpiresAt(long leeway) {
            if (leeway < 0) {
                throw new IllegalArgumentException("leeway < 0");
            }
            this.expiresAtLeeway = leeway;
            return this;
        }


        public Builder withNotBefore(long leeway) {
            if (leeway < 0) {
                throw new IllegalArgumentException("leeway < 0");
            }
            this.notBeforeLeeway = leeway;
            return this;
        }


        public Builder withPublicKey(String keyId, File file) throws SRUConfigException {
            try {
                loadPublicKeyFromStream(keyId, new FileInputStream(file));
                return this;
            } catch (FileNotFoundException e) {
                throw new SRUConfigException("failed to load key '" + keyId + "'", e);
            }
        }


        public Builder withPublicKey(String keyId, InputStream stream) throws SRUConfigException {
            loadPublicKeyFromStream(keyId, stream);
            return this;
        }

        private void loadPublicKeyFromStream(String keyId, InputStream publicKeyStream) throws SRUConfigException {
            PemReader pemReader = null;
            try {
                KeyFactory factory = KeyFactory.getInstance("RSA");

                InputStreamReader keyReader =
                        new InputStreamReader(publicKeyStream, StandardCharsets.UTF_8);
                pemReader = new PemReader(keyReader);

                PemObject pemObject = pemReader.readPemObject();
                byte[] content = pemObject.getContent();
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
                RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(pubKeySpec);

                if (keys == null) {
                    keys = new ArrayList<>();
                }
                keys.add(new Key(keyId, publicKey));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                throw new SRUConfigException("failed to load key '" + keyId + "'", e);
            } finally {
                if (pemReader != null) {
                    try {
                        pemReader.close();
                    } catch (IOException e) {
                        /* IGNORE */
                    }
                }
            }
        }


        public AuthenticationProvider build() {
            List<Verifier> verifiers = null;

            if (keys != null) {
                verifiers = new ArrayList<>();
                for (Key key : keys) {
                    Algorithm algorithm = Algorithm.RSA256(key.publicKey, null);

                    builder = JWT.require(algorithm);
                    if (audiences != null) {
                        builder.withAnyOfAudience(
                                audiences.toArray(new String[audiences.size()]));
                    }
                    if (ignoreIssuedAt) {
                        builder.ignoreIssuedAt();
                    } else {
                        if (issuedAtLeeway > 0) {
                            builder.acceptIssuedAt(issuedAtLeeway);
                        }
                    }
                    if (expiresAtLeeway > 0) {
                        builder.acceptExpiresAt(expiresAtLeeway);
                    }
                    if (notBeforeLeeway > 0) {
                        builder.acceptNotBefore(notBeforeLeeway);
                    }
                    JWTVerifier jwtVerifier = builder.build();

                    if (verifiers == null) {
                        verifiers = new ArrayList<>();
                    }
                    verifiers.add(new Verifier(key.keyId, jwtVerifier));
                }
            }
            return new AuthenticationProvider(verifiers);
        }


        public static Builder create() {
            return new Builder();
        }


        private static final class Key {
            public String keyId;
            public RSAPublicKey publicKey;


            private Key(String keyId, RSAPublicKey publicKey) {
                this.keyId = keyId;
                this.publicKey = publicKey;
            }
        }
    }

}
