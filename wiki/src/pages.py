"""
pages.py - Defines methods for generating IdleFantasy GitHub wiki pages from game data assets

Reads JSON assets from app/src/main/assets/data/ and generates appropriate markdown content
"""

from __future__ import annotations

import json
import traceback
from dataclasses import dataclass

from wiki.src import ASSETS, TEMPLATES

# ---------------------------------------------------------------------------
# Page Listings
# ---------------------------------------------------------------------------

@dataclass
class PageInfo:
    title: str
    url: str


PAGE_DIRECTORY: dict[str, PageInfo] = {
    "sidebar": PageInfo("Sidebar", "_Sidebar.md"),
    "home": PageInfo("Home", "Home.md"),
    "skills": PageInfo("Skills", "Skills.md"),
    "mining": PageInfo("Mining", "Mining.md"),
    "fishing": PageInfo("Fishing", "Fishing.md"),
    "woodcutting": PageInfo("Woodcutting", "Woodcutting.md"),
    "farming": PageInfo("Farming", "Farming.md"),
    "agility": PageInfo("Agility", "Agility.md"),
    "smithing": PageInfo("Smithing", "Smithing.md"),
    "cooking": PageInfo("Cooking", "Cooking.md"),
    "fletching": PageInfo("Fletching", "Fletching.md"),
    "crafting": PageInfo("Crafting", "Crafting.md"),
    "firemaking": PageInfo("Firemaking", "Firemaking.md"),
    "runecrafting": PageInfo("Runecrafting", "Runecrafting.md"),
    "herblore": PageInfo("Herblore", "Herblore.md"),
    "prayer": PageInfo("Prayer", "Prayer.md"),
    "mercantile": PageInfo("Mercantile", "Mercantile.md"),
    "slayer": PageInfo("Slayer", "Slayer.md"),
}

PAGE_HIERARCHY = (
    ("Home", "home"),
    ("Skills", "skills"),
    ("Gathering", (
        ("Mining", "mining"),
        ("Fishing", "fishing"),
        ("Woodcutting", "woodcutting"),
        ("Farming", "farming"),
        ("Agility", "agility"),
    )),
    ("Crafting", (
        ("Smithing", "smithing"),
        ("Cooking", "cooking"),
        ("Fletching", "fletching"),
        ("Crafting", "crafting"),
        ("Firemaking", "firemaking"),
        ("Runecrafting", "runecrafting"),
        ("Herblore", "herblore"),
    )),
    ("Support", (
        ("Prayer", "prayer"),
        ("Mercantile", "mercantile"),
    )),
    ("Combat", (
        ("Slayer", "slayer"),
    )),
)


def _get_page_to_content() -> dict[str, str]:
    return {
        "home": gen_home(),
        "sidebar": gen_sidebar(),
        "skills": gen_skills()
    }


def get_pages() -> dict[str, str]:
    page_to_content = _get_page_to_content()
    return {PAGE_DIRECTORY[page].url: content for page, content in page_to_content.items()}


def check_wiki_validity():
    # Check hierarchy and directory links
    # Get all pages in the hierarchy
    pages_in_hierarchy = []
    listing_items = list(PAGE_HIERARCHY)
    while len(listing_items) > 0:
        item = listing_items.pop(0)
        if isinstance(item[1], str):
            pages_in_hierarchy.append(item[1])
        else:
            listing_items += list(item[1])
    # Confirm page listing has all pages
    for page in pages_in_hierarchy:
        if page not in PAGE_DIRECTORY:
            print(f"Critical: Page '{page}' is listed in the hierarchy but not in the directory")
    # Confirm all directory items are in the hierarchy excluding special pages (eg. Sidebar/Footer)
    for page_id, page_info in PAGE_DIRECTORY.items():
        if page_id not in pages_in_hierarchy and not page_info.url.startswith("_"):
            print(f"Warning: Page '{page_id}' is listed in the directory but not present in the hierarchy")

    # Ensure all pages have associated content
    try:
        page_to_content = _get_page_to_content()
        for page in PAGE_DIRECTORY.keys():
            if page not in page_to_content:
                print(f"Critical: Page '{page}' does not have any content")
    except KeyError:
        print(f"Error: Content test failed due to below issue")
        print(f"\033[91m{traceback.format_exc()}\033[00m")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def get_template(name: str) -> str:
    """Gets a template file by name"""
    with open(TEMPLATES / f"{name}.md") as f:
        return f.read()


def load(rel_path: str) -> dict | list:
    return json.loads((ASSETS / rel_path).read_text())


def title(key: str) -> str:
    return key.replace("_", " ").title()


