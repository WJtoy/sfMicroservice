package com.kkl.kklplus.b2b.sf.http.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties({B2BSFProperties.class})
@Configuration
public class OKHttpConfig {

    @Bean
    public OkHttpClient okHttpClient(B2BSFProperties sfProperties) {
        return new OkHttpClient().newBuilder()
                .connectTimeout(sfProperties.getOkhttp().getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(sfProperties.getOkhttp().getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(sfProperties.getOkhttp().getReadTimeout(), TimeUnit.SECONDS)
                .pingInterval(sfProperties.getOkhttp().getPingInterval(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(sfProperties.getOkhttp().getRetryOnConnectionFailure())
                .build();
    }

}
