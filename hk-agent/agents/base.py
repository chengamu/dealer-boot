from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class Agent(ABC):
    """Common contract for all child agents."""

    agent_id: str

    @abstractmethod
    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:
        """Run the agent and return a JSON-serializable result."""
