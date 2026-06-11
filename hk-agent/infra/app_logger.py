from __future__ import annotations

import hashlib
import json
import os
from datetime import UTC, datetime, timedelta
from pathlib import Path
from typing import Any


LOG_DIR = Path(os.getenv("HK_AGENT_LOG_DIR", "logs"))
RETENTION_DAYS = int(os.getenv("HK_AGENT_LOG_RETENTION_DAYS", "30"))


def log_event(event: str, **fields: Any) -> None:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    _cleanup_old_logs()
    payload = {
        "ts": datetime.now(UTC).isoformat(),
        "event": event,
        **_redact(fields),
    }
    log_path = LOG_DIR / f"{datetime.now().date().isoformat()}.log"
    with log_path.open("a", encoding="utf-8") as file:
        file.write(json.dumps(payload, ensure_ascii=False, default=str) + "\n")


def text_hash(text: str | None) -> str | None:
    if not text:
        return None
    return hashlib.sha256(text.encode("utf-8")).hexdigest()[:16]


def _cleanup_old_logs() -> None:
    cutoff = datetime.now().date() - timedelta(days=RETENTION_DAYS)
    for path in LOG_DIR.glob("*.log"):
        try:
            log_date = datetime.strptime(path.stem, "%Y-%m-%d").date()
        except ValueError:
            continue
        if log_date < cutoff:
            path.unlink(missing_ok=True)


def _redact(value: Any) -> Any:
    if isinstance(value, dict):
        redacted: dict[str, Any] = {}
        for key, item in value.items():
            lower_key = str(key).lower()
            if any(secret in lower_key for secret in ["key", "token", "secret", "password"]):
                redacted[key] = "***"
            elif lower_key in {"text", "message", "user_text", "content", "prompt"}:
                redacted[f"{key}_hash"] = text_hash(str(item))
                redacted[f"{key}_length"] = len(str(item))
            else:
                redacted[key] = _redact(item)
        return redacted
    if isinstance(value, list):
        return [_redact(item) for item in value]
    return value
