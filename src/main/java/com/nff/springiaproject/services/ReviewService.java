package com.nff.springiaproject.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private static final int MAX_REVIEW_LENGTH = 500;

    private final ChatClient chatClient;

    public ReviewService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateReview(String title) {
        try {
            String review = chatClient.prompt()
                    .user("Com base apenas no seu conhecimento, sem consultar fontes externas, "
                            + "escreva um resumo objetivo e direto do livro com o título: " + title
                            + ". Responda em português, sem emitir opinião e sem repetir o título. "
                            + "A resposta deve ter no máximo 500 caracteres.")
                    .call()
                    .content();

            if (review.codePointCount(0, review.length()) > MAX_REVIEW_LENGTH) {
                int endIndex = review.offsetByCodePoints(0, MAX_REVIEW_LENGTH);
                return review.substring(0, endIndex);
            }

            return review;
        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao gerar o review; esse erro precisa ser tratado." + e.getMessage());
            // Aqui caberia tratamento adequado, envio para fila de erro, retentativa ou circuit breaker.
            return null;
        }
    }
}
