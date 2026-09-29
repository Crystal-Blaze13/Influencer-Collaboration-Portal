# Legacy version (archived)

This folder preserves the original text-file based console program (the university OOP project) so the
rewrite in the repository root can be compared against it. It is **not maintained**.

* `src/` - the original Java sources (packages `Users`, `Functions`, `FileReaders`, `FileWriters`), unchanged
  except that the hard-coded `/Users/palakkshetrapal/Documents/FinalOops/src/...` file paths were replaced with
  `legacy/data/...`, relative to the repository root.
* `data/` - the original `.txt` "database". **Passwords have been replaced with `REDACTED`**, so logging in to
  the legacy program no longer works from this copy.
* `docs/` - the original project report (`.docx`).
* The original commit is also tagged `legacy-original` in git (`git show legacy-original:src/Functions/Platform.java`).

To compile it anyway: `javac -d /tmp/legacy-out $(find legacy/src -name '*.java')` and run
`java -cp /tmp/legacy-out Functions.Platform` from the repository root.

## Audit of the original program

The original was compiled and run before any change (JDK 21). Findings:

### Did not run outside the author's machine
* All four data files were read from `/Users/palakkshetrapal/Documents/FinalOops/src/`. On any other machine every
  reader printed `Error ... (No such file or directory)` and the program then crashed on the first prompt.
  Data files sat inside the source tree, duplicated in `bin/`, next to committed `.class` files.

### Correctness
* Every menu created its own `new Scanner(System.in)`. With piped or scripted input the first Scanner buffers the
  whole stream and later Scanners see nothing (observed: `NoSuchElementException` at the login prompt).
* Only *adding/removing users* was saved. Campaigns, contract signatures, payments, budgets, earnings, password
  changes, niche/platform edits were all lost when the program exited.
* Brand-manager "Create Campaign" used `return` on failure, which logged the user out of the whole menu.
* `Admin.addCampaign` was never called, so the admin "View Campaigns" screen could never show a campaign.
* Engagement rate was not an engagement rate: the constructor used `(0.4*platforms + 0.6*followers)/10000`, while
  `updateEngRate` used `0.04*platforms + 0.06*followers` (a different scale), so the value depended on follower count.
* Payment amount was `contracts * engagementRate * 1000`, a formula unrelated to any agreed fee. Money was `double`.
* A campaign's budget was copied at creation; a later budget change and the contract copy could drift apart.
* Advertiser commission grew by 1 percentage point per contract (`commission += 0.01 * contracts`) with no limit.
* Non-numeric input to `nextInt()` crashed the program (`InputMismatchException`).
* Removing a user left campaigns pointing at the removed user.
* No state machine: a contract was just a boolean, "stop campaign" was a boolean, and states could not be reasoned about.

### Security
* Passwords stored and compared in plaintext, and committed to git (`admin.txt` held admin credentials).
* Any advertiser could add arbitrary amounts to their own earnings ("Update Earnings" menu item).
* Duplicate usernames were accepted at registration; login only checked the first match per role.

### Design
* `Platform` was a 600-line class mixing menus, registration, data loading (static initialiser) and control flow;
  registration and admin "add user" code was copy-pasted.
* Arrays were resized by hand with `Arrays.copyOf`.
* Domain classes printed to the console and read from files directly.