def fmt_materials(mats: dict) -> str:
    return ", ".join(f"{qty}× {title(item)}" for item, qty in mats.items())


def fmt_pct(chance: float) -> str:
    pct = chance * 100
    return f"{pct:.1f}%" if pct < 1 else f"{pct:.0f}%"


def table(headers: list[str], rows: list[list]) -> str:
    sep = " | "
    header_row  = sep.join(headers)
    divider_row = sep.join("---" for _ in headers)
    data_rows   = "\n".join(sep.join(str(c) for c in row) for row in rows)
    return f"| {header_row} |\n| {divider_row} |\n" + "\n".join(f"| {sep.join(str(c) for c in row)} |" for row in rows)


def session_minutes(level: int) -> int:
    """Mirrors SkillSimulator.sessionDurationMs() — 60→40 min linear across levels 1–99."""
    fraction = (level - 1) / 98.0
    return round(60 - 20 * fraction)


def link(page_id: str):
    page = PAGE_DIRECTORY[page_id]
    return f"[[{page.title}|{page.url.removesuffix('.md')}]]"

# ---------------------------------------------------------------------------
# Page Creation
# ---------------------------------------------------------------------------

def _gen_page_listing(pages, level: int = 2) -> str:
    content = ""
    for name, value in pages:
        if isinstance(value, str): # Add link
            content += f"- {link(value)}"
        else: # Add subsection
            content += f"\n{"#" * level} {name}\n"
            content += f"{_gen_page_listing(value, level + 1)}\n"
    # Return content without trailing newline/etc
    return content.strip()


def gen_home() -> str:
    links = _gen_page_listing(PAGE_HIERARCHY, 3)
    return get_template("home").format(links=links)


def gen_sidebar() -> str:
    return _gen_page_listing(PAGE_HIERARCHY)


def gen_skills() -> str:
    skill_list = [
        ("Mining", "gathering", "Extract ores and gems from the earth."),
        ("Fishing", "gathering", "Catch fish and aquatic creatures."),
        ("Woodcutting", "gathering", "Chop trees for logs."),
        ("Farming", "gathering", "Plant seeds and harvest crops."),
        ("Firemaking", "gathering", "Burn logs for XP. Produces ashes for Prayer."),
        ("Agility", "gathering", "Reduces session time across all skills (60→40 min at level 99)."),
        ("Mercantile", "gathering",
         "Send trade caravans and explore skilling expeditions for lore and dungeon unlocks."),
        ("Smithing", "crafting", "Smelt ores into bars and forge equipment."),
        ("Cooking", "crafting", "Cook raw food to restore HP in combat."),
        ("Fletching", "crafting", "Craft bows and arrows."),
        ("Crafting", "crafting", "Make jewellery and other items."),
        ("Runecrafting", "crafting", "Craft runes from rune essence."),
        ("Herblore", "crafting", "Brew potions for combat stat boosts."),
        ("Attack", "combat", "Increases melee accuracy."),
        ("Strength", "combat", "Increases max melee damage."),
        ("Defense", "combat", "Reduces damage taken."),
        ("Ranged", "combat", "Attack from a distance with a bow."),
        ("Magic", "combat", "Cast spells using runes."),
        ("Hitpoints", "combat", "Total health. Increases with combat."),
        ("Prayer", "combat", "Bury bones to unlock combat prayers."),
        ("Slayer", "combat", "Receive tasks from the Slayer Master to kill specific enemies for bonus XP and points."),
    ]
    rows = [[link(skill.lower()) if skill.lower() in PAGE_DIRECTORY else skill, cat, desc] for skill, cat, desc
            in skill_list]
    return get_template("skills").format(skills_table=table(["Skill", "Category", "Description"], rows))


def gen_agility() -> str:
    courses = load("agility_courses.json")
    sorted_courses = sorted(courses.values(), key=lambda c: c["level_required"])

    course_rows = []
    for c in sorted_courses:
        laps_per_min = 2
        success_rate = 0.90  # approximate mid-point
        xp_per_min   = round(laps_per_min * c["xp_per_success"] * success_rate)
        xp_per_session = xp_per_min * 60
        course_rows.append([
            c["display_name"],
            c["level_required"],
            c["xp_per_success"],
            f"~{xp_per_min:,}",
            f"~{xp_per_session:,}",
        ])

    duration_rows = []
    for level in [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 99]:
        mins = session_minutes(level)
        duration_rows.append([level, f"{mins} min"])

    return get_template("agility").format(
        session_duration_table=table(["Agility Level", "Session Duration"], duration_rows),
        course_count=len(courses),
        course_table=table(['Course', 'Level Required', 'XP / Lap', 'XP / Min (est.)', 'XP / Session (est.)'], course_rows)
    )
