# Fix IntelliJ IDEA Java 21 Error

The error `release version 21 not supported` happens because IntelliJ is using an older Java version to run Maven.

## Step-by-Step Fix

### 1. Open Maven Settings
1. Press `Ctrl` + `Alt` + `S` to open **Settings**.
2. Go to **Build, Execution, Deployment** > **Build Tools** > **Maven** > **Runner**.

### 2. Change the JRE
1. Look for the **JRE** field on the right side.
2. It probably says `17` or `1.8` or `Use Project JDK`.
3. Click the dropdown and select **Java 21** (it might be named `corretto-21` or similar).
   - If you don't see 21, click the `...` (browse) button.
   - Navigate to: `/home/ratnesh/.jdks/corretto-21.0.9`

### 3. Check Project Structure
1. Press `Ctrl` + `Alt` + `Shift` + `S` to open **Project Structure**.
2. In **Project** settings (left sidebar):
   - **SDK**: Select `21`.
   - **Language level**: Select `21 - Pattern matching for switch`.
3. In **Modules** settings (left sidebar):
   - Select `concierge`.
   - **Dependencies** tab: Ensure **Module SDK** is set to `Project SDK (21)`.

### 4. Re-import Maven
1. Open the **Maven** tool window (usually on the right sidebar).
2. Click the **Reload All Maven Projects** button (circular arrows icon).

### 5. Try Running Again
Run your Maven command or application again.

---

## Alternative: Use the Terminal

If IntelliJ is still giving you trouble, use the terminal **inside IntelliJ** (Alt+F12) and run:

```bash
./build-with-java21.sh
```

This script forces the correct Java version regardless of IntelliJ's settings.

