import re
import sys

def process_file(filepath, has_viewmodel):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "com.sahay.ai.ui.Localization" not in content:
        content = content.replace("package com.sahay.ai.ui.screens\n", "package com.sahay.ai.ui.screens\n\nimport com.sahay.ai.ui.Localization\n")
    
    # Simple regex to replace Text("Some String"
    # We must be careful not to replace Text("Text with variables ${var}")
    # We'll just replace the keys in Localization.kt
    keys = [
        "SAHAY-AI", "Public Trust Infrastructure", "Select Portal Access",
        "Victim/Citizen", "Access your journey & support", "Official Portal", "Manage active case queues",
        "Preferred Language", "Select your communication language", "Continue",
        "Your choice. Your control.", "SAHAY-AI uses your check-ins to understand changes in your wellbeing. Your information is protected and accessed only by authorized personnel.",
        "CONSENT-BASED — You choose whether to participate.", "PRIVATE — Your information is protected.", "HUMAN SUPPORT — AI signals are reviewed by authorized people.",
        "I understand and consent to wellbeing monitoring.", "Wellbeing monitoring requires your consent to continue.", "Give Consent", "Not Now",
        "Registration & Linkage", "Link your secure case record", "Full Name", "Enter your full name",
        "Mobile Number", "Enter your mobile number", "Case ID", "Enter your case ID", "Trusted Person’s Mobile Number",
        "Enter trusted person’s number", "You may provide a trusted person’s number for urgent support. This is optional.",
        "Is this you? Log in securely", "Link Profile", "You’re all set", "Your details have been securely saved. You can now begin your wellbeing check-ins and access support.",
        "Go to My Dashboard", "HOME", "CHAT", "JOURNEY", "SUPPORT", "PROFILE", "Good evening.", "How are you doing today?",
        "TODAY'S CHECK-IN", "Take a moment to tell us how you are feeling.", "Start Check-in", "OTHER WAYS TO CONNECT",
        "Talk to SAHAY", "VOICE", "Speak instead", "CALL", "Scheduled Call", "Last Check-in", "Distress Score", "Message SAHAY-AI...",
        "Your Legal Journey", "Case Status", "Timeline", "Support Services", "Emergency SOS", "Trigger immediate official response",
        "Trusted Contacts", "Profile & Settings", "Secure Sign Out", "Wellbeing Check-in", "On a scale from 0 to 100, where 100 corresponds to extreme distress, how do you feel today?",
        "Score (0-100)", "Submit Securely", "Cancel", "Stable", "Supported"
    ]
    
    for k in keys:
        # Replace Text("Key" with Text(Localization.getString("Key", viewModel.selectedLanguage)
        # Because in some cases it's Text(text = "Key", ...
        # Or label = { Text("Key") }
        # Or placeholder = { Text("Key") }
        # Let's replace literally `"${k}"` with `Localization.getString("${k}", viewModel.selectedLanguage)`
        # wait, we must be careful with modifiers or string interpolation. We only replace complete quoted strings.
        target = f'"{k}"'
        replacement = f'Localization.getString("{k}", viewModel.selectedLanguage)'
        content = content.replace(target, replacement)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    process_file(r"c:\Users\Jackson\OneDrive\Documents\Sahay Ai\SahayAiApp\app\src\main\java\com\sahay\ai\ui\screens\InitialScreens.kt", True)
    process_file(r"c:\Users\Jackson\OneDrive\Documents\Sahay Ai\SahayAiApp\app\src\main\java\com\sahay\ai\ui\screens\VictimScreens.kt", True)
    print("Done")
