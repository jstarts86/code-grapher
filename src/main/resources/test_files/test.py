"""A small but expressive test file for the Code Grapher tool."""

# (import_statement ; [2, 0] - [2, 18]
#   name: (aliased_import ; [2, 7] - [2, 18]
#     name: (dotted_name ; [2, 7] - [2, 12]
#       (identifier)) ; [2, 7] - [2, 12]
#     alias: (identifier))) ; [2, 16] - [2, 18]
# (import_from_statement ; [3, 0] - [3, 34]
#   module_name: (dotted_name ; [3, 5] - [3, 12]
#     (identifier)) ; [3, 5] - [3, 12]
#   name: (dotted_name ; [3, 20] - [3, 24]
#     (identifier)) ; [3, 20] - [3, 24]
#   name: (dotted_name ; [3, 26] - [3, 34]
#     (identifier))) ; [3, 26] - [3, 34]
# (import_from_statement ; [4, 0] - [4, 20]
#   module_name: (relative_import ; [4, 5] - [4, 7]
#     (import_prefix)) ; [4, 5] - [4, 7]
#   name: (dotted_name ; [4, 15] - [4, 20]
#     (identifier))) ; [4, 15] - [4, 20]
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
