package ch.cyberduck.core.ftps;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

public abstract class AbstractX509TrustManager implements X509TrustManager {
    private static Logger log = Logger.getLogger(AbstractX509TrustManager.class);

    private X509TrustManager standardTrustManager = null;
    
    protected void init(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509");
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("SunX509 trust manager not supported");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        if(Preferences.instance().getBoolean("ftp.tls.acceptAnyCertificate")) {
            log.warn("Certificate not verified!");
            return;
        }
        this.standardTrustManager.checkClientTrusted(x509Certificates, authType);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        if(Preferences.instance().getBoolean("ftp.tls.acceptAnyCertificate")) {
            log.warn("Certificate not verified!");
            return;
        }
        if ((x509Certificates != null)) {
            log.info("Server certificate chain:");
            for (int i = 0; i < x509Certificates.length; i++) {
                log.info("X509Certificate[" + i + "]=" + x509Certificates[i]);
            }
        }
        this.standardTrustManager.checkServerTrusted(x509Certificates, authType);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}