from __future__ import annotations

import copy
from typing import Iterator


class PageHierarchy:
    def __init__(self, name: str = ""):
        super().__init__()
        self.name = name
        self._internal: list[str | PageHierarchy] = []

    def _add_item(self, path: list[str], item, index: int | None = None):
        # Simple base case
        if len(path) == 0:
            if index is None:
                self._internal.append(item)
            else:
                self._internal.insert(index, item)
        # Walk through the path to find where to add location
        for item in self:
            if isinstance(item, PageHierarchy) and item.name == path[0]:
                try:
                    item._add_item(path[1:], index)
                except KeyError: # Ensures that the parent always takes care of any errors
                    raise KeyError(f"Could not find the parent sections for '{path}' in {self}")
        # Could not find
        raise KeyError(f"Could not find the parent sections for '{path}' in {self}")

    def insert_page(self, path: str, index: int | None = None):
        """Inserts a page into the hierarchy

        Usage:
            hierarchy.insert_page("/path/to/page", index) - Inserts a page at the given path

        Raises:
            KeyError: If the parent sections going to the page do not exist
        """
        path, page_name = path.rsplit("/", 1)
        self._add_item(path.split("/"), page_name, index)

    def insert_section(self, path: str, index: int | None = None):
        """Inserts a section into the hierarchy. A section allows more pages to be added

        Usage:
            hierarchy.insert_page("/path/to/page", index) - Inserts a page at the given path

        Raises:
            KeyError: If the parent sections going to the section do not exist
        """
        path, section_name = path.rsplit("/", 1)
        self._add_item(path.split("/"), PageHierarchy(section_name), index)

    def merge(self, other: PageHierarchy | list):
        """An inplace merge with another PageHierarchy instance or list

        All pages get appended to their respective hierarchies. If a nested section already exists, then the existing
        section will be merged with the listed section.
        The name of the hierarchy that it is getting merged with is ignored

        Example:
            hierarchy1 = PageHierarchy()
            hierarchy1.merge([
                ["Section 1", [
                    "page 1",
                    "page 2"
                ]]
            ])
            hierarchy2 = [
                ["Section 1", [
                    "page 3"
                ]]
            ]
            print(hierarchy1.merge(hierarchy2))

        """
        for item in other:
            if isinstance(item, str): # Simply append pages
                self._internal.append(item)
            elif isinstance(item, PageHierarchy): # Merge hierarchies
                if item.name in self:
                    self[item.name].merge(item)
                else:
                    self._internal.append(copy.deepcopy(item))
            elif isinstance(item, list): # Merge with lists using first item as name and second item as list of items
                if item[0] in self:
                    self[item[0]].merge(item[1])
                else:
                    nested_hierarchy = PageHierarchy(item[0])
                    nested_hierarchy.merge(item[1])
                    self._internal.append(nested_hierarchy)
            else:
                raise NotImplemented

    def __str__(self):
        return "{" + f"{self.name}: {self._internal.__str__()}" + "}"

    def __getitem__(self, path: str):
        split_path = path.split("/")
        if len(split_path) == 1: # Base case - find the actual item
            for item in self:
                if isinstance(item, PageHierarchy) and item.name == path or item == path:
                    return item
        else:
            next_path = split_path[0]
            for item in self:
                if isinstance(item, PageHierarchy) and item.name == next_path:
                    return item.__getitem__(str(split_path[1:]))
        raise KeyError(f"Could not find '{path}' in {self}")

    def __contains__(self, path: str):
        # Simply see if you can get the item or not
        try:
            self[path]
        except KeyError:
            return False
        return True

    def __iter__(self) -> Iterator[str | PageHierarchy]:
        return iter(self._internal)

    def __add__(self, other: PageHierarchy | list):
        new_hierarchy = copy.deepcopy(self)
        new_hierarchy.merge(other)
        return new_hierarchy

    def __copy__(self):
        duplicate = PageHierarchy(self.name)
        duplicate._internal = self._internal.copy()
        return duplicate

    def __deepcopy__(self, memo):
        if id(self) in memo:
            return memo[id(self)]
        duplicate = PageHierarchy(self.name)
        memo[id(self)] = duplicate
        duplicate._internal = [copy.deepcopy(item, memo) for item in self._internal]
        return duplicate

    def __len__(self):
        return self._internal.__len__()
