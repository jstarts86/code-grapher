"""A small but expressive test file for the Code Grapher tool."""

import numpy as np
from pathlib import Path, PurePath
from .. import utils
import os
import sys as system
from functools import lru_cache

# Module-level constant and function
PI = 3.14


def top_level_util(x: int, y: float) -> float:
    """Top level utility function."""
    result = x * y
    print(f"Product is {result}")
    return result


@lru_cache(maxsize=32)
def cached_compute(n: int) -> int:
    if n <= 1:
        return n
    # Recursive call should appear as a CALL edge to itself
    return cached_compute(n - 1) + cached_compute(n - 2)


class Chicken:
    kind = "generic"


class Shape:
    kind = "generic"

    def __init__(self, color: str = "red"):
        self.color = color

    def area(self) -> float:
        raise NotImplementedError("Subclasses must implement 'area'")

    @staticmethod
    def describe() -> str:
        return "Base shape class"


class Pair[T, U]:
    def __init__(self, first: T, second: U):
        self.first = first
        self.second = second

    def swap(self) -> "Pair[U, T]":
        return Pair(self.second, self.first)


class Circle(Shape, Chicken):
    def __init__(self, radius: float, color: str = "blue"):
        super().__init__(color)
        self.radius = radius

    def area(self) -> float:
        return PI * self.radius**2

    def scale(self, factor: float) -> "Circle":
        new_radius = self.radius * factor
        # Explicit call to top_level_util to test cross-scope CALL
        top_level_util(self.radius, factor)
        return Circle(new_radius, self.color)


def main() -> None:
    c = Circle(5.0)
    print(f"Area: {c.area()}")
    small = c.scale(0.5)
    print(f"Smaller area: {small.area()}")


if __name__ == "__main__":
    main()
