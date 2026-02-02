# Git Setup and Commit Guide

## Issue: Git Not Installed

Git is not currently installed on your Windows system. You have two options to commit your project to GitHub:

---

## Option 1: Install Git and Use Command Line (Recommended)

### Step 1: Install Git for Windows

1. Download Git from: https://git-scm.com/download/win
2. Run the installer
3. Use default settings (recommended)
4. Restart your terminal/PowerShell after installation

### Step 2: Configure Git

Open PowerShell or Command Prompt and run:

```bash
git config --global user.name "manojmanoj22250-stack"
git config --global user.email "your-email@example.com"
```

### Step 3: Initialize and Commit

Navigate to your project directory and run:

```bash
cd "C:\Users\student\Desktop\AD_FSD\Mobile-app"

# Initialize git repository
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: App Addiction Controller Android app"

# Add remote repository
git remote add origin https://github.com/manojmanoj22250-stack/mobile-app.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### Step 4: Enter GitHub Credentials

When prompted, enter your GitHub username and password (or personal access token).

---

## Option 2: Use GitHub Desktop (Easier for Beginners)

### Step 1: Install GitHub Desktop

1. Download from: https://desktop.github.com/
2. Install and sign in with your GitHub account

### Step 2: Add Repository

1. Click **File** → **Add Local Repository**
2. Browse to: `C:\Users\student\Desktop\AD_FSD\Mobile-app`
3. Click **Add Repository**
4. If prompted to create a repository, click **Create**

### Step 3: Commit Changes

1. You'll see all files listed in the "Changes" tab
2. Add a commit message: "Initial commit: App Addiction Controller Android app"
3. Click **Commit to main**

### Step 4: Publish to GitHub

1. Click **Publish repository**
2. Repository name: `mobile-app`
3. Uncheck "Keep this code private" (if you want it public)
4. Click **Publish Repository**

---

## Option 3: Upload via GitHub Web Interface

### Step 1: Create Repository on GitHub

1. Go to: https://github.com/manojmanoj22250-stack/mobile-app
2. If repository doesn't exist, create it
3. Click **uploading an existing file**

### Step 2: Upload Files

1. Drag and drop the entire `Mobile-app` folder
2. Or click "choose your files" and select all files
3. Add commit message: "Initial commit: App Addiction Controller"
4. Click **Commit changes**

**Note**: This method is not ideal for large projects with many files.

---

## Recommended Approach

**Use Option 1 (Git Command Line)** for the best experience and full control.

### Quick Install Commands

After installing Git, run these commands in PowerShell:

```powershell
# Navigate to project
cd "C:\Users\student\Desktop\AD_FSD\Mobile-app"

# Initialize git
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: App Addiction Controller

Features:
- App selection and time limit configuration
- Real-time usage tracking with UsageStatsManager
- Warning system (3 warnings at 1h intervals)
- Auto-blocking after warnings ignored
- 1-hour block period
- Offline support with Room Database
- Material 3 UI design
- Background monitoring service"

# Add remote
git remote add origin https://github.com/manojmanoj22250-stack/mobile-app.git

# Push to GitHub
git branch -M main
git push -u origin main
```

---

## Troubleshooting

### Authentication Error

If you get an authentication error, you need to create a Personal Access Token:

1. Go to GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Select scopes: `repo` (all)
4. Copy the token
5. Use the token as your password when pushing

### Repository Already Exists

If the repository already has content:

```bash
# Pull first, then push
git pull origin main --allow-unrelated-histories
git push -u origin main
```

---

## Files to Commit

Your project includes:
- ✅ All source code files (.kt, .xml)
- ✅ Build configuration (build.gradle.kts, settings.gradle.kts)
- ✅ AndroidManifest.xml
- ✅ Resources (layouts, drawables, values)
- ✅ README.md
- ✅ .gitignore (excludes build files)

The `.gitignore` file will automatically exclude:
- Build outputs
- IDE files
- Local configuration
- APK files

---

## Next Steps After Commit

1. ✅ Verify files on GitHub
2. Add repository description
3. Add topics/tags (android, kotlin, productivity)
4. Update README with screenshots (optional)
5. Add license file (optional)

---

**Need Help?** Let me know which option you'd like to use, and I can provide more specific guidance!
