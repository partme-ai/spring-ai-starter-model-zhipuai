package org.springframework.ai.zhipuai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhipu.oapi.service.v4.model.ChatTool;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.util.JsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZhipuAiChatOptions implements ToolCallingChatOptions {
    @JsonProperty("max_tokens") private Integer maxTokens;
    @JsonProperty("do_sample") private Boolean doSample;
    @JsonProperty("temperature") private Double temperature;
    @JsonProperty("top_p") private Double topP;
    @JsonProperty("user_id") private String user;
    @JsonProperty("stop") private List<String> stop;
    @JsonProperty("model") private String model;
    @JsonProperty("tools") private List<ChatTool> tools;
    @JsonProperty("toolChoice") private String toolChoice;
    private List<ToolCallback> toolCallbacks = new ArrayList<>();
    private Map<String, Object> toolContext = new HashMap<>();

    public static Builder builder() { return new Builder(); }
    @Override public Builder mutate() { return new Builder().combineWith(new Builder().model(model).maxTokens(maxTokens)
            .stopSequences(stop).temperature(temperature).topP(topP).toolCallbacks(toolCallbacks).toolContext(toolContext)); }
    public static class Builder implements ToolCallingChatOptions.Builder<Builder> {
        private final ZhipuAiChatOptions options = new ZhipuAiChatOptions();
        public Builder withModel(String v) { options.model = v; return this; }
        public Builder withMaxToken(Integer v) { options.maxTokens = v; return this; }
        public Builder withDoSample(Boolean v) { options.doSample = v; return this; }
        public Builder withTemperature(Double v) { options.temperature = v; return this; }
        public Builder withTopP(Double v) { options.topP = v; return this; }
        public Builder withUser(String v) { options.user = v; return this; }
        public Builder withStop(List<String> v) { options.stop = v; return this; }
        public Builder withTools(List<ChatTool> v) { options.tools = v; return this; }
        public Builder withToolChoice(String v) { options.toolChoice = v; return this; }
        public ZhipuAiChatOptions build() { return options; }
        @Override public Builder clone() { return new Builder().combineWith(this); }
        @Override public Builder model(String v) { return withModel(v); }
        @Override public Builder frequencyPenalty(Double v) { return this; }
        @Override public Builder maxTokens(Integer v) { return withMaxToken(v); }
        @Override public Builder presencePenalty(Double v) { return this; }
        @Override public Builder stopSequences(List<String> v) { return withStop(v); }
        @Override public Builder temperature(Double v) { return withTemperature(v); }
        @Override public Builder topK(Integer v) { return this; }
        @Override public Builder topP(Double v) { return withTopP(v); }
        @Override public Builder toolCallbacks(List<ToolCallback> v) { options.toolCallbacks = new ArrayList<>(v); return this; }
        @Override public Builder toolCallbacks(ToolCallback... v) { return toolCallbacks(List.of(v)); }
        @Override public Builder toolContext(Map<String, Object> v) { options.toolContext = new HashMap<>(v); return this; }
        @Override public Builder toolContext(String k, Object v) { options.toolContext.put(k, v); return this; }
        @Override public Builder combineWith(ChatOptions.Builder<?> other) {
            ChatOptions v = other.build();
            if (v.getModel() != null) model(v.getModel());
            if (v.getMaxTokens() != null) maxTokens(v.getMaxTokens());
            if (v.getStopSequences() != null) stopSequences(v.getStopSequences());
            if (v.getTemperature() != null) temperature(v.getTemperature());
            if (v.getTopP() != null) topP(v.getTopP());
            if (v instanceof ToolCallingChatOptions t) {
                toolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(options.toolCallbacks, t.getToolCallbacks()));
                toolContext(ToolCallingChatOptions.mergeToolContext(options.toolContext, t.getToolContext()));
            }
            return this;
        }
    }
    @Override public String getModel() { return model; }
    @Override public Integer getMaxTokens() { return maxTokens; }
    @Override public Double getFrequencyPenalty() { return null; }
    @Override public Double getPresencePenalty() { return null; }
    @Override public List<String> getStopSequences() { return stop; }
    @Override public Double getTemperature() { return temperature; }
    @Override public Integer getTopK() { return null; }
    @Override public Double getTopP() { return topP; }
    @Override public List<ToolCallback> getToolCallbacks() { return toolCallbacks; }
    @Override public Map<String, Object> getToolContext() { return toolContext; }
    public void setToolCallbacks(List<ToolCallback> v) { toolCallbacks = v; }
    public Boolean getDoSample() { return doSample; }
    public String getUser() { return user; }
    public List<ChatTool> getTools() { return tools; }
    public String getToolChoice() { return toolChoice; }
    public Map<String, Object> toMap() { return new JsonHelper().convertToMap(this); }
}
