import json
import os
from typing import Optional
from urllib import error, request


class LlmService:
    def __init__(self) -> None:
        self.provider = os.getenv("LLM_PROVIDER", "openai").lower()
        self.api_key = self._resolve_api_key()
        self.model = self._resolve_model()
        self.base_url = self._resolve_base_url()
        self.last_error: Optional[str] = None

    def generate(self, system_prompt: str, user_prompt: str) -> Optional[str]:
        self.last_error = None
        if self.provider not in {"openai", "groq"}:
            self.last_error = f"unsupported_provider:{self.provider}"
            return None
        if not self._has_real_api_key():
            self.last_error = "missing_api_key"
            return None

        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.2,
        }

        http_request = request.Request(
            url=f"{self.base_url.rstrip('/')}/chat/completions",
            data=json.dumps(payload).encode("utf-8"),
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
                "User-Agent": "CAISIP/1.0",
            },
            method="POST",
        )

        try:
            with request.urlopen(http_request, timeout=20) as response:
                parsed = json.loads(response.read().decode("utf-8"))
                return parsed["choices"][0]["message"]["content"].strip()
        except error.HTTPError as exc:
            self.last_error = self._parse_http_error(exc)
            return None
        except (error.URLError, TimeoutError) as exc:
            self.last_error = type(exc).__name__
            return None
        except (KeyError, IndexError, json.JSONDecodeError) as exc:
            self.last_error = f"bad_response:{type(exc).__name__}"
            return None

    def _has_real_api_key(self) -> bool:
        if not self.api_key:
            return False
        return self.api_key.strip().lower() not in {"replace-me", "changeme", "none", "null"}

    def _resolve_api_key(self) -> Optional[str]:
        if self.provider == "groq":
            return os.getenv("GROQ_API_KEY") or os.getenv("LLM_API_KEY")
        return os.getenv("OPENAI_API_KEY") or os.getenv("LLM_API_KEY")

    def _resolve_model(self) -> str:
        if self.provider == "groq":
            return os.getenv("GROQ_MODEL", "llama-3.1-8b-instant")
        return os.getenv("OPENAI_MODEL", "gpt-4o-mini")

    def _resolve_base_url(self) -> str:
        if self.provider == "groq":
            return os.getenv("GROQ_BASE_URL", "https://api.groq.com/openai/v1")
        return os.getenv("OPENAI_BASE_URL", "https://api.openai.com/v1")

    def _parse_http_error(self, exc: error.HTTPError) -> str:
        try:
            payload = json.loads(exc.read().decode("utf-8"))
            code = payload.get("error", {}).get("code")
            return f"http_{exc.code}:{code or 'openai_error'}"
        except (json.JSONDecodeError, UnicodeDecodeError):
            return f"http_{exc.code}:openai_error"


llm_service = LlmService()
