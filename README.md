# 📚 StudyNoters - Android Note-Taking App

**StudyNoters** is a modern Android note-taking app built with **Jetpack Compose** and **Firebase**.  
Designed with a clean UI and practical features, it helps users take notes, set reminders, and manage personal settings—all in a lightweight experience.

---

## 🚀 Features

- ✅ **User Authentication** (Register / Login / Forgot Password)  
- ✅ **Personalized Notes** – Add, view, update, and delete notes  
- ✅ **Reminder Alarms** – Set exact reminders with system notifications  
- ✅ **Settings** – Change display name and reset password  
- ✅ **Splash Screen** with branding  
- ✅ **Navigation Drawer** with Add / View / Settings / Logout  
- ✅ **Firestore + Firebase Auth + FCM Integration**  
- ✅ **Dark Mode Support**  
- ✅ **Reminder Receiver** with Firestore sync  

---

## 📱 Screenshots

👉 [Click here to view all screenshots in PDF](https://github.com/MonangiUpendra/Study-Notes-App/blob/main/StudyNoters_Screenshots.pdf)

---

## 🛠️ Technologies Used

- Kotlin  
- Jetpack Compose  
- Firebase Authentication  
- Cloud Firestore  
- Firebase Cloud Messaging  
- AlarmManager (for exact reminders)  
- Jetpack Navigation  
- Material 3 Design  
- DataStore (for theme preference)  

---

## 🔧 Setup Instructions

1. **Clone the repository:**

   ```bash
   git clone https://github.com/MonangiUpendra/Study-Notes-App
   cd StudyNoters
   ```

2. **Open in Android Studio**

3. **Add your Firebase config:**

   Place your `google-services.json` file inside the `app/` directory

4. **Enable Firebase Services in Console:**
   - Firestore  
   - Authentication (Email/Password)  
   - Cloud Messaging  

5. **Run the app! 🚀**

---

## 🔒 Permissions Used

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
```

---

## 🧪 Unit Tests

🧪 `NoteValidatorTest.kt` validates:
- Title is not empty  
- Reminder time is in the future  

---

## 👨‍💻 Developed By

**Monangi Upendra**  
_Android Developer | Firebase Enthusiast | Kotlin Learner_