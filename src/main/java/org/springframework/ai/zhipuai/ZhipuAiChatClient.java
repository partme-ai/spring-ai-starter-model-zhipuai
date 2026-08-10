package org.springframework.ai.zhipuai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.FlowableSubscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.zhipuai.api.ZhipuAiChatOptions;
import org.springframework.ai.zhipuai.util.ApiUtils;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ZhipuAiChatClient implements ChatModel {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String REQUEST_ID_TEMPLATE = "zhipu-ai-chat-%s";
    private final ZhipuAiChatOptions defaultOptions;
    private final ClientV4 zhipuClient;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZhipuAiChatClient(ClientV4 client) {
        this(client, ZhipuAiChatOptions.builder().withModel(Constants.ModelChatGLM3TURBO)
                .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS).withDoSample(true)
                .withTemperature(ApiUtils.DEFAULT_TEMPERATURE).withTopP(ApiUtils.DEFAULT_TOP_P).build());
    }
    public ZhipuAiChatClient(ClientV4 client, ZhipuAiChatOptions options) {
        this(client, options, new RetryTemplate());
    }
    public ZhipuAiChatClient(ClientV4 client, ZhipuAiChatOptions options, RetryTemplate retryTemplate) {
        Assert.notNull(client, "ClientV4 must not be null");
        Assert.notNull(options, "Options must not be null");
        this.zhipuClient = client;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override public ChatOptions getOptions() { return defaultOptions; }

    @Override
    public ChatResponse call(Prompt prompt) {
        return retryTemplate.invoke(() -> {
            ModelApiResponse response = zhipuClient.invokeModelApi(createRequest(prompt, false));
            if (!response.isSuccess() || response.getData() == null) {
                throw new IllegalStateException("Failed to call Zhipu AI chat API: " + response.getMsg());
            }
            return toChatResponse(response.getData());
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return retryTemplate.invoke(() -> {
            ModelApiResponse response = zhipuClient.invokeModelApi(createRequest(prompt, true));
            if (!response.isSuccess()) return Flux.error(new IllegalStateException(response.getMsg()));
            return Flux.create(sink -> response.getFlowable().subscribe(new FlowableSubscriber<>() {
                @Override public void onSubscribe(Subscription subscription) { subscription.request(Long.MAX_VALUE); }
                @Override public void onNext(ModelData data) { sink.next(toChatResponse(data)); }
                @Override public void onError(Throwable throwable) { sink.error(throwable); }
                @Override public void onComplete() { sink.complete(); }
            }));
        });
    }

    ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        ZhipuAiChatOptions options = prompt.getOptions() == null ? defaultOptions.mutate().build()
                : defaultOptions.mutate().combineWith(prompt.getOptions().mutate()).build();
        List<ChatMessage> messages = prompt.getInstructions().stream()
                .map(message -> new ChatMessage(message.getMessageType().getValue(), message.getText())).toList();
        List<ChatTool> tools = resolveTools(options.getToolCallbacks());
        return ChatCompletionRequest.builder().model(options.getModel()).messages(messages)
                .requestId(String.format(REQUEST_ID_TEMPLATE, System.currentTimeMillis()))
                .doSample(options.getDoSample()).stream(stream)
                .temperature(options.getTemperature() == null ? null : options.getTemperature().floatValue())
                .topP(options.getTopP() == null ? null : options.getTopP().floatValue())
                .maxTokens(options.getMaxTokens()).stop(options.getStopSequences())
                .tools(tools.isEmpty() ? options.getTools() : tools)
                .toolChoice(options.getToolChoice() == null ? "auto" : options.getToolChoice())
                .invokeMethod(Constants.invokeMethod).build();
    }

    private List<ChatTool> resolveTools(List<ToolCallback> callbacks) {
        if (CollectionUtils.isEmpty(callbacks)) return List.of();
        List<ChatTool> tools = new ArrayList<>();
        for (ToolCallback callback : callbacks) {
            var definition = callback.getToolDefinition();
            try {
                ChatFunctionParameters parameters = objectMapper.readValue(definition.inputSchema(), ChatFunctionParameters.class);
                ChatFunction function = ChatFunction.builder().name(definition.name())
                        .description(definition.description()).parameters(parameters).build();
                ChatTool tool = new ChatTool();
                tool.setType(ChatToolType.FUNCTION.value());
                tool.setFunction(function);
                tools.add(tool);
            }
            catch (Exception exception) {
                throw new IllegalArgumentException("Invalid tool schema for " + definition.name(), exception);
            }
        }
        return tools;
    }

    private ChatResponse toChatResponse(ModelData data) {
        if (CollectionUtils.isEmpty(data.getChoices())) {
            log.warn("No choices returned by Zhipu AI");
            return new ChatResponse(List.of());
        }
        List<Generation> generations = data.getChoices().stream().map(choice -> {
            ChatMessage nativeMessage = choice.getMessage();
            Delta delta = choice.getDelta();
            String content = nativeMessage != null ? Objects.toString(nativeMessage.getContent(), "")
                    : delta == null ? "" : Objects.toString(delta.getContent(), "");
            List<ToolCalls> nativeCalls = nativeMessage != null ? nativeMessage.getTool_calls()
                    : delta == null ? List.of() : delta.getTool_calls();
            List<AssistantMessage.ToolCall> calls = CollectionUtils.isEmpty(nativeCalls) ? List.of()
                    : nativeCalls.stream().map(call -> new AssistantMessage.ToolCall(call.getId(), call.getType(),
                    call.getFunction().getName(), call.getFunction().getArguments().toString())).toList();
            AssistantMessage message = AssistantMessage.builder().content(content)
                    .properties(Map.of("id", Objects.toString(data.getId(), ""))).toolCalls(calls).build();
            ChatGenerationMetadata metadata = ChatGenerationMetadata.builder()
                    .finishReason(choice.getFinishReason() == null ? "" : choice.getFinishReason()).build();
            return new Generation(message, metadata);
        }).toList();
        return new ChatResponse(generations);
    }
}
