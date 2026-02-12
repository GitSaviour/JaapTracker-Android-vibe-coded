# JaapTracker-Compose 🕉️

**JaapTracker** is a minimalist, high-performance mantra counter for Android. I built this to solve a personal problem: most spiritual tracking apps are either clunky, full of ads, or don't let you manage multiple people.

This project is 100% **vibecoded**. Every transition, button press, and data flow was built to feel intuitive and out of the way, so the focus stays on the *Jaap*, not the screen.

## ✨ Features (The Full List)

* **Multi-Profile Architecture:** You aren't locked into one counter. Create separate profiles for family members, different types of malas (e.g., Tulsi vs. Rudraksh), or specific long-term goals.
* **Flexible Daily Logging:**
* **Log Today:** For when you're tracking in real-time or just finished a session.
* **Back-dating (Log at Date):** Life happens. If you forgot to log your counts from three days ago, you can jump back on the calendar and add them.


* **Live History & Correction:** Every profile has a dedicated history screen.
* **Visual Logs:** View every entry as a clean card showing the date and the count.
* **Edit on Tap:** Typos suck. Tap any history card to pull up an edit dialog and fix the number.
* **Long-press/Delete:** Clean up accidental entries easily.


* **Custom Date-Range Analytics:** This is the "Anushthan" tool. Pick a start date and an end date, and the app will instantly calculate the total sum of japs performed in that window.
* **Modern Material 3 UI:** Built entirely with Jetpack Compose. It supports dynamic colors, smooth scrolling, and has a "hidden" spiritual vibe with encouraging prompts like *“Om namoh Narayana”* tucked into the UI.
* **Dev-Contact Integration:** I’ve put a direct "mail me" link in the top bar. It copies my email to your clipboard and opens your mail app automatically.

---
## ScreenShots

<img width="385" height="854" alt="image" src="https://github.com/user-attachments/assets/f78bf4ba-9b17-47c9-8197-96a7177b7b07" />
<img width="402" height="864" alt="image" src="https://github.com/user-attachments/assets/8bafdb13-a49b-493a-a52e-425aeee33325" />
<img width="369" height="552" alt="image" src="https://github.com/user-attachments/assets/7385ecb0-aeae-4215-a0c7-6e49d9322446" />
<img width="398" height="870" alt="image" src="https://github.com/user-attachments/assets/c79ba295-6b65-4918-9000-2c40cd6c3e10" />



## 🛠️ The Tech Stack

* **UI:** 100% Jetpack Compose (Material 3).
* **Database:** Room (SQLite) with a custom TypeConverter for handling `LocalDate`.
* **Architecture:** Clean MVVM. I used `StateFlow` and `flatMapLatest` to make sure the UI stays reactive and never shows stale data.
* **Data Handling:** GSON for JSON serialization.

---

## ⚠️ The "Import/Export" Situation (Read This)

I’ve included a backup system, but let’s be real: it’s currently **experimental and unstable.**

* **The Issue:** Right now, the app uses auto-incrementing `Long` IDs for profiles and logs. When you export data from one phone and import it to another, the new phone tries to assign its own IDs, which breaks the "link" between the profile and the logs.
* **The Result:** The import might look successful, but clicking an imported profile often causes an immediate crash because the database can't find the parent ID.
* **The Plan:** I am currently refactoring the entire ID system to use **UUID Strings**. Since Strings are unique and don't rely on a "counter," this will make imports 100% stable. For now, consider the export a "read-only" backup.

---

## 🚀 Getting it Running

1. Clone the repo.
2. Open in **Android Studio (Ladybug or later)**.
3. Make sure your `libs.versions.toml` or `build.gradle` has these:
* `androidx.compose.material:material-icons-extended`
* `com.google.code.gson:gson`


4. Build and enjoy the vibe.

---

## 👨‍💻 Dev

**Vansh Sharma** Built with a focus on flow.
