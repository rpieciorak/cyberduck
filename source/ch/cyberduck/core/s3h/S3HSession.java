package ch.cyberduck.core.s3h;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.cloudfront.DistributionConfig;
import org.jets3t.service.model.cloudfront.LoggingStatus;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Connecting to S3 service with plain HTTP.
 *
 * @version $Id$
 */
public class S3HSession extends HTTPSession implements CloudSession {
    private static Logger log = Logger.getLogger(S3HSession.class);

    static {
        SessionFactory.addFactory(Protocol.S3, new Factory());
    }

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new S3HSession(h);
        }
    }

    private CustomRestS3Service S3;

    protected S3HSession(Host h) {
        super(h);
    }

    @Override
    protected CustomRestS3Service getClient() throws ConnectionCanceledException {
        if(null == S3) {
            throw new ConnectionCanceledException();
        }
        return S3;
    }

    /**
     * Exposing protected methods
     */
    public static class CustomRestS3Service extends RestS3Service {
        public CustomRestS3Service(AWSCredentials awsCredentials, String invokingApplicationDescription, CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties, HostConfiguration hostConfig) throws S3ServiceException {
            super(awsCredentials, invokingApplicationDescription, credentialsProvider, jets3tProperties, hostConfig);
        }

        @Override
        public void pubObjectWithRequestEntityImpl(String bucketName, S3Object object, RequestEntity requestEntity) throws S3ServiceException {
            super.pubObjectWithRequestEntityImpl(bucketName, object, requestEntity);
        }
    }

    /**
     *
     */
    protected Jets3tProperties configuration = new Jets3tProperties();

    protected void configure() {
        if(host.getHostname().endsWith(host.getProtocol().getDefaultHostname())) {
            configuration.setProperty("s3service.s3-endpoint", host.getProtocol().getDefaultHostname());
        }
        else {
            configuration.setProperty("s3service.s3-endpoint", host.getHostname());
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        configuration.setProperty("s3service.https-only", String.valueOf(host.getProtocol().isSecure()));
        // The maximum number of retries that will be attempted when an S3 connection fails
        // with an InternalServer error. To disable retries of InternalError failures, set this to 0.
        configuration.setProperty("s3service.internal-error-retry-max", String.valueOf(0));
        // The maximum number of concurrent communication threads that will be started by
        // the multi-threaded service for upload and download operations.
        configuration.setProperty("s3service.max-thread-count", String.valueOf(1));

        configuration.setProperty("httpclient.proxy-autodetect", String.valueOf(false));
        final Proxy proxy = ProxyFactory.instance();
        if(host.getProtocol().isSecure()) {
            if(proxy.isHTTPSProxyEnabled()) {
                configuration.setProperty("httpclient.proxy-host", proxy.getHTTPSProxyHost());
                configuration.setProperty("httpclient.proxy-port", String.valueOf(proxy.getHTTPSProxyPort()));
                configuration.setProperty("httpclient.proxy-user", null);
                configuration.setProperty("httpclient.proxy-password", null);
            }
        }
        else {
            if(proxy.isHTTPProxyEnabled()) {
                configuration.setProperty("httpclient.proxy-host", proxy.getHTTPProxyHost());
                configuration.setProperty("httpclient.proxy-port", String.valueOf(proxy.getHTTPProxyPort()));
                configuration.setProperty("httpclient.proxy-user", null);
                configuration.setProperty("httpclient.proxy-password", null);
            }
        }
        configuration.setProperty("httpclient.connection-timeout-ms", String.valueOf(this.timeout()));
        configuration.setProperty("httpclient.socket-timeout-ms", String.valueOf(this.timeout()));
        configuration.setProperty("httpclient.useragent", this.getUserAgent());
        configuration.setProperty("httpclient.authentication-preemptive", String.valueOf(false));

        // How many times to retry connections when they fail with IO errors. Set this to 0 to disable retries.
        // configuration.setProperty("httpclient.retry-max", String.valueOf(0));
    }

    /**
     * @param bucket
     * @return
     */
    public String getHostnameForBucket(String bucket) {
        return ServiceUtils.generateS3HostnameForBucket(bucket,
                configuration.getBoolProperty("s3service.disable-dns-buckets", false), this.getHost().getHostname());
    }

    /**
     * @param hostname
     * @return
     */
    protected String getBucketForHostname(String hostname) {
        if(hostname.equals(host.getProtocol().getDefaultHostname())) {
            return null;
        }
        // Bucket name is available in URL's host name.
        if(hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            // Bucket name is available as S3 subdomain
            return hostname.substring(0, hostname.length() - host.getProtocol().getDefaultHostname().length() - 1);
        }
        return null;
    }

    /**
     * Caching the uses's buckets
     */
    private List<S3Bucket> buckets = new ArrayList<S3Bucket>();

    /**
     * @param reload
     * @return
     * @throws S3ServiceException
     */
    protected List<S3Bucket> getBuckets(boolean reload) throws IOException, S3ServiceException {
        if(buckets.isEmpty() || reload) {
            buckets.clear();
            if(host.getCredentials().isAnonymousLogin()) {
                // Listing buckets not supported for thirdparty buckets
                String bucketname = this.getBucketForHostname(host.getHostname());
                if(null == bucketname) {
                    if(StringUtils.isNotBlank(host.getDefaultPath())) {
                        Path d = PathFactory.createPath(this, host.getDefaultPath(), AbstractPath.DIRECTORY_TYPE);
                        while(!d.getParent().isRoot()) {
                            d = d.getParent();
                        }
                        bucketname = d.getName();
                    }
                }
                if(null == bucketname) {
                    log.error("No bucket name given in hostname or default path");
                    return buckets;
                }
                if(!this.getClient().isBucketAccessible(bucketname)) {
                    throw new IOException("Bucket not accessible: " + bucketname);
                }
                final S3HPath thirdparty = (S3HPath) PathFactory.createPath(this, bucketname,
                        Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                buckets.add(new S3Bucket(thirdparty.getContainerName()));
            }
            else {
                buckets.addAll(Arrays.asList(this.getClient().listAllBuckets()));
            }
        }
        return buckets;
    }

    @Override
    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));

        // Configure connection options
        this.configure();

        // Prompt the login credentials first
        this.login();
        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));
        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void login(final Credentials credentials) throws IOException {
        final HostConfiguration hostconfig = new StickyHostConfiguration();
        hostconfig.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        new DefaultProtocolSocketFactory(), host.getPort())
        );
        this.login(credentials, hostconfig);
    }

    /**
     * @param credentials
     * @param hostconfig
     * @throws IOException
     */
    protected void login(final Credentials credentials, final HostConfiguration hostconfig) throws IOException {
        try {
            this.S3 = new CustomRestS3Service(credentials.isAnonymousLogin() ? null : new AWSCredentials(credentials.getUsername(),
                    credentials.getPassword()), this.getUserAgent(), new CredentialsProvider() {
                /**
                 * Implementation method for the CredentialsProvider interface
                 * @throws CredentialsNotAvailableException
                 */
                public org.apache.commons.httpclient.Credentials getCredentials(AuthScheme authscheme, String hostname, int port, boolean proxy)
                        throws CredentialsNotAvailableException {
                    log.error("Additional HTTP authentication not supported:" + authscheme.getSchemeName());
                    throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                            authscheme.getSchemeName());
                }
            }, configuration, hostconfig);
            this.getBuckets(true);
        }
        catch(S3ServiceException e) {
            if(this.isLoginFailure(e)) {
                this.message(Locale.localizedString("Login failed", "Credentials"));
                this.login.fail(host,
                        Locale.localizedString("Login with username and password", "Credentials"));
                this.login();
            }
            else {
                throw new IOException(e.getS3ErrorMessage());
            }
        }
    }

    /**
     * Check for Invalid Access ID or Invalid Secret Key
     *
     * @param e
     * @return True if the error code of the S3 exception is a login failure
     */
    private boolean isLoginFailure(S3ServiceException e) {
        if(null == e.getS3ErrorCode()) {
            return false;
        }
        return e.getS3ErrorCode().equals("InvalidAccessKeyId") // Invalid Access ID
                || e.getS3ErrorCode().equals("SignatureDoesNotMatch"); // Invalid Secret Key
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            // No logout required
            S3 = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    /**
     * @return True
     */
    @Override
    public boolean isDownloadResumable() {
        return true;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    protected void noop() throws IOException {
        ;
    }

    @Override
    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Amazon CloudFront Extension to create a new distribution configuration
     * *
     *
     * @param enabled Distribution status
     * @param bucket  Name of the container
     * @param cnames  DNS CNAME aliases for distribution
     * @param logging Access log configuration
     * @return Distribution configuration
     * @throws CloudFrontServiceException CloudFront failure details
     */
    public org.jets3t.service.model.cloudfront.Distribution createDistribution(boolean enabled,
                                                                               Distribution.Method method,
                                                                               final String bucket,
                                                                               String[] cnames,
                                                                               LoggingStatus logging) throws CloudFrontServiceException {
        final long reference = System.currentTimeMillis();
        if(method.equals(Distribution.STREAMING)) {
            return this.createCloudFrontService().createStreamingDistribution(
                    this.getHostnameForBucket(bucket),
                    String.valueOf(reference), // Caller reference - a unique string value
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled,  // Enabled?
                    logging
            );
        }
        return this.createCloudFrontService().createDistribution(
                this.getHostnameForBucket(bucket),
                String.valueOf(reference), // Caller reference - a unique string value
                cnames, // CNAME aliases for distribution
                new Date(reference).toString(), // Comment
                enabled,  // Enabled?
                logging // Logging Status. Disabled if null
        );
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param enabled      Distribution status
     * @param distribution Distribution configuration
     * @param cnames       DNS CNAME aliases for distribution
     * @param logging      Access log configuration
     * @throws CloudFrontServiceException CloudFront failure details
     */
    public void updateDistribution(boolean enabled, Distribution.Method method,
                                   final org.jets3t.service.model.cloudfront.Distribution distribution, String[] cnames, LoggingStatus logging
    ) throws CloudFrontServiceException {
        final long reference = System.currentTimeMillis();
        if(method.equals(Distribution.STREAMING)) {
            this.createCloudFrontService().updateStreamingDistributionConfig(
                    distribution.getId(),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging
            );
        }
        else {
            this.createCloudFrontService().updateDistributionConfig(
                    distribution.getId(),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging // Logging Status. Disabled if null
            );
        }
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @param bucket Name of the container
     * @param method
     * @return All distributions for the given AWS Credentials
     * @throws CloudFrontServiceException CloudFront failure details
     */
    protected org.jets3t.service.model.cloudfront.Distribution[] listDistributions(String bucket,
                                                                                   Distribution.Method method) throws CloudFrontServiceException {
        if(method.equals(Distribution.STREAMING)) {
            return this.createCloudFrontService().listStreamingDistributions(bucket);
        }
        return this.createCloudFrontService().listDistributions(bucket);
    }

    /**
     * @param distribution Distribution configuration
     * @throws CloudFrontServiceException CloudFront failure details
     * @returann
     */
    protected DistributionConfig getDistributionConfig(final org.jets3t.service.model.cloudfront.Distribution distribution
    ) throws CloudFrontServiceException {
        if(distribution.isStreamingDistribution()) {
            return this.createCloudFrontService().getStreamingDistributionConfig(distribution.getId());
        }
        return this.createCloudFrontService().getDistributionConfig(distribution.getId());
    }

    /**
     * @param distribution A distribution (the distribution must be disabled and deployed first)
     * @throws CloudFrontServiceException CloudFront failure details
     */
    public void deleteDistribution(final org.jets3t.service.model.cloudfront.Distribution distribution
    ) throws CloudFrontServiceException {
        if(distribution.isStreamingDistribution()) {
            this.createCloudFrontService().deleteStreamingDistribution(distribution.getId());
        }
        else {
            this.createCloudFrontService().deleteDistribution(distribution.getId());
        }
    }

    /**
     * Cached instance for session
     */
    private CloudFrontService cloudfront;

    /**
     * Amazon CloudFront Extension
     *
     * @return A cached cloud front service interface
     * @throws CloudFrontServiceException CloudFront failure
     */
    private CloudFrontService createCloudFrontService() throws CloudFrontServiceException {
        if(null == cloudfront) {
            final Credentials credentials = host.getCredentials();

            // Construct a CloudFrontService object to interact with the service.
            HostConfiguration hostconfig = null;
            try {
                hostconfig = new StickyHostConfiguration();
                final HttpHost endpoint = new HttpHost(new URI(CloudFrontService.ENDPOINT, false));
                hostconfig.setHost(endpoint.getHostName(), endpoint.getPort(),
                        new org.apache.commons.httpclient.protocol.Protocol(endpoint.getProtocol().getScheme(),
                                (ProtocolSocketFactory) new CustomTrustSSLProtocolSocketFactory(new KeychainX509TrustManager(endpoint.getHostName())), endpoint.getPort())
                );
            }
            catch(URIException e) {
                log.error(e.getMessage(), e);
            }
            cloudfront = new CloudFrontService(
                    new AWSCredentials(credentials.getUsername(), credentials.getPassword()),
                    this.getUserAgent(), // Invoking application description
                    null, // Credentials Provider
                    new Jets3tProperties(),
                    hostconfig);
        }
        return cloudfront;
    }


    /**
     * @return
     */
    public Distribution readDistribution(String container, Distribution.Method method) {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            return new Distribution();
        }
        try {
            this.check();
            for(org.jets3t.service.model.cloudfront.Distribution d : this.listDistributions(container, method)) {
                // Retrieve distribution's configuration to access current logging status settings.
                final DistributionConfig distributionConfig = this.getDistributionConfig(d);
                // We currently only support one distribution per bucket
                return new Distribution(d.isEnabled(), d.isDeployed(),
                        method.getProtocol() + d.getDomainName() + method.getContext(),
                        Locale.localizedString(d.getStatus(), "S3"),
                        distributionConfig.getCNAMEs(),
                        distributionConfig.isLoggingEnabled(),
                        method);
            }
        }
        catch(CloudFrontServiceException e) {
            if(e.getResponseCode() == 403) {
                log.warn("Invalid CloudFront account:" + e.getMessage());
                return new Distribution();
            }
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
        return new Distribution();
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param enabled
     * @param method
     * @param cnames
     * @param logging
     */
    public void writeDistribution(final boolean enabled, String container, Distribution.Method method, final String[] cnames, boolean logging) {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            return;
        }
        try {
            LoggingStatus l = null;
            if(logging) {
                l = new LoggingStatus(
                        this.getHostnameForBucket(container),
                        Preferences.instance().getProperty("cloudfront.logging.prefix"));
            }
            this.check();
            StringBuilder name = new StringBuilder(Locale.localizedString("Amazon CloudFront", "S3")).append(" ").append(method.toString());
            if(enabled) {
                this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), name));
            }
            else {
                this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), name));
            }
            for(org.jets3t.service.model.cloudfront.Distribution distribution : this.listDistributions(container, method)) {
                this.updateDistribution(enabled, method, distribution, cnames, l);
                // We currently only support one distribution per bucket
                return;
            }
            // Create new configuration
            this.createDistribution(enabled, method, container, cnames, l);
        }
        catch(CloudFrontServiceException e) {
            this.error("Cannot write file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
    }

    public List<Distribution.Method> getSupportedMethods() {
        return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING);
    }
}
