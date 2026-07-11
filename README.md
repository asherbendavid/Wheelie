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

## Features

- Add a task via FAB or the "Add Task" home screen shortcut — single text
  field, nothing else
- Spin the wheel with a realistic deceleration animation
- Landing on a task opens a dialog: **Completed** (removes it) or **Skip**
  (leaves it, dismiss dialog)
- Single remaining task skips the wheel entirely — tapping Spin opens the
  result dialog directly, no animation needed for a deterministic outcome
- Wheel always shows a minimum of 4 slices — tasks are duplicated evenly
  (e.g. 3 tasks -> A,B,C,A,B,C) so the wheel never feels empty or unfair
- Duplicates are two independent entries: completing one only removes that
  copy, so you can still land on the same task again (a sign from the
  universe, apparently)
- Up to 20 tasks shown on the wheel at once; additional tasks queue in the
  background and fill slots as they free up (oldest first)
- Dialog and current screen state (wheel vs. task dialog) survive app
  restarts, rotation, process death, app updates, and full device reboots -
  reopen the app and you're exactly where you left off
- Empty state when there are no tasks
- **Add Task home screen shortcut**: long-press the app icon (or drag the
  shortcut to the home screen) to add a task without opening the full app,
  via a lightweight dialog-only activity
- Portrait-locked (no landscape layout maintained - not needed for this
  app's use case)
- 100% ad-free, no network access required

## Tech stack

- Kotlin, Android Studio
- Room (single-table persistence: `id`, `text`, `createdAt`)
- Custom `View` for wheel rendering (Canvas-drawn wedges, `StaticLayout`
  for wrapped text)
- `ValueAnimator` for spin physics
- SharedPreferences for lightweight UI-state persistence (pending result
  dialog)
- Android App Shortcuts (`shortcuts.xml`) + a dialog-themed `Activity` for
  the home screen "Add Task" shortcut

## Design decisions (for future me)

- **Slot duplication is display-only.** `buildSlots()` never writes
  duplicate rows to Room - it takes the real task list and produces a
  padded `List<TaskSlot>` (each still pointing at a real `taskId`) purely
  for rendering. This is what makes "complete one duplicate, the other
  stays" work correctly, and keeps Room's data model simple (no need to
  track which duplicate is which).
- **The result dialog is the single source of truth for "in-progress"
  state**, not the wheel or the add-task flow. Only the result dialog gets
  SharedPreferences persistence (`PendingTaskStore`) - losing an
  in-progress *add* on process death was judged a non-issue (re-type a few
  words), but losing track of "which task did I just land on" after a
  crash would be a real, confusing bug. Don't be tempted to persist
  add-task state too; it's not worth the complexity for what it protects.
- **`AddTaskDialogFragment` is intentionally host-agnostic about UI, but
  not about Room.** It owns the `AppDatabase` insert directly rather than
  returning a result to whoever hosts it. This was a deliberate simplicity
  choice for a single-purpose app - every host (`MainActivity`,
  `QuickAddActivity`) would do the identical insert anyway, so a
  callback-based "DB-agnostic" version would have added indirection with
  no real reuse benefit. If Wheelie ever needs a host that *shouldn't*
  write to Room directly, revisit this.
- **The shortcut reuses `AddTaskDialogFragment` as-is.**
  `QuickAddActivity` is just a themed `Activity` shell that shows the
  fragment and calls `finish()` in `onTaskAdded`. Zero duplicated dialog
  or insert logic between the full app and the shortcut. This worked out
  cleanly *because* of the host-agnostic-UI decision above - worth
  remembering as a pattern for any future single-purpose add-ons.
- **Text sizing/measurement must use `sp`, not raw pixels, and must use
  actual `Paint.measureText()` for truncation, not a fixed character
  count.** Both were bugs caught during testing across two physical
  devices with different densities/screen sizes - raw-pixel sizing looked
  fine on one device and oversized on another; character-count truncation
  overflowed wedge bounds on narrower screens even at a "safe" character
  limit. Any future text-in-canvas work should assume this from the start.
- **The landing-slot calculation has a real coordinate-system gotcha**:
  `Canvas.drawArc`'s 0 degrees is 3 o'clock, sweeping clockwise, but a
  top-mounted pointer is visually at 270 degrees in that system, not 0.
  Getting this wrong produces a subtle "off-by-one-ish" bug where the
  reported task doesn't match the wedge under the pointer. See
  `calculateLandedIndex()` in `MainActivity.kt` for the corrected formula
  and reasoning in comments.
