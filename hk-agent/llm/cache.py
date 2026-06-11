from __future__ import annotations

import copy
import hashlib
import json
import time
from collections import OrderedDict
from dataclasses import dataclass
from typing import Any


@dataclass
class _CacheItem:
    value: Any
    expires_at: float
    size: int


class LLMResponseCache:
    def __init__(self, max_size: int = 128, ttl_seconds: int = 600, max_item_bytes: int = 256_000) -> None:
        self.max_size = max_size
        self.ttl_seconds = ttl_seconds
        self.max_item_bytes = max_item_bytes
        self._items: OrderedDict[str, _CacheItem] = OrderedDict()

    def make_key(self, *parts: Any) -> str:
        payload = json.dumps(parts, ensure_ascii=False, sort_keys=True, default=str)
        return hashlib.sha256(payload.encode("utf-8")).hexdigest()

    def get(self, key: str) -> Any | None:
        if key not in self._items:
            return None
        item = self._items.pop(key)
        if item.expires_at < time.time():
            return None
        self._items[key] = item
        return copy.deepcopy(item.value)

    def set(self, key: str, value: Any) -> None:
        size = _json_size(value)
        if size > self.max_item_bytes:
            return
        if key in self._items:
            self._items.pop(key)
        self._items[key] = _CacheItem(
            value=copy.deepcopy(value),
            expires_at=time.time() + self.ttl_seconds,
            size=size,
        )
        while len(self._items) > self.max_size:
            self._items.popitem(last=False)


def _json_size(value: Any) -> int:
    return len(json.dumps(value, ensure_ascii=False, default=str).encode("utf-8"))
