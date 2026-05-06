# GitHub Actions & Release Setup Guide

This guide explains how to configure your GitHub repository to enable automated builds, signing, and releases using the provided workflows.

## 1. Required GitHub Secrets

To make the "Release" workflow work, you need to add the following secrets to your repository.
Navigate to: **Settings > Secrets and variables > Actions > New repository secret**.

| Secret Name | Description |
|-------------|-------------|
| `ANDROID_SIGNING_KEY` | The **Base64 encoded** content of your `.keystore` or `.jks` file. |
| `ANDROID_KEY_ALIAS` | The alias name you gave your key during creation. |
| `ANDROID_KEYSTORE_PASSWORD` | The password for the keystore file. |
| `ANDROID_KEY_PASSWORD` | The password for the specific key alias. |
| `YANDEX_CLIENT_ID` | Your Yandex OAuth App Client ID (for cloud sync). |
| `YANDEX_CLIENT_SECRET` | Your Yandex OAuth App Client Secret (for cloud sync). |
| `ACCESS_TOKEN` | (Optional) A Personal Access Token (PAT) with `repo` scope for string sorting. |

---

## 2. Generating Android Signing Credentials

If you don't have a keystore yet, you can create one using the `keytool` command (installed with the JDK):

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### Encoding the Keystore
GitHub Secrets only accept text. You must convert your binary keystore file to a Base64 string:

**Linux / macOS:**
```bash
base64 -w0 release-key.jks
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks"))
```

Copy the resulting long string and paste it into the `ANDROID_SIGNING_KEY` secret.

---

## 3. Obtaining Yandex API Credentials

1. Go to the [Yandex OAuth Platform](https://oauth.yandex.ru/).
2. Register a new application.
3. Select **"Web services"** as the platform.
4. Set the **Callback URI** to `https://oauth.yandex.ru/verification_code` (or use the one specified in the app's networking logic).
5. Grant permissions for **Yandex Disk REST API**.
6. Copy the **ID** and **Password** (Secret) to your GitHub secrets.

---

## 4. Releasing via Obtainium

[Obtainium](https://github.com/ImranR04/Obtainium) allows you to install and update apps directly from GitHub Releases.

### How to set it up:
1. **Trigger a Release:** Go to the **Actions** tab in your GitHub repo, select the **Release** workflow, and click **Run workflow**.
2. **Copy Repo URL:** Once the workflow finishes, it will create a new "Release" on the main page of your repository.
3. **Add to Obtainium:**
   - Open Obtainium on your Android device.
   - Click **Add App**.
   - Paste your GitHub repository URL (e.g., `https://github.com/yourusername/HomeMedkit-App`).
   - Obtainium will automatically detect the latest release, download the `HomeMedkit.apk`, and prompt you to install it.
   - In the future, every time you run the **Release** workflow, Obtainium will notify you of an update.

---

## 5. Automated String Sorting (Optional)

The `sort_android_strings.yml` workflow is designed to work with Weblate. If you are using a Personal Access Token (PAT) for this:
1. Go to your **GitHub Profile Settings > Developer Settings > Personal access tokens (classic)**.
2. Generate a token with `repo` scope.
3. Add it as `ACCESS_TOKEN` in repository secrets.
