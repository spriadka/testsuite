/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.hal.testsuite.test.configuration.picketlink.federation;

/**
 * Utility class to provide various constants as well as testcase specific resource names
 */
public final class UtilFedName {

    static final String
    // subtabs
    // labels
        NAME_LABEL = "name",
        IDENTITY_PROVIDER_LABEL = "Identity Provider",
        SAML_CONFIGURATION_LABEL = "SAML Configuration",
        SAML_HANDLERS_LABEL = "SAML Handlers",
        KEY_STORE_LABEL = "Key Store",
        TRUSTED_DOMAINS_LABEL = "Trusted Domains",
        HOST_KEYS_LABEL = "Host Keys",
        SERVICE_PROVIDER_LABEL = "Service Provider",
    // DMR
        FEDERATION = "federation",
        IDENTITY_PROVIDER = "identity-provider",
        SECURITY_DOMAIN = "security-domain",
        URL = "url",
        IDP_EXTERNAL = "external",
        HANDLER = "handler",
        HANDLER_CLASS_NAME = "class-name",
        IDP_TRUST_DOMAIN = "trust-domain",
        SAML = "saml",
        SAML_CLOCK_SKEW = "clock-skew",
        SAML_TOKEN_TIMEOUT = "token-timeout",
        KEY_STORE = "key-store",
        KS_FILE = "file",
        KS_PASS = "password",
        KS_RELATIVE_TO = "relative-to",
        KS_KEY_ALIAS = "sign-key-alias",
        KS_KEY_PASS = "sign-key-password",
        KS_HK_HOST = "host",
        SERVICE_PROVIDER = "service-provider",
        SP_ERROR_PAGE = "error-page",
        SP_LOGOUT_PAGE = "logout-page",
        SP_POST_BINDING = "post-binding",
        SP_STRICT_POST_BINDING = "strict-post-binding",
    // DMR values
        SD_OTHER = "other",
        SD_JBOSS_WEB_POLICY = "jboss-web-policy";

    private final AbstractFederationTestCase testCase;

    UtilFedName(AbstractFederationTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * get resource name traceable to particular testcase
     * @return string specific for each Name and testcase combination
     */
    String get(Key attr) {
        return attr.name() + testCase.getStringSuffix();
    }

    String getFederation() {
        return get(Key.FED);
    }

    String getIdp() {
        return get(Key.IDP);
    }

    String getSp() {
        return get(Key.SP);
    }

    enum Key {
        FED, IDP, URL, HANDLER, CLASS_NAME, SP;
    }
}
