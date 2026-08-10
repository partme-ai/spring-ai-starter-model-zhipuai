package org.springframework.ai.zhipuai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhipu.oapi.service.v4.model.ChatTool;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.model.ModelOptionsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<String> toolNames = new java.util.HashSet<>();
    private Boolean internalToolExecutionEnabled = true;
    private Map<String, Object> toolContext = new HashMap<>();

    public static Builder builder() { return new Builder(); }
    public Builder mutate() { return new Builder().combineWith(new Builder().model(model).maxTokens(maxTokens)
            .stopSequences(stop).temperature(temperature).topP(topP).toolCallbacks(toolCallbacks)
            .toolNames(toolNames).internalToolExecutionEnabled(internalToolExecutionEnabled).toolContext(toolContext).build()); }
    public static class Builder {
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
        public Builder clone() { return new Builder().combineWith(options); }
        public Builder model(String v) { return withModel(v); }
        public Builder frequencyPenalty(Double v) { return this; }
        public Builder maxTokens(Integer v) { return withMaxToken(v); }
        public Builder presencePenalty(Double v) { return this; }
        public Builder stopSequences(List<String> v) { return withStop(v); }
        public Builder temperature(Double v) { return withTemperature(v); }
        public Builder topK(Integer v) { return this; }
        public Builder topP(Double v) { return withTopP(v); }
        public Builder toolCallbacks(List<ToolCallback> v) { options.toolCallbacks = new ArrayList<>(v); return this; }
        public Builder toolCallbacks(ToolCallback... v) { return toolCallbacks(List.of(v)); }
        public Builder toolNames(Set<String> v) { options.toolNames = new java.util.HashSet<>(v); return this; }
        public Builder internalToolExecutionEnabled(Boolean v) { options.internalToolExecutionEnabled = v; return this; }
        public Builder toolContext(Map<String, Object> v) { options.toolContext = new HashMap<>(v); return this; }
        public Builder toolContext(String k, Object v) { options.toolContext.put(k, v); return this; }
        public Builder combineWith(ChatOptions v) {
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
    @Override public Set<String> getToolNames() { return toolNames; }
    @Override public void setToolNames(Set<String> v) { toolNames = v; }
    @Override public Boolean getInternalToolExecutionEnabled() { return internalToolExecutionEnabled; }
    @Override public void setInternalToolExecutionEnabled(Boolean v) { internalToolExecutionEnabled = v; }
    @Override public void setToolContext(Map<String, Object> v) { toolContext = v; }
    @Override public ChatOptions copy() { return mutate().build(); }
    public Boolean getDoSample() { return doSample; }
    public String getUser() { return user; }
    public List<ChatTool> getTools() { return tools; }
    public String getToolChoice() { return toolChoice; }
    public Map<String, Object> toMap() { return ModelOptionsUtils.objectToMap(this); }
}
