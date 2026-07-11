# Wheelie

A single-purpose, ad-free spinning wheel app for gamifying chores and tasks.

## Why

Free "spin the wheel" apps on the Play Store bury simple task entry behind
multiple menus, categories, and ad interstitials. Wheelie strips that away:
open the app, see the wheel, tap + to add a task, spin. That's it.

## Core idea

Add tasks (e.g. "mow the lawn", "pack away shopping") as unstructured text
entries. They appear as slices on a spinning wheel. Spin to pick one at
random. Mark it **Completed** to remove it, or **Skip** to try again later.
No lists, no categories, no folders — everything lives on the wheel.

## Features (MVP)

- Add a task via FAB — single text field, nothing else
- Spin the wheel with a realistic deceleration animation
- Landing on a task opens a dialog: **Completed** (removes it) or **Skip**
  (leaves it, dismiss dialog)
- Wheel always shows a minimum of 4 slices — tasks are duplicated evenly
  (e.g. 3 tasks → A,B,C,A,B,C) so the wheel never feels empty or unfair
- Duplicates are two independent entries: completing one only removes that
  copy, so you can still land on the same task again
- Up to 20 tasks shown on the wheel at once; additional tasks queue in the
  background and fill slots as they free up (oldest first)
- Dialog and current screen state (wheel vs. task dialog) survive app
  restarts, rotation, and process death — reopen the app and you're exactly
  where you left off
- Empty state when there are no tasks
- 100% ad-free, no network access required

## Tech stack

- Kotlin, Android Studio
- Room (single-table persistence: `id`, `text`, `createdAt`)
- Custom `View` for wheel rendering (Canvas-drawn wedges)
- `ValueAnimator` for spin physics
- SharedPreferences for lightweight UI-state persistence (pending task /
  current screen)

## Development phases

1. **Data layer** — Room entity + DAO, slot-filling logic (duplication,
   20-slot cap, FIFO promotion), unit + instrumented tests
2. **WheelView (static)** — custom view rendering N wedges from a slot list,
   dynamic text sizing, empty-state rendering
3. **Spin mechanic** — animated rotation with deceleration, random landing
   slot calculation
4. **Task dialog + FAB** — add-task dialog, Completed/Skip result dialog,
   FAB visibility tied to spin/dialog state
5. **Persistence across process death** — SharedPreferences-backed state
   restoration on launch
6. **Polish** — empty-state design, app icon, optional haptics/animation
   refinement

## Status

🚧 In development — Phase 1 (data layer)

## License

Personal project — license TBD.
