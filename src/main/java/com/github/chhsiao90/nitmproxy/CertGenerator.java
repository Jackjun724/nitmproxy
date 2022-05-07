package com.github.chhsiao90.nitmproxy;

import com.github.chhsiao90.nitmproxy.tls.CertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class CertGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertGenerator.class);

    private static final String DEFAULT_SUBJECT = "C=US, ST=VA, L=Vienna, O=Nitm, OU=Nitm, CN=Nitm CA Root";
    private static final int DEFAULT_KEYSIZE = 2048;

    private CertGenerator() {
    }

    public static void main(String[] args) throws Exception {

        CertGeneratorConfig config = new CertGeneratorConfig();
        LOGGER.info("Generating certificate with subject:{} and keysize:{}",
                config.getSubject(), config.getKeySize());

        File serverPem = new File("server.pem");
        File keyPem = new File("key.pem");

        CertUtil.createCACertificates(serverPem, keyPem, config.getSubject(), config.getKeySize());

        //we'll copy server.pem to server.crt for easy import
        Files.copy(Paths.get(serverPem.toURI()), Paths.get("server.crt"));
    }

    private static class CertGeneratorConfig {
        String subject = DEFAULT_SUBJECT;
        int keySize = DEFAULT_KEYSIZE;

        public int getKeySize() {
            return keySize;
        }

        public void setKeySize(int keySize) {
            this.keySize = keySize;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }
}