"""Schemas for the liveness endpoint."""

from enum import Enum

from pydantic import BaseModel


class LivenessChallenge(str, Enum):
    TURN_LEFT = "turn_left"
    TURN_RIGHT = "turn_right"
    BLINK = "blink"


class LivenessResponse(BaseModel):
    passed: bool
    challenge: LivenessChallenge
    frames_analyzed: int
    metric: float
    threshold: float
    message: str
