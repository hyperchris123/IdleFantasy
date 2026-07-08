# Adding wiki pages

Wiki pages are defined in [`wiki/src/pages.py`](src/pages.py). Adding a new page requires updating three places in that file, and usually adding a template under [`wiki/templates/`](../templates/).

Run `python -m src validity` from the `wiki/` directory after making changes to confirm the directory, hierarchy, and content mappings are consistent.

## 1. Register the page in `PAGE_DIRECTORY`

Every page needs an entry in `PAGE_DIRECTORY`. The key is a short **page ID** (lowercase, no spaces) used throughout the generator. The value is a `PageInfo` with the display title and output filename.

```python
PAGE_DIRECTORY: dict[str, PageInfo] = {
    # ...
    "agility": PageInfo("Agility", "Agility_Docs.md")
}
```

- **Page ID** — used whenever you need to refer to a specific page when generating content
- **Title** — shown in wiki links (e.g. `[[Agility|Agility_Docs]]`).
- **URL / filename** — the markdown file written to the wiki repo (e.g. `Agility.md`).

Special pages such as the sidebar use an underscore prefix and are not listed in the hierarchy as they shouldn't be linked to:

```python
"sidebar": PageInfo("Sidebar", "_Sidebar.md"),
```

## 2. Add the page to `PAGE_HIERARCHY`

`PAGE_HIERARCHY` controls navigation on the Home page and in `_Sidebar.md`. It is a nested tuple structure:

- A **leaf entry** is `(display_name, page_id)`.
- A **section** is `(section_name, (child_entries...))`.

```python
PAGE_HIERARCHY = (
    ("Home", "home"),
    ("Gathering", (
        ("Mining", "mining"),
        ("Agility", "agility"),
        # ...
    )),
)
```

Pages in the `PAGE_HIERARCHY` show up in the sidebar, FAQ, and any other places which list all the pages in the wiki.

Every normal page (non-underscore filename) in `PAGE_DIRECTORY` should appear somewhere in the hierarchy. Pages with underscore-prefixed filenames (e.g. `_Sidebar.md`) are excluded from this requirement.

## 3. Wire up content in `_get_page_to_content()`

`_get_page_to_content()` maps each page ID to its generated markdown string. Every entry in `PAGE_DIRECTORY` must have a corresponding key here.

```python
def _get_page_to_content() -> dict[str, str]:
    return {
        "home": gen_home(),
        "sidebar": gen_sidebar(),
        "agility": gen_agility(),
        # ...
    }
```

`get_pages()` uses this dict together with `PAGE_DIRECTORY` to produce the final `{filename: content}` mapping written to disk.

## 4. Implement a `gen_*` function (and template)

Each page typically has a `gen_<page_id>()` function in the **Page Creation** section of `pages.py`. These functions load game data from `app/src/main/assets/data/` and return formatted markdown.

Common helpers:

| Helper                    | Purpose                                |
|---------------------------|----------------------------------------|
| `load("some_file.json")`  | Load a JSON asset                      |
| `get_template("agility")` | Load a template from `wiki/templates/` |
| `table(headers, rows)`    | Build a markdown table                 |
| `link("agility")`         | Build a wiki link to another page      |
| `title("iron_ore")`       | Format an asset key as a title         |

Example pattern (see `gen_agility()`):

1. Add a template file, e.g. `wiki/templates/agility.md`, with `{placeholder}` fields.
2. Implement `gen_agility()` to load data, build tables/rows, and call `get_template("agility").format(...)`.
3. Register the result in `_get_page_to_content()`.

## Checklist

1. Add a `PageInfo` entry to `PAGE_DIRECTORY`.
2. Add a `(Name, page_id)` entry (or nest it under a section) in `PAGE_HIERARCHY`.
3. Implement `gen_<page_id>()` and any template file.
4. Add `"page_id": gen_<page_id>()` to `_get_page_to_content()`.
5. Run `python -m src validity` and fix any reported errors or warnings.
