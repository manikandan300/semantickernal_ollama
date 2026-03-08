package org.example.semantickernal;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticKernelConfig {

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String model;

    @Bean
    public Kernel kernel() {

        HttpPipelinePolicy authPolicy = (context, next) -> {
            context.getHttpRequest().setHeader("Authorization", "Bearer ollama");
            return next.process();
        };

        // Also need to override the URL to remove the Azure deployment path
        HttpPipelinePolicy urlFixPolicy = (context, next) -> {
            String url = context.getHttpRequest().getUrl().toString();
            // Replace Azure-style deployment URL with plain OpenAI-compatible URL
            url = url.replaceAll("/openai/deployments/[^/]+/", "/");
            try {
                context.getHttpRequest().setUrl(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return next.process();
        };

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .endpoint(ollamaBaseUrl)
                .addPolicy(authPolicy)
                .addPolicy(urlFixPolicy)
                .buildAsyncClient();

        ChatCompletionService chatService = OpenAIChatCompletion.builder()
                .withModelId(model)
                .withOpenAIAsyncClient(client)
                .build();

        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatService)
                .build();
    }
}
