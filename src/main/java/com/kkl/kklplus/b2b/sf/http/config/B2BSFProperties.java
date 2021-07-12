package com.kkl.kklplus.b2b.sf.http.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "sf")
public class B2BSFProperties {

    @Getter
    private final OkHttpProperties okhttp = new OkHttpProperties();

    public static class OkHttpProperties {
        /**
         * 设置连接超时
         */
        @Getter
        @Setter
        private Integer connectTimeout = 10;

        /**
         * 设置读超时
         */
        @Getter
        @Setter
        private Integer writeTimeout = 10;

        /**
         * 设置写超时
         */
        @Getter
        @Setter
        private Integer readTimeout = 10;

        /**
         * 是否自动重连
         */
        @Getter
        @Setter
        private Boolean retryOnConnectionFailure = true;

        /**
         * 设置ping检测网络连通性的间隔
         */
        @Getter
        @Setter
        private Integer pingInterval = 0;
    }

    /**
     * 数据源配置
     */
    @Getter
    private final DataSourceConfig dataSourceConfig = new DataSourceConfig();

    public static class DataSourceConfig {
        @Getter
        @Setter
        private String requestMainUrl;

        @Getter
        @Setter
        private String partnerID;

        @Getter
        @Setter
        private String md5Key;

        @Setter
        @Getter
        private String clientCode;

        @Getter
        @Setter
        private String picUrl;

        @Getter
        @Setter
        private Boolean orderMqEnabled = false;
    }

    @Getter
    private final ThreadPoolProperties threadPool = new ThreadPoolProperties();

    public static class ThreadPoolProperties {

        @Getter
        @Setter
        private Integer corePoolSize = 1;

        @Getter
        @Setter
        private Integer maxPoolSize = 12;

        @Getter
        @Setter
        private Integer keepAliveSeconds = 60;

        @Getter
        @Setter
        private Integer queueCapacity = 24;

    }

    @Getter
    @Setter
    private String b2bConfigUrl = "";

    //子系统
    @Getter
    @Setter
    private final SiteInfo site = new SiteInfo();

    public static class SiteInfo {

        //负责的子系统
        @Getter
        @Setter
        private String code;

        @Getter
        @Setter
        private Map<String,String> otherSites = new HashMap<>();
    }
}
