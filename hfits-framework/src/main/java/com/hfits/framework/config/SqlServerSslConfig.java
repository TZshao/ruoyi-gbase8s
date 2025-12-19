package com.hfits.framework.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * SQL Server SSL 配置
 * 用于兼容旧版 SQL Server 的 TLS 1.0 连接
 *
 * @author hfits
 */
@Configuration
public class SqlServerSslConfig {

    @PostConstruct
    public void configureSsl() {
        try {
            // 设置系统属性，允许 TLS 1.0
            System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
            
            // 创建一个接受所有证书的 TrustManager（仅用于测试环境）
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // 设置默认的 SSLContext（虽然 SQL Server 驱动可能不使用，但可以确保兼容性）
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sc);
        } catch (Exception e) {
            // 忽略配置错误，使用默认设置
        }
    }
}
