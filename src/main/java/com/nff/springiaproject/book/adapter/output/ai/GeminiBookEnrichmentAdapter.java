package com.nff.springiaproject.book.adapter.output.ai;

import com.nff.springiaproject.book.domain.intefaces.BookEnrichment;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiBookEnrichmentAdapter implements BookEnrichment {

    private static final int MAX_REVIEW_LENGTH = 500;

    private final ChatClient chatClient;

    public GeminiBookEnrichmentAdapter(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public BookDetails enrich(String title, String author) {
        String review = generateReview(title);
        String recommendations = generateRecommendations(title, author);
        GeminiClassificationResponse classification = classify(title, author);

        if (classification == null) {
            return new BookDetails(review, recommendations, null, null, null, null);
        }

        return new BookDetails(
                review,
                recommendations,
                classification.genre(),
                classification.targetAudience(),
                classification.language(),
                classification.keywords()
        );
    }

    private String generateReview(String title) {
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
    }

    private String generateRecommendations(String title, String author) {
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
    }

    private GeminiClassificationResponse classify(String title, String author) {
        return chatClient.prompt()
                .user("Classifique o livro \"" + title + "\", de " + author + ". "
                        + "Retorne o gênero literário, o público-alvo, o idioma original e palavras-chave "
                        + "úteis para busca. Use português nas palavras-chave e no público-alvo. "
                        + "Se não souber algum campo com segurança, informe \"Desconhecido\". "
                        + "Retorne as palavras-chave como uma única string separada por vírgulas.")
                .call()
                .entity(GeminiClassificationResponse.class);
    }

    private record GeminiClassificationResponse(
            String genre,
            String targetAudience,
            String language,
            String keywords
    ) {
    }
}
