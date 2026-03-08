package org.example.semantickernal;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final Kernel kernel;

    public ChatService(Kernel kernel) {
        this.kernel = kernel;
    }

    public List<ChatMessageContent<?>> chat(String userMessage) throws ServiceNotFoundException {
        ChatCompletionService chatCompletion = kernel.getService(ChatCompletionService.class);

        ChatHistory history = new ChatHistory();
        history.addUserMessage(userMessage);

        List<ChatMessageContent<?>> results = chatCompletion
                .getChatMessageContentsAsync(history, kernel, null)
                .block();

        return results;
    }
}