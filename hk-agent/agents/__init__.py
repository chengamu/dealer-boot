from agents.fabric_cutting.agent import FabricCuttingAgent
from agents.registry import AgentRegistry


def create_registry() -> AgentRegistry:
    registry = AgentRegistry()
    registry.register(FabricCuttingAgent())
    return registry
