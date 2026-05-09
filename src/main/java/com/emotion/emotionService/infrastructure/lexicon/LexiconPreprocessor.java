package com.emotion.emotionService.infrastructure.lexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class LexiconPreprocessor {

  private static final Set<String> STOP_WORDS =
      Set.of(
          "a",
          "an",
          "and",
          "are",
          "as",
          "at",
          "be",
          "been",
          "being",
          "but",
          "by",
          "for",
          "from",
          "he",
          "her",
          "his",
          "i",
          "if",
          "in",
          "is",
          "it",
          "its",
          "me",
          "my",
          "of",
          "on",
          "or",
          "our",
          "she",
          "so",
          "that",
          "the",
          "their",
          "them",
          "then",
          "these",
          "they",
          "this",
          "those",
          "to",
          "us",
          "was",
          "we",
          "were",
          "with",
          "you",
          "your");

  private LexiconPreprocessor() {}

  static List<Token> tokenizeWithNegation(String text) {
    List<Token> tokens = new ArrayList<>();
    if (text == null) {
      return tokens;
    }

    String normalized = text.toLowerCase().replaceAll("[^a-z\\s]", " ");
    String[] rawTokens = normalized.trim().split("\\s+");
    if (rawTokens.length == 1 && rawTokens[0].isEmpty()) {
      return tokens;
    }

    for (int i = 0; i < rawTokens.length; i++) {
      String token = rawTokens[i];
      if (token.isEmpty()) {
        continue;
      }

      if ("not".equals(token)) {
        int nextIndex = i + 1;
        if (nextIndex < rawTokens.length) {
          String nextToken = rawTokens[nextIndex];
          if (!nextToken.isEmpty() && !STOP_WORDS.contains(nextToken)) {
            tokens.add(new Token(nextToken, true));
          }
          i = nextIndex;
        }
        continue;
      }

      if (STOP_WORDS.contains(token)) {
        continue;
      }

      tokens.add(new Token(token, false));
    }

    return tokens;
  }

  static int countWords(String text) {
    if (text == null) {
      return 0;
    }
    String normalized = text.toLowerCase().replaceAll("[^a-z\\s]", " ").trim();
    if (normalized.isEmpty()) {
      return 0;
    }
    return normalized.split("\\s+").length;
  }

  static final class Token {
    private final String word;
    private final boolean negated;

    private Token(String word, boolean negated) {
      this.word = word;
      this.negated = negated;
    }

    String word() {
      return word;
    }

    boolean negated() {
      return negated;
    }
  }
}

