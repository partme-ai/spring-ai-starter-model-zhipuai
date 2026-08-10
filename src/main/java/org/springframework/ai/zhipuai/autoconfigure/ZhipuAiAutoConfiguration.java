package org.springframework.ai.zhipuai.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.core.cache.ICache;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.zhipuai.ZhipuAiChatClient;
import org.springframework.ai.zhipuai.ZhipuAiEmbeddingClient;
import org.springframework.ai.zhipuai.ZhipuAiFineTuningClient;
import org.springframework.ai.zhipuai.ZhipuAiImageClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for 智普AI Chat Client.
 */
@AutoConfiguration(after = SpringAiRetryAutoConfiguration.class)
@EnableConfigurationProperties({ ZhipuAiChatProperties.class, ZhipuAiConnectionProperties.class, ZhipuAiEmbeddingProperties.class, ZhipuAiImageProperties.class })
@ConditionalOnClass(ClientV4.class)
public class ZhipuAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ZhipuAiChatProperties.CONFIG_PREFIX, name = "enabled")
    public ZhipuAiChatClient zhipuAiChatClient(ZhipuAiConnectionProperties connectionProperties,
                                               ZhipuAiChatProperties chatProperties,
                                               ObjectProvider<ToolCallback> toolFunctionCallbacks,
                                               ObjectProvider<ICache> cacheProvider,
                                               ObjectProvider<RetryTemplate> retryTemplateProvider) {
        chatProperties.getOptions().setToolCallbacks(toolFunctionCallbacks.orderedStream().toList());

        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(apiKey, "ZhipuAI API key must be set");

        ClientV4 zhipuClient = new ClientV4.Builder(apiKey).tokenCache(cacheProvider.getIfAvailable()).build();

        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> new RetryTemplate());
        return new ZhipuAiChatClient(zhipuClient, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ZhipuAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled")
    public ZhipuAiEmbeddingClient zhipuAiEmbeddingClient(ZhipuAiConnectionProperties connectionProperties,
                                                         ZhipuAiEmbeddingProperties embeddingProperties,
                                                         ObjectProvider<ICache> cacheProvider,
                                                         ObjectProvider<RetryTemplate> retryTemplateProvider) {

        String apiKey = StringUtils.hasText(embeddingProperties.getApiKey()) ? embeddingProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(apiKey, "ZhipuAI API key must be set");

        ClientV4 zhipuClient = new ClientV4.Builder(apiKey).tokenCache(cacheProvider.getIfAvailable()).build();
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> new RetryTemplate());
        return new ZhipuAiEmbeddingClient(zhipuClient, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ZhipuAiFineTuningProperties.CONFIG_PREFIX, name = "enabled")
    public ZhipuAiFineTuningClient zhipuAiFineTuningClient(ZhipuAiConnectionProperties connectionProperties,
                                                           ZhipuAiFineTuningProperties fineTuningProperties,
                                                           ObjectProvider<ICache> cacheProvider,
                                                           ObjectProvider<RetryTemplate> retryTemplateProvider) {

        String apiKey = StringUtils.hasText(fineTuningProperties.getApiKey()) ? fineTuningProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(apiKey, "ZhipuAI API key must be set");

        ClientV4 zhipuClient = new ClientV4.Builder(apiKey).tokenCache(cacheProvider.getIfAvailable()).build();
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> new RetryTemplate());
        return new ZhipuAiFineTuningClient(zhipuClient, retryTemplate);
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ZhipuAiImageProperties.CONFIG_PREFIX, name = "enabled")
    public ZhipuAiImageClient zhipuAiImageClient(ZhipuAiConnectionProperties connectionProperties,
                                                 ZhipuAiImageProperties imageProperties,
                                                 ObjectProvider<ICache> cacheProvider,
                                                 ObjectProvider<RetryTemplate> retryTemplateProvider) {

        String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(apiKey, "ZhipuAI API key must be set");

        ClientV4 zhipuClient = new ClientV4.Builder(apiKey).tokenCache(cacheProvider.getIfAvailable()).build();
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> new RetryTemplate());
        return new ZhipuAiImageClient(zhipuClient, imageProperties.getOptions(), retryTemplate);
    }



    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

}
