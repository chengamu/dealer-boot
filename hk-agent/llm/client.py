from __future__ import annotations

import json
import os
from pathlib import Path
from typing import Any

from openai import AsyncOpenAI

from infra.app_logger import log_event, text_hash
from llm.cache import LLMResponseCache
from llm.prompts import (
    CLARIFICATION_SYSTEM_PROMPT,
    CUTTING_REQUEST_SYSTEM_PROMPT,
    build_clarification_user_prompt,
)


class LLMClient:
    def __init__(self) -> None:
        _load_env_file()
        api_key = (
            os.getenv("DS_API_KEY")
            or os.getenv("DEEPSEEK_API_KEY")
            or os.getenv("OPENAI_API_KEY")
        )
        base_url = (
            os.getenv("DS_BASE_URL")
            or os.getenv("DEEPSEEK_BASE_URL")
            or os.getenv("OPENAI_BASE_URL")
        )
        self.model = os.getenv("DS_MODEL") or os.getenv("OPENAI_MODEL") or "deepseek-chat"
        self.enabled = bool(api_key)
        self._client = AsyncOpenAI(api_key=api_key, base_url=base_url, timeout=20) if api_key else None
        self._cache = LLMResponseCache()
        self.last_usage: dict[str, Any] | None = None

    async def extract_cutting_request(self, user_text: str) -> dict[str, Any] | None:
        if not self._client:
            return None
        cache_key = self._cache.make_key("extract", self.model, CUTTING_REQUEST_SYSTEM_PROMPT, user_text)
        cached = self._cache.get(cache_key)
        if cached is not None:
            log_event("llm_cache_hit", task="extract", model=self.model, cache_hash=cache_key[:16], input_hash=text_hash(user_text))
            self.last_usage = _usage_payload(self.model, "extract", cached=True, response=None)
            return cached
        log_event("llm_request", task="extract", model=self.model, cache_hash=cache_key[:16], input_hash=text_hash(user_text))

        try:
            response = await self._client.chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": CUTTING_REQUEST_SYSTEM_PROMPT},
                    {"role": "user", "content": user_text},
                ],
                response_format={"type": "json_object"},
                temperature=0,
            )
        except Exception as exc:
            log_event("llm_error", task="extract", model=self.model, cache_hash=cache_key[:16], error=type(exc).__name__)
            raise
        content = response.choices[0].message.content or "{}"
        result = json.loads(content)
        self._cache.set(cache_key, result)
        self.last_usage = _usage_payload(self.model, "extract", cached=False, response=response)
        log_event("llm_response", task="extract", model=self.model, cache_hash=cache_key[:16], usage=self.last_usage)
        return result

    async def explain_report(self, report_payload: dict[str, Any]) -> str | None:
        if not self._client:
            return None
        cache_payload = _strip_volatile_report_fields(report_payload)
        cache_key = self._cache.make_key("explain", self.model, cache_payload)
        cached = self._cache.get(cache_key)
        if cached is not None:
            log_event("llm_cache_hit", task="explain", model=self.model, cache_hash=cache_key[:16])
            self.last_usage = _usage_payload(self.model, "explain", cached=True, response=None)
            return cached
        log_event("llm_request", task="explain", model=self.model, cache_hash=cache_key[:16])

        try:
            response = await self._client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "你是窗饰工艺员。用简洁中文说明推荐门幅、用料、损耗和裁剪注意事项。",
                    },
                    {
                        "role": "user",
                        "content": json.dumps(cache_payload, ensure_ascii=False),
                    },
                ],
                temperature=0.2,
            )
        except Exception as exc:
            log_event("llm_error", task="explain", model=self.model, cache_hash=cache_key[:16], error=type(exc).__name__)
            raise
        result = response.choices[0].message.content
        self._cache.set(cache_key, result)
        self.last_usage = _usage_payload(self.model, "explain", cached=False, response=response)
        log_event("llm_response", task="explain", model=self.model, cache_hash=cache_key[:16], usage=self.last_usage)
        return result

    async def clarify_missing_info(
        self,
        *,
        user_text: str,
        missing_fields: list[str],
        request_data: dict[str, Any] | None = None,
        validation_error: str | None = None,
    ) -> str | None:
        if not self._client:
            return None
        user_prompt = build_clarification_user_prompt(
            user_text=user_text,
            missing_fields=missing_fields,
            request_data=request_data,
            validation_error=validation_error,
        )
        cache_key = self._cache.make_key("clarify", self.model, CLARIFICATION_SYSTEM_PROMPT, user_prompt)
        cached = self._cache.get(cache_key)
        if cached is not None:
            log_event("llm_cache_hit", task="clarify", model=self.model, cache_hash=cache_key[:16], input_hash=text_hash(user_text))
            self.last_usage = _usage_payload(self.model, "clarify", cached=True, response=None)
            return cached
        log_event("llm_request", task="clarify", model=self.model, cache_hash=cache_key[:16], input_hash=text_hash(user_text))

        try:
            response = await self._client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": CLARIFICATION_SYSTEM_PROMPT,
                    },
                    {
                        "role": "user",
                        "content": user_prompt,
                    },
                ],
                temperature=0.2,
            )
        except Exception as exc:
            log_event("llm_error", task="clarify", model=self.model, cache_hash=cache_key[:16], error=type(exc).__name__)
            raise
        result = response.choices[0].message.content
        self._cache.set(cache_key, result)
        self.last_usage = _usage_payload(self.model, "clarify", cached=False, response=response)
        log_event("llm_response", task="clarify", model=self.model, cache_hash=cache_key[:16], usage=self.last_usage)
        return result


def _load_env_file() -> None:
    env_path = Path.cwd() / ".env"
    if not env_path.exists():
        return
    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key:
            os.environ.setdefault(key, value)


def _strip_volatile_report_fields(value: Any) -> Any:
    if isinstance(value, dict):
        return {
            key: _strip_volatile_report_fields(item)
            for key, item in value.items()
            if key not in {"report_id"}
        }
    if isinstance(value, list):
        return [_strip_volatile_report_fields(item) for item in value]
    return value


def _usage_payload(model: str, task: str, *, cached: bool, response: Any | None) -> dict[str, Any]:
    usage = getattr(response, "usage", None)
    input_tokens = int(getattr(usage, "prompt_tokens", 0) or 0)
    output_tokens = int(getattr(usage, "completion_tokens", 0) or 0)
    total_tokens = int(getattr(usage, "total_tokens", input_tokens + output_tokens) or 0)
    input_price = float(os.getenv("DS_INPUT_PRICE_PER_1M", os.getenv("LLM_INPUT_PRICE_PER_1M", "1")) or 1)
    output_price = float(os.getenv("DS_OUTPUT_PRICE_PER_1M", os.getenv("LLM_OUTPUT_PRICE_PER_1M", "2")) or 2)
    estimated_cost = (input_tokens / 1_000_000 * input_price) + (output_tokens / 1_000_000 * output_price)
    return {
        "model": model,
        "task": task,
        "cached": cached,
        "input_tokens": input_tokens,
        "output_tokens": output_tokens,
        "total_tokens": total_tokens,
        "estimated_cost": round(estimated_cost, 8),
    }
