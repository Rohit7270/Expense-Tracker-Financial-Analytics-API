# Ollama & DeepSeek Local LLM Setup

## 1. Local AI Architecture

```text
Local Workstation / Docker
│
├── Ollama Server (Port 11434)
│     └── Model: `deepseek-r1:1.5b` or `llama3.2`
│
└── Expense Tracker API (Spring Boot)
      └── Spring AI Ollama Starter
```

---

## 2. Setting Up Ollama Locally

```bash
# 1. Install Ollama CLI
# Visit https://ollama.com

# 2. Pull & run DeepSeek / Llama model
ollama run deepseek-r1:1.5b

# 3. Verify local REST endpoint
curl http://localhost:11434/api/generate -d '{
  "model": "deepseek-r1:1.5b",
  "prompt": "Hello!"
}'
```

---

## 3. Application Properties Setup

In `application.properties`:
```properties
# Enable AI Integration
spring.ai.enabled=true

# Ollama Model Config
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=deepseek-r1:1.5b
spring.ai.ollama.chat.options.temperature=0.7
```
