# Spring AI Integration Architecture

## 1. What is Spring AI?
Spring AI is a Spring ecosystem abstraction framework for integrating Artificial Intelligence (AI) and Large Language Models (LLMs) into Spring applications.

It provides standard interfaces (`ChatClient`, `Prompt`, `ChatResponse`) allowing developers to switch between model providers (OpenAI, Ollama, Anthropic, DeepSeek) without changing application code.

---

## 2. Spring AI Financial Insights Pattern
- **Concept**: Generate natural-language expense advice by feeding monthly financial summary DTOs into an LLM prompt.
- **Prompt Template Structure**:
```text
You are a personal financial advisor. Analyze the following monthly financial summary:
- Month/Year: {month}/{year}
- Total Income: ${totalIncome}
- Total Expense: ${totalExpense}
- Net Savings: ${netSavings}
- Category Breakdown: {categoryBreakdown}
```