- **App Widget was considered and deliberately dropped** in favor of a
  pinned App Shortcut. A resizable, potentially-live `AppWidgetProvider`
  subsystem was unnecessary bloat for what turned out to be achievable
  with a static shortcut + a one-tap dialog activity - same end-user
  result (an icon on the home screen that adds a task), far less new
  platform surface area. Worth remembering this comparison before reaching
  for a widget on future personal apps: check whether a shortcut already
  gets you there first.

## Lessons learned

- **Windows usernames/paths containing apostrophes can break Gradle's unit
  test runner** (`ClassNotFoundException` on a class that compiles fine
  and whose `.class` file genuinely exists on disk) even when everything
  else about the build succeeds. Moving the project to an apostrophe-free
  path fixed it outright. If a from-scratch clean build still can't find
  a compiled test class, check the account/path for special characters
  before chasing anything else.
- **KSP versions are tied to a specific Kotlin version** in the format
  `<kotlin-version>-<ksp-version>` - this isn't derivable, it has to be
  looked up (KSP GitHub releases) against whatever Kotlin version the
  project is on.
- **AAR metadata errors** ("this dependency requires compiling against a
  higher API level than your project's compileSdk") can be fixed short-term
  by pinning the offending dependency back to an older version in
  `libs.versions.toml` - a legitimate, low-risk strategy for a hobby
  project, not just a hack. Full AGP/compileSdk upgrades are a separate,
  deliberate task, not something to do reactively mid-feature.
- **`AlertDialog`'s `setCancelable(false)` alone doesn't reliably block
  both outside-touch and back-button dismissal.** Needed explicit
  `setCanceledOnTouchOutside(false)` plus a `setOnKeyListener` intercepting
  `KEYCODE_BACK` to get fully "modal, can't accidentally dismiss" behavior.
- **`DisplayMetrics.scaledDensity` is deprecated** (inaccurate due to
  non-linear font scaling in newer Android versions) - use
  `TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics)`
  for sp-to-px conversion instead.
- **`StaticLayout.Builder.obtain()` requires `TextPaint`, not plain
  `Paint`** - an easy type mismatch to hit when reusing an existing Paint
  for wrapped-text rendering.

## Omissions / deliberately not built

- No task list/editing screen - by design, part of the "no select list,
  no categories" zero-bloat mission from day one. Deleting an unwanted
  task means spinning to it and marking it Completed, or (rare edge case)
  living with it until it comes up.
- No resizable/live App Widget - see design decisions above.
- No haptics on spin/landing - deliberately excluded; personal preference,
  not a gap.
- No landscape layout - deliberately excluded, portrait-only use case.
- Keyboard doesn't auto-focus/auto-show when the add-task dialog opens
  (attempted via `SOFT_INPUT_STATE_VISIBLE`, didn't reliably work in
  testing). Minor, non-blocking, left as a known gap rather than sunk
  further time into it.
- Room's schema export warning (`exportSchema` not explicitly configured)
  is left as-is - not worth addressing for a single-table app with no
  planned migrations.

## Development phases (as actually built)

1. **Data layer** - Room entity + DAO, slot-filling logic (duplication,
   20-slot cap, FIFO promotion), unit + instrumented tests - complete
2. **WheelView (static)** - custom view rendering N wedges from a slot
   list, dynamic sp-based text sizing with measure-based truncation,
   empty-state and single-task-state rendering - complete
3. **Spin mechanic** - `ValueAnimator` rotation with deceleration, random
   landing slot calculation, fixed pointer marker - complete
4. **Task dialog + FAB** - add-task dialog (reusable `DialogFragment`),
   Completed/Skip result dialog (locked against accidental dismissal),
   FAB tied to spin state, live Room data - complete
5. **Persistence across process death** - SharedPreferences-backed
   pending-result-dialog state, portrait lock - complete
6. **App icon + home screen shortcut** - adaptive icon (7-slice rainbow
   wheel), "Add Task" shortcut via `shortcuts.xml` + `QuickAddActivity`
   reusing the existing `AddTaskDialogFragment` - complete

Feature-complete as of this writing. No further phases currently planned.

## License

Personal project - license TBD.
