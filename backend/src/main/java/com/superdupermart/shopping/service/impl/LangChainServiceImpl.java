package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.entity.ChatMessage;
import com.superdupermart.shopping.service.AIService;
import com.superdupermart.shopping.service.RagService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("langChainService")
@Primary
public class LangChainServiceImpl implements AIService {

    private final ChatLanguageModel chatModel;
    private final RagService ragService;

    private static final String SYSTEM_PROMPT = """
            You are a helpful customer support assistant for SuperDuper Mart, an online shopping platform.

            You can help customers with:
            - Product information and recommendations
            - Order status inquiries
            - Return and refund policies
            - General shopping questions

            Be friendly, concise, and helpful. If you don't know something, say so honestly.

            Current store policies:
            - Free shipping on orders over $50
            - 30-day return policy for unused items
            - Customer service hours: 9 AM - 9 PM EST
            """;

    public LangChainServiceImpl(ChatLanguageModel chatModel, RagService ragService) {
        this.chatModel = chatModel;
        this.ragService = ragService;
    }

    @Override
    public String generateResponse(String userMessage, List<ChatMessage> conversationHistory) {
        try {
            // 1. Retrieve Context from existing Spring AI RAG service
            List<Document> docs = ragService.retrieveDocuments(userMessage);
            String context = formatRagContext(docs);

            // 2. Build Messages list for LangChain
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

            // System Message with Context
            String fullSystemPrompt = SYSTEM_PROMPT +
                    "\n\nRelevant Product Information:\n" + context +
                    "\n\nAnswer the user's question based on the above information.";

            messages.add(new SystemMessage(fullSystemPrompt));

            // History (limit to last 10 messages)
            int startIdx = Math.max(0, conversationHistory.size() - 10);
            for (int i = startIdx; i < conversationHistory.size(); i++) {
                ChatMessage msg = conversationHistory.get(i);
                if (msg.getRole() == ChatMessage.MessageRole.USER) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if (msg.getRole() == ChatMessage.MessageRole.ASSISTANT
                        || msg.getRole() == ChatMessage.MessageRole.SYSTEM) {
                    // Treat system messages in history as AI messages if needed, or ignore
                    messages.add(new AiMessage(msg.getContent()));
                }
            }

            // Current User Message
            messages.add(new UserMessage(userMessage));

            // 3. Generate Response
            Response<AiMessage> response = chatModel.generate(messages);
            return response.content().text();

        } catch (Exception e) {
            e.printStackTrace();
            return "I apologize, but I'm currently unable to process your request. Please try again later.";
        }
    }

    private String formatRagContext(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No specific product information found.";
        }
        return docs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));
    }
}
