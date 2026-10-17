package com.nff.springiaproject.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private static final int MAX_REVIEW_LENGTH = 500;

    private final ChatClient chatClient;

    public GeminiService(ChatClient.Builder chatClientBuilder) {
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
        } catch (Exception exception) {
            System.out.println("Ocorreu um erro ao gerar o review; esse erro precisa ser tratado."
                    + exception.getMessage());
            // Aqui caberia tratamento adequado, envio para fila de erro, retentativa ou circuit breaker.
            return null;
        }
    }

    public String generateRecommendations(String title, String author) {
        try {
            return chatClient.prompt()
                    .user("Com base apenas no seu conhecimento, sem consultar fontes externas, recomende "
                            + "exatamente 3 livros que possam agradar a leitores que gostaram da obra "
                            + "\"" + title + "\", de " + author + ". Baseie as recomendações nas características "
                            + "literárias da obra, como temas, estilo, gênero, atmosfera e narrativa. "
                            + "Não limite as recomendações ao mesmo autor. Responda em português, de forma curta, "
                            + "com o título e o autor de cada recomendação em uma lista numerada. Não inclua "
                            + "introdução nem repita o livro informado.")
                    .call()
                    .content();
        } catch (Exception exception) {
            return null;
        }
    }
}
